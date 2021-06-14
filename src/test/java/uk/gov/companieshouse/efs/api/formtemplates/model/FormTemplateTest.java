package uk.gov.companieshouse.efs.api.formtemplates.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class FormTemplateTest {
    private static final FormTemplate.FormTypeKey FORM_TYPE_KEY = new FormTemplate.FormTypeKey("CC01", "CC");
    public static final List<Integer> MESSAGE_TEXT_ID_LIST = Collections.singletonList(1);
    
    private FormTemplate testFormTemplate;

    @BeforeEach
    void setUp() {
        testFormTemplate = new FormTemplate(FORM_TYPE_KEY, "Form01", "100", false, false, null);
        testFormTemplate.setMessageTextIdList(MESSAGE_TEXT_ID_LIST);
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    void formTemplate() {
        assertThat(testFormTemplate.getId(), is(FORM_TYPE_KEY));
        assertThat(testFormTemplate.getFormType(), is("CC01"));
        assertThat(testFormTemplate.getFormCategory(), is("CC"));
        assertThat(testFormTemplate.getFormName(), is("Form01"));
        assertThat(testFormTemplate.getFee(), is("100"));
        assertThat(testFormTemplate.getMessageTextIdList(), is(MESSAGE_TEXT_ID_LIST));
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
                "FormTemplate[formType=CC01,formCategory=CC,formName=Form01,fee=100," +
                        "isAuthenticationRequired=false,isFesEnabled=false,messageTextIdList=[1]]"
                //@formatter:on
        ));
    }

}