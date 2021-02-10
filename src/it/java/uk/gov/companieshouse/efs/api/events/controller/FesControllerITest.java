package uk.gov.companieshouse.efs.api.events.controller;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.api.BaseIntegrationTest;
import uk.gov.companieshouse.efs.api.submissions.model.RejectReason;
import uk.gov.companieshouse.efs.api.submissions.model.Submission;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FesControllerITest extends BaseIntegrationTest {

    private static final String SUBMISSION_ID = "1234abcd5678defa9012bcde";
    private static final String SUBMISSION_COLLECTION_NAME = "submissions";

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    protected void after() throws InterruptedException, ExecutionException {
        super.after();
        getMongoTemplate().remove(Query.query(Criteria.where("_id").is(SUBMISSION_ID)), getSubmissionCollectionName());
    }

    @Test
    @Disabled("Oracle container image unavailable")
    void testRejectSubmission() throws Exception {
        //given {an application has been submitted}
        getMongoTemplate().insert(Document.parse(IOUtils.resourceToString("/submission-sent-to-fes.json", StandardCharsets.UTF_8)), SUBMISSION_COLLECTION_NAME);

        //when {the submission is rejected}
        mockMvc.perform(post("/efs-submission-api/fes/submissions/Y9999999/complete")
                .content("{\"status\": \"REJECTED\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());

        //then {submission status should be REJECTED}
        Submission actual = getMongoTemplate().findById(SUBMISSION_ID, Submission.class, SUBMISSION_COLLECTION_NAME);
        assertEquals(SubmissionStatus.REJECTED, actual.getStatus());

        //and {reject reasons should be added to the submission}
        assertEquals(Collections.singletonList(new RejectReason("Invalid form")), actual.getChipsRejectReasons());

        //and {an email message should be sent to the email-send topic}
        assertTrue(newKafkaOffsetsHaveBeenPublished());
    }

    @Test
    void testAcceptSubmission() throws Exception {
        //given {an application has been submitted}
        getMongoTemplate().insert(Document.parse(IOUtils.resourceToString("/submission-sent-to-fes.json", StandardCharsets.UTF_8)), SUBMISSION_COLLECTION_NAME);

        //when {the submission is rejected}
        mockMvc.perform(post("/efs-submission-api/fes/submissions/Y9999999/complete")
                .content("{\"status\": \"ACCEPTED\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isOk());

        //then {submission status should be ACCEPTED}
        Submission actual = getMongoTemplate().findById(SUBMISSION_ID, Submission.class, SUBMISSION_COLLECTION_NAME);
        assertEquals(SubmissionStatus.ACCEPTED, actual.getStatus());

        //and {an email mesage should be sent to the email-send topic}
        assertTrue(newKafkaOffsetsHaveBeenPublished());
    }
}
