package uk.gov.companieshouse.efs.api.events.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.submissions.model.FormDetails;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;

@ExtendWith(MockitoExtension.class)
public class SqsMessageServiceTest {

    private SqsMessageService messageService;

    @Mock
    private SqsClient client;

    @Mock
    private IdentifierGeneratable idGenerator;

    @BeforeEach
    void setUp() {
        this.messageService = new SqsMessageService(client, idGenerator, "queue", 2);
    }

    @Test
    void testSqsMessageServiceSendsOneMessagePerAttachedSubmissionFile() {
        //given
        Submission submissionSingleFile = Submission.builder()
                .withId("abc123")
                .withFormDetails(
                        new FormDetails(
                                null,
                                null,
                                Collections.singletonList(
                                        new FileDetails("1234-5678-9012-3456", "out.txt", 5L, null, null, null, null)
                                )
                        )
                ).build();
        Submission submissionMultiFile = Submission.builder()
                .withId("abc124")
                .withFormDetails(
                        new FormDetails(
                                null,
                                null,
                                Arrays.asList(
                                        new FileDetails("1234-5678-9012-3457", "1.txt", 5L, null, null, null, null),
                                        new FileDetails("1234-5678-9012-3458", "2.txt", 6L, null, null, null, null)
                                )
                        )
                ).build();
        Decision decisionSingleFile = mock(Decision.class);
        Decision decisionMultiFile = mock(Decision.class);
        when(decisionSingleFile.getSubmission()).thenReturn(submissionSingleFile);
        when(decisionMultiFile.getSubmission()).thenReturn(submissionMultiFile);
        List<Decision> completedSubmissions = Arrays.asList(decisionSingleFile, decisionMultiFile);
        when(idGenerator.generateId()).thenReturn("id1", "deduplicationId1", "id2", "deduplicationId2", "id3",
                "deduplicationId3");

        //when
        messageService.queueMessages(completedSubmissions);

        //then
        verify(client).sendMessageBatch(expectedSendMessageBatchRequestPart1());
        verify(client).sendMessageBatch(expectedSendMessageBatchRequestPart2());
    }

    @Test
    void testSqsMessageServiceWithEmptySubmissionList() {
        //given
        List<Decision> completedSubmissions = Collections.emptyList();

        //when
        messageService.queueMessages(completedSubmissions);

        //then
        verifyNoInteractions(client);
    }

    private SendMessageBatchRequest expectedSendMessageBatchRequestPart1() {
        return SendMessageBatchRequest.builder()
                .queueUrl("queue")
                .entries(SendMessageBatchRequestEntry.builder()
                        .id("id1")
                        .messageBody("efs-submission-api")
                        .messageAttributes(expectedEntries("abc123", "1234-5678-9012-3456"))
                        .messageGroupId("efs-submission-api")
                        .messageDeduplicationId("deduplicationId1")
                        .build(), SendMessageBatchRequestEntry.builder()
                        .id("id2")
                        .messageBody("efs-submission-api")
                        .messageAttributes(expectedEntries("abc124", "1234-5678-9012-3457"))
                        .messageGroupId("efs-submission-api")
                        .messageDeduplicationId("deduplicationId2")
                        .build())
                .build();
    }

    private SendMessageBatchRequest expectedSendMessageBatchRequestPart2() {
        return SendMessageBatchRequest.builder()
                .queueUrl("queue")
                .entries(SendMessageBatchRequestEntry.builder()
                        .id("id3")
                        .messageBody("efs-submission-api")
                        .messageAttributes(expectedEntries("abc124", "1234-5678-9012-3458"))
                        .messageGroupId("efs-submission-api")
                        .messageDeduplicationId("deduplicationId3")
                        .build())
                .build();
    }

    private Map<String, MessageAttributeValue> expectedEntries(String submissionId, String fileId) {
        Map<String, MessageAttributeValue> expected = new HashMap<>();
        expected.put("submissionId", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(submissionId)
                .build());
        expected.put("fileId", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(fileId)
                .build());
        return expected;
    }
}
