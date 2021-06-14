package uk.gov.companieshouse.efs.api.formtemplates.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;

class FormTemplateServiceTest {

    private class TestEFSServiceImpl implements FormTemplateService {

    }

    private TestEFSServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TestEFSServiceImpl();
    }

    @Test
    void getFormTemplates() {
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
                () -> testService.getFormTemplates());

        assertThat(thrown.getMessage(), is("not implemented"));
    }

    @Test
    void getFormTemplate() {
        final FormTemplate.FormTypeKey formTypeKey = new FormTemplate.FormTypeKey("RESOLUTIONS", "CC");
        
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
            () -> {
                testService.getFormTemplateById(formTypeKey);
            });

        assertThat(thrown.getMessage(), is("not implemented"));
    }

    @Test
    void getFormTemplateByFormType() {
        UnsupportedOperationException thrown =
            assertThrows(UnsupportedOperationException.class, () -> testService.getFormTemplate("CC01"));

        assertThat(thrown.getMessage(), is("not implemented"));
    }

    @Test
    void getFormTemplatesByCategory() {
        UnsupportedOperationException thrown =
            assertThrows(UnsupportedOperationException.class, () -> testService.getFormTemplatesByCategory("CC01"));

        assertThat(thrown.getMessage(), is("not implemented"));
    }

}