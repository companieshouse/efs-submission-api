package uk.gov.companieshouse.efs.api.events.controller;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.BaseIntegrationTest;
import uk.gov.companieshouse.efs.api.filetransfer.model.FileTransferDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SubmissionPollingControllerITest extends BaseIntegrationTest {

    private static final String SUBMISSION_ID = "1234abcd5678defa9012bcde";
    private static final String SUBMISSION_COLLECTION_NAME = "submissions";
    private static final String FILE_TRANSFER_API_KEY_HEADER = "x-api-key";
    private static final String FILE_TRANSFER_API_KEY_VALUE = "1234";
    private static final String FILE_ID = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    private static final String PDF_PATH = "/test/files/" + FILE_ID;
    private static final String PDF_FILENAME = "CC01.pdf";
    private static final long PDF_SIZE = 100L;
    private static final String AV_STATUS_CLEAN = "clean";
    private static final String AV_STATUS_INFECTED = "infected";

    private static final String FILE_ID_MESSAGE_ATTRIBUTE = "fileId";
    private static final String SUBMISSION_ID_MESSAGE_ATTRIBUTE = "submissionId";

    @Autowired
    private MockMvc mockMvc;

    private MockServerClient mockServerClient = getMockServerClient();

    @AfterEach
    protected void after() throws InterruptedException, ExecutionException {
        super.after();
        mockServerClient.reset();
        getMongoTemplate().remove(Query.query(Criteria.where("_id").is(SUBMISSION_ID)), getSubmissionCollectionName());
        getSqsClient().purgeQueue(PurgeQueueRequest.builder()
                .queueUrl(getQueueUrl())
                .build());
    }

    @Test
    void testQueueFileIfFormIsFESEnabled() throws Exception {
        // given {an application has been submitted}
        getMongoTemplate().insert(Document.parse(IOUtils.resourceToString("/submission.json", StandardCharsets.UTF_8)), SUBMISSION_COLLECTION_NAME);

        //and {the file attached to the submission is clean}
        mockServerClient.when(
            request()
                .withMethod(HttpMethod.GET.toString())
                .withHeader(FILE_TRANSFER_API_KEY_HEADER, FILE_TRANSFER_API_KEY_VALUE)
                .withPath(PDF_PATH))
            .respond(response()
                .withStatusCode(HttpStatus.SC_OK)
                .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                .withBody(JsonBody.json(expectedFileTransferDetails(AV_STATUS_CLEAN)))
        );

        // when {the submission is processed}
        mockMvc.perform(post("/efs-submission-api/events/queue-files")
                .contentType("application/json")
                .accept("application/json")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());

        //then {file metadata should be obtained from file-transfer-api}
        mockServerClient.verify(request()
                .withMethod(HttpMethod.GET.toString())
                .withPath(PDF_PATH)
                .withHeader(FILE_TRANSFER_API_KEY_HEADER, FILE_TRANSFER_API_KEY_VALUE));

        //and {submission status should be PROCESSING and file conversion status should be QUEUED}
        Submission actual = getMongoTemplate().findById(SUBMISSION_ID, Submission.class, SUBMISSION_COLLECTION_NAME);
        assertEquals(SubmissionStatus.PROCESSING, actual.getStatus());
        assertEquals(FileConversionStatus.QUEUED, actual.getFormDetails().getFileDetailsList().get(0).getConversionStatus());

        //and {a message containing the submission ID and file ID should be sent to the target SQS queue}
        ReceiveMessageResponse receiveMessageResponse = getSqsClient().receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(getQueueUrl())
                        .messageAttributeNames(SUBMISSION_ID_MESSAGE_ATTRIBUTE, FILE_ID_MESSAGE_ATTRIBUTE)
                        .build());
        assertEquals(SUBMISSION_ID, receiveMessageResponse.messages().get(0).messageAttributes().get(SUBMISSION_ID_MESSAGE_ATTRIBUTE).stringValue());
        assertEquals(FILE_ID, receiveMessageResponse.messages().get(0).messageAttributes().get(FILE_ID_MESSAGE_ATTRIBUTE).stringValue());
    }

    @Test
    void testSendEmailIfFormNotFesEnabled() throws Exception {
        // given {an application has been submitted}
        getMongoTemplate().insert(Document.parse(IOUtils.resourceToString("/submission-non-fes.json", StandardCharsets.UTF_8)), SUBMISSION_COLLECTION_NAME);

        //and {the file attached to the submission is clean}
        mockServerClient.when(
                request()
                        .withMethod(HttpMethod.GET.toString())
                        .withHeader(FILE_TRANSFER_API_KEY_HEADER, FILE_TRANSFER_API_KEY_VALUE)
                        .withPath(PDF_PATH))
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                        .withBody(JsonBody.json(expectedFileTransferDetails(AV_STATUS_CLEAN)))
                );

        // when {the submission is processed}
        mockMvc.perform(post("/efs-submission-api/events/queue-files")
                .contentType("application/json")
                .accept("application/json")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());

        //then {file metadata should be obtained from file-transfer-api}
        mockServerClient.verify(request()
                .withMethod(HttpMethod.GET.toString())
                .withPath(PDF_PATH)
                .withHeader(FILE_TRANSFER_API_KEY_HEADER, FILE_TRANSFER_API_KEY_VALUE));

        //and {submission status should be PROCESSED_BY_EMAIL}
        Submission actual = getMongoTemplate().findById(SUBMISSION_ID, Submission.class, SUBMISSION_COLLECTION_NAME);
        assertEquals(SubmissionStatus.PROCESSED_BY_EMAIL, actual.getStatus());

        //and {an email mesage should be sent to the email-send topic}
        assertTrue(newKafkaOffsetsHaveBeenPublished());
    }

    @Test
    void testSendEmailIfFileNotClean() throws Exception {
        // given {an application has been submitted}
        getMongoTemplate().insert(Document.parse(IOUtils.resourceToString("/submission.json", StandardCharsets.UTF_8)), SUBMISSION_COLLECTION_NAME);

        //and {the file attached to the submission is infected}
        mockServerClient.when(
                request()
                        .withMethod(HttpMethod.GET.toString())
                        .withHeader(FILE_TRANSFER_API_KEY_HEADER, FILE_TRANSFER_API_KEY_VALUE)
                        .withPath(PDF_PATH))
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                        .withBody(JsonBody.json(expectedFileTransferDetails(AV_STATUS_INFECTED)))
                );

        // when {the submission is processed}
        mockMvc.perform(post("/efs-submission-api/events/queue-files")
                .contentType("application/json")
                .accept("application/json")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());

        //then {file metadata should be obtained from file-transfer-api}
        mockServerClient.verify(request()
                .withMethod(HttpMethod.GET.toString())
                .withPath(PDF_PATH)
                .withHeader(FILE_TRANSFER_API_KEY_HEADER, FILE_TRANSFER_API_KEY_VALUE));

        //and {submission status should be REJECTED_BY_VIRUS_SCAN and file conversion status should be FAILED_AV}
        Submission actual = getMongoTemplate().findById(SUBMISSION_ID, Submission.class, SUBMISSION_COLLECTION_NAME);
        assertEquals(SubmissionStatus.REJECTED_BY_VIRUS_SCAN, actual.getStatus());
        assertEquals(FileConversionStatus.FAILED_AV, actual.getFormDetails().getFileDetailsList().get(0).getConversionStatus());

        //and {no messages should be sent to SQS}
        ReceiveMessageResponse receiveMessageResponse = getSqsClient().receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(getQueueUrl())
                .messageAttributeNames(SUBMISSION_ID_MESSAGE_ATTRIBUTE, FILE_ID_MESSAGE_ATTRIBUTE)
                .build());
        assertTrue(receiveMessageResponse.messages().isEmpty());

        //and {an email mesage should be sent to the email-send topic}
        assertTrue(newKafkaOffsetsHaveBeenPublished());
    }

    private FileTransferDetails expectedFileTransferDetails(String avStatus) {
        FileTransferDetails result = new FileTransferDetails();
        result.setAvStatus(avStatus);
        result.setAvTimestamp(LocalDateTime.of(2020, 1, 1, 12, 20).toString());
        result.setContentType(ContentType.APPLICATION_JSON.toString());
        result.setId(FILE_ID);
        result.setSize(PDF_SIZE);
        result.setName(PDF_FILENAME);
        return result;
    }
}
