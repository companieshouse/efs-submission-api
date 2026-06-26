package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Validation;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.model.efs.submissions.FileApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private static final String SUBMISSION_ID = "SUB-001";
    private static final String ENDPOINT = "/efs-submission-api/submission/{id}/files";

    @Mock
    private SubmissionService submissionService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        final var fileController = new FileController(submissionService);
        final SpringValidatorAdapter validator;
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = new SpringValidatorAdapter(validatorFactory.getValidator());
        }
        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
            .setValidator(validator)
            .build();
        objectMapper = JsonMapper.builder().build();
    }

    @Test
    void shouldAcceptValidFilenamesAndReturnOk() throws Exception {
        final var expectedResponse = new SubmissionResponseApi(SUBMISSION_ID);
        when(submissionService.updateSubmissionWithFileDetails(any(), any()))
            .thenReturn(expectedResponse);

        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "invoice_2024.pdf", 1024L),
            new FileApi("file-2", "report-final.docx", 2048L),
            new FileApi("file-3", "документ_2024.pdf", 512L)
        ));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectXssPayloadInFilename() throws Exception {
        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "<script>alert('xss')</script>.pdf", 1024L)
        ));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @Test
    void shouldRejectSqlInjectionAttemptInFilename() throws Exception {
        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "file;DROP TABLE submissions;--.pdf", 1024L)
        ));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @Test
    void shouldRejectMixedValidAndInvalidFilenames() throws Exception {
        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "valid_document.pdf", 1024L),
            new FileApi("file-2", "malicious\"onload=\"alert.pdf", 2048L)
        ));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @Test
    void shouldReturnNotFoundWhenSubmissionDoesNotExist() throws Exception {
        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "document.pdf", 1024L)
        ));
        when(submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileList))
            .thenThrow(new SubmissionNotFoundException("Submission not found"));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    void shouldReturnConflictWhenSubmissionIsInIncorrectState() throws Exception {
        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "document.pdf", 1024L)
        ));
        when(submissionService.updateSubmissionWithFileDetails(SUBMISSION_ID, fileList))
            .thenThrow(new SubmissionIncorrectStateException("Submission in incorrect state"));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isConflict())
            .andExpect(content().string(""));
    }
}
