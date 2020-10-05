package uk.gov.companieshouse.efs.api.formtemplates.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
class FormTemplateTest {

    private FormTemplate testFormTemplate;

    @BeforeEach
    void setUp() {
        testFormTemplate = new FormTemplate("CC01", "Form01", "CC", "100", false, false);
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    public void formTemplate() {

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
        assertThat(testFormTemplate.toString(), Matchers.is(
                //@formatter:off
                "FormTemplate[formType=CC01,formName=Form01,formCategory=CC,fee=100," +
                        "isAuthenticationRequired=false,isFesEnabled=false]"
                //@formatter:on
        ));
    }

}