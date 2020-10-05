package uk.gov.companieshouse.efs.api.events.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import uk.gov.companieshouse.efs.api.events.service.model.Decision;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;
import uk.gov.companieshouse.efs.api.util.IdentifierGeneratable;

@Service
public class SqsMessageService implements MessageService {

    private static final uk.gov.companieshouse.logging.Logger LOGGER = uk.gov.companieshouse.logging.LoggerFactory.getLogger("efs-submission-api");

    private static final String SUBMISSION_KEY = "submissionId";
    private static final String FILE_KEY = "fileId";
    private static final String STRING_DATA_TYPE = "String";
    private static final String MESSAGE_BODY = "efs-submission-api";

    private SqsClient client;
    private String queueUrl;
    private IdentifierGeneratable idGenerator;
    private int messagePartitionSize;

    /**
     * Constructor.
     *
     * @param client                dependency
     * @param idGenerator           dependency
     * @param queueUrl              dependency
     * @param messagePartitionSize  dependency
     */
    public SqsMessageService(SqsClient client, @Qualifier("idGenerator") IdentifierGeneratable idGenerator,
        @Value("${aws.sqs.queue.url}") String queueUrl, @Value("${message.partition.size}") int messagePartitionSize) {
        this.client = client;
        this.idGenerator = idGenerator;
        this.queueUrl = queueUrl;
        this.messagePartitionSize = messagePartitionSize;
    }

    @Override
    public void queueMessages(List<Decision> submissions) {
        List<SendMessageBatchRequestEntry> entries = submissions
                .stream()
                .map(Decision::getSubmission)
                .flatMap(this::getMessageBatchRequestEntries)
                .collect(Collectors.toList());
        entries.forEach(entry -> LOGGER.debug(
                String.format("Sending message for submission [%s] and file [%s] with message id [%s]",
                        entry.messageAttributes().get(SUBMISSION_KEY).stringValue(),
                        entry.messageAttributes().get(FILE_KEY).stringValue(),
                        entry.id())));

        Streams.stream(Iterables.partition(entries, messagePartitionSize))
                .forEach(partition -> client.sendMessageBatch(SendMessageBatchRequest.builder()
                        .queueUrl(queueUrl)
                        .entries(partition)
                        .build()));
        LOGGER.debug(String.format("Sent %d messages to SQS", entries.size()));
    }

    private Stream<SendMessageBatchRequestEntry> getMessageBatchRequestEntries(Submission submission) {
        return submission.getFormDetails().getFileDetailsList().stream()
                .map(file -> SendMessageBatchRequestEntry.builder()
                        .messageAttributes(getMessageAttributes(submission.getId(), file.getFileId()))
                        .id(idGenerator.generateId()).messageBody(MESSAGE_BODY).messageGroupId(MESSAGE_BODY)
                        .messageDeduplicationId(idGenerator.generateId()).build());
    }

    private Map<String, MessageAttributeValue> getMessageAttributes(String submissionId, String fileId) {
        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put(SUBMISSION_KEY,
                MessageAttributeValue.builder().dataType(STRING_DATA_TYPE).stringValue(submissionId).build());
        attributes.put(FILE_KEY,
                MessageAttributeValue.builder().dataType(STRING_DATA_TYPE).stringValue(fileId).build());
        return attributes;
    }
}
