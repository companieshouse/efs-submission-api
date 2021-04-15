package uk.gov.companieshouse.efs.api.formtemplates.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class FormTemplateTest {

    private FormTemplate testFormTemplate;

    @BeforeEach
    void setUp() {
        testFormTemplate = new FormTemplate("CC01", "Form01", "CC", "100", false, false, true, null);
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    void formTemplate() {

        assertThat(testFormTemplate.getFormType(), is("CC01"));
        assertThat(testFormTemplate.getFormName(), is("Form01"));
        assertThat(testFormTemplate.getFormCategory(), is("CC"));
        assertThat(testFormTemplate.getFee(), is("100"));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(FormTemplate.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
        // EqualsVerifier does the asserts
    }

    @Test
    void toStringTest() {
        assertThat(testFormTemplate.toString(), containsString("FormTemplate["));
        assertThat(testFormTemplate.toString(), containsString("formType=CC01"));
        assertThat(testFormTemplate.toString(), containsString("formName=Form01"));
        assertThat(testFormTemplate.toString(), containsString("formCategory=CC"));
        assertThat(testFormTemplate.toString(), containsString("fee=100"));
        assertThat(testFormTemplate.toString(), containsString("isAuthenticationRequired=false"));
        assertThat(testFormTemplate.toString(), containsString("isFesEnabled=false"));
        assertThat(testFormTemplate.toString(), containsString("messageTextIdList=<null>"));
    }

}