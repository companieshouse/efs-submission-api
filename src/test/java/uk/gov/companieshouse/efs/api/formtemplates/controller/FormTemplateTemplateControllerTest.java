package uk.gov.companieshouse.efs.api.formtemplates.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class FormTemplateTemplateControllerTest {

    @Mock
    private FormTemplateService service;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;

    private FormTemplateController controller;

    @BeforeEach
    void setUp() {
        this.controller = new FormTemplateController(service, logger);
    }

    @Test
    void testGetAllFormsReturnsArrayOfForms() {
        //given
        FormTemplateListApi expected = new FormTemplateListApi();
        when(service.getFormTemplates()).thenReturn(expected);

        //when
        ResponseEntity<FormTemplateListApi> actual = controller.getFormTemplates(null, request);

        //then
        assertEquals(expected, actual.getBody());
        assertEquals(HttpStatus.OK,  actual.getStatusCode());
    }

    @Test
    void testGetFormsByCategoryReturnsArrayOfForms() {
        //given
        FormTemplateListApi expected = new FormTemplateListApi();
        final String categoryId = "category";
        when(service.getFormTemplatesByCategory(categoryId)).thenReturn(expected);

        //when
        ResponseEntity<FormTemplateListApi> actual = controller.getFormTemplates(categoryId, request);

        //then
        assertEquals(expected, actual.getBody());
        assertEquals(HttpStatus.OK,  actual.getStatusCode());
    }

    @Test
    void testGetFormsByCategoryExceptionThrown() {

        //given
        final String categoryId = "category";
        when(service.getFormTemplatesByCategory(categoryId)).thenThrow(new RuntimeException("Test exception scenario"));

        //when
        final ResponseEntity<FormTemplateListApi> actual = controller.getFormTemplates(categoryId, request);

        //then
        assertThat(actual.getStatusCodeValue(), is(500));
    }

    @Test
    void testGetFormsTemplate() {
        //given
        FormTemplateApi expected = new FormTemplateApi();
        final String formType = "form01";
        when(service.getFormTemplate(formType)).thenReturn(expected);

        //when
        ResponseEntity<FormTemplateApi> actual = controller.getFormTemplate(formType, request);

        //then
        assertEquals(expected, actual.getBody());
        assertEquals(HttpStatus.OK,  actual.getStatusCode());
    }

    @Test
    void testGetFormTemplateExceptionThrown() {

        //given
        final String formType = "form01";
        when(service.getFormTemplate(formType)).thenThrow(new RuntimeException("Test exception scenario"));

        //when
        final ResponseEntity<FormTemplateApi> actual = controller.getFormTemplate(formType, request);

        //then
        assertThat(actual.getStatusCodeValue(), is(500));
    }
}
