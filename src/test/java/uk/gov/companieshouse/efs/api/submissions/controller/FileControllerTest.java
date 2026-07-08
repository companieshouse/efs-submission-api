package uk.gov.companieshouse.efs.api.submissions.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Validation;
import java.util.List;
import java.util.stream.IntStream;
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
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.efs.api.submissions.service.SubmissionService;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionIncorrectStateException;
import uk.gov.companieshouse.efs.api.submissions.service.exception.SubmissionNotFoundException;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private static final String SUBMISSION_ID = "SUB-001";
    private static final String ENDPOINT = "/efs-submission-api/submission/{id}/files";
    private static final String FORM_TYPE = "CC01";

    @Mock
    private SubmissionService submissionService;

    @Mock
    private FormTemplateService formTemplateService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        final var fileController = new FileController(submissionService, formTemplateService);
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
        final var submissionApi = createSubmissionWithFormType(FORM_TYPE);
        final var formTemplate = createFormTemplate(false);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplate);
        when(submissionService.updateSubmissionWithFileDetails(any(), any()))
            .thenReturn(expectedResponse);

        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "invoice_2024.pdf", 1024L),
            new FileApi("file-2", "report-final.docx", 2048L)
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
        final var submissionApi = createSubmissionWithFormType(FORM_TYPE);
        final var formTemplate = createFormTemplate(false);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplate);
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
        final var submissionApi = createSubmissionWithFormType(FORM_TYPE);
        final var formTemplate = createFormTemplate(false);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplate);
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

    @Test
    void shouldReturnBadRequestWhenFesEnabledFormExceedsMaxOneFile() throws Exception {
        final var submissionApi = createSubmissionWithFormType(FORM_TYPE);
        final var formTemplate = createFormTemplate(true);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplate);

        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "document1.pdf", 1024L),
            new FileApi("file-2", "document2.pdf", 2048L)
        ));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @Test
    void shouldReturnOkWhenFesEnabledFormHasExactlyOneFile() throws Exception {
        final var expectedResponse = new SubmissionResponseApi(SUBMISSION_ID);
        final var submissionApi = createSubmissionWithFormType(FORM_TYPE);
        final var formTemplate = createFormTemplate(true);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplate);
        when(submissionService.updateSubmissionWithFileDetails(any(), any()))
            .thenReturn(expectedResponse);

        final var fileList = new FileListApi(List.of(
            new FileApi("file-1", "document.pdf", 1024L)
        ));

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenNonFesFormExceedsMaxTenFiles() throws Exception {
        final var submissionApi = createSubmissionWithFormType(FORM_TYPE);
        final var formTemplate = createFormTemplate(false);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplate);

        final var files = IntStream.rangeClosed(1, 11)
            .mapToObj(i -> new FileApi("file-" + i, "document" + i + ".pdf", 1024L))
            .toList();
        final var fileList = new FileListApi(files);

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    @Test
    void shouldReturnOkWhenNonFesFormHasExactlyTenFiles() throws Exception {
        final var expectedResponse = new SubmissionResponseApi(SUBMISSION_ID);
        final var submissionApi = createSubmissionWithFormType(FORM_TYPE);
        final var formTemplate = createFormTemplate(false);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplate);
        when(submissionService.updateSubmissionWithFileDetails(any(), any()))
            .thenReturn(expectedResponse);

        final var files = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> new FileApi("file-" + i, "document" + i + ".pdf", 1024L))
            .toList();
        final var fileList = new FileListApi(files);

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenSubmissionHasNoFormDetails() throws Exception {
        final var submissionApi = new SubmissionApi(SUBMISSION_ID, null, null, null, null, null,
            null, null, null, null, null, null, null);
        when(submissionService.readSubmission(SUBMISSION_ID)).thenReturn(submissionApi);

        final var files = IntStream.rangeClosed(1, 11)
            .mapToObj(i -> new FileApi("file-" + i, "document" + i + ".pdf", 1024L))
            .toList();
        final var fileList = new FileListApi(files);

        mockMvc.perform(put(ENDPOINT, SUBMISSION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fileList)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }

    private SubmissionApi createSubmissionWithFormType(final String formType) {
        final var submissionForm = new SubmissionFormApi();
        submissionForm.setFormType(formType);
        return new SubmissionApi(SUBMISSION_ID, null, null, null, null, null, null, null,
            submissionForm, null, null, null, null);
    }

    private FormTemplateApi createFormTemplate(final boolean fesEnabled) {
        final var formTemplate = new FormTemplateApi();
        formTemplate.setFesEnabled(fesEnabled);
        return formTemplate;
    }
}
