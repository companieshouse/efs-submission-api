package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;

@ExtendWith(MockitoExtension.class)
public class FetchSubmissionControllerTest {

    private FetchSubmissionController fetchSubmissionController;

    @Mock
    private SubmissionService service;

    @Mock
    private SubmissionApi response;

    @BeforeEach
    void setUp() {
        this.fetchSubmissionController = new FetchSubmissionController(service);
    }

    @Test
    void testSubmissionControllerReturnsSubmission() {
        //given
        when(service.readSubmission(any())).thenReturn(response);

        //when
        ResponseEntity<SubmissionApi> actual = fetchSubmissionController.fetchSubmission("123");

        //then
        assertEquals(response, actual.getBody());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void testSubmissionControllerReturnsNotFoundWhenSubmissionDoesNotExist() {
        //given

        //when
        ResponseEntity<SubmissionApi> actual = fetchSubmissionController.fetchSubmission("123");

        //then
        assertNull(actual.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actual.getStatusCode());
    }

}
