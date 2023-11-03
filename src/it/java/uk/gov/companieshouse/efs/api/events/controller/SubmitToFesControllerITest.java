package uk.gov.companieshouse.efs.api.events.controller;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.IntegrationTestBase;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeRequest;
import uk.gov.companieshouse.efs.api.events.service.model.BarcodeResponse;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubmitToFesControllerITest extends IntegrationTestBase {

    private static final String SUBMISSION_ID = "1234abcd5678defa9012bcde";
    private static final String CONVERTED_TIFF_OBJECT_KEY = "converted-tiffs/eeeeeeee-dddd-cccc-bbbb-aaaaaaaaaaaa";
    private static final String TIFF_FILE_NAME = "/Hello World.tiff";

    @Autowired
    private MockMvc mockMvc;

    private MockServerClient mockServerClient = getMockServerClient();

    @BeforeEach
    protected void before() {
        super.before();
    }

    @AfterEach
    protected void after() throws ExecutionException, InterruptedException {
        super.after();
        getMongoTemplate().remove(Query.query(Criteria.where("_id").is(SUBMISSION_ID)), getSubmissionCollectionName());
        getS3Client().deleteObject(DeleteObjectRequest.builder()
                .key(CONVERTED_TIFF_OBJECT_KEY)
                .bucket(getBucketName())
                .build());
    }

    @Test
    @Disabled("Oracle container image unavailable")
    void testSubmitToFes() throws Exception {
        //given {an application has been submitted}
        getMongoTemplate().insert(Document.parse(IOUtils.resourceToString("/submission-ready-to-submit.json", StandardCharsets.UTF_8)), getSubmissionCollectionName());

        //and {barcode service will return a Y-prefixed barcode}
        mockServerClient.when(request()
                .withMethod(HttpMethod.POST.toString())
                .withPath("/barcode"))
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                        .withBody(JsonBody.json(new BarcodeResponse("Y9999999"))));

        //and {a TIFF file exists in the S3 bucket for the submission}
        getS3Client().putObject(PutObjectRequest.builder()
                .key(CONVERTED_TIFF_OBJECT_KEY)
                .bucket(getBucketName())
                .build(), RequestBody.fromBytes(IOUtils.resourceToByteArray(TIFF_FILE_NAME)));

        //when {the submission is submitted to FES}
        mockMvc.perform(post("/efs-submission-api/events/submit-files-to-fes")
                .contentType("application/json")
                .accept("application/json")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());

        //then {a Y-prefixed barcode should be requested from barcode generator}
        mockServerClient.verify(request().withMethod(HttpMethod.POST.toString())
                .withPath("/barcode")
                .withBody(JsonBody.json(new BarcodeRequest(20200101, true))));

        //and {submission status should be SENT_TO_FES}
        Submission actual = getMongoTemplate().findById(SUBMISSION_ID, Submission.class, getSubmissionCollectionName());
        assertEquals(SubmissionStatus.SENT_TO_FES, actual.getStatus());

        //and {records should be inserted into FES}
        assertEquals(1, getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM BATCH", Integer.class));
        assertEquals(1, getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM ENVELOPE", Integer.class));
        assertEquals(1, getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM IMAGE", Integer.class));
        assertEquals(1, getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM FORM", Integer.class));
    }

}
