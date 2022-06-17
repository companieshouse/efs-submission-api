package uk.gov.companieshouse.efs.api.formtemplates.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
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

    private FormTemplate testFormTemplate;

    @BeforeEach
    void setUp() {
        testFormTemplate = FormTemplate.builder()
            .withFormType("CC01")
            .withOrderIndex(1)
            .withFormName("Form01")
            .withFormCategory("CC")
            .withFee("100")
            .withAuthenticationRequired(true)
            .withFesEnabled(true)
            .withFesDocType("FES")
            .withSameDay(true)
            .withMessageTextIdList(Arrays.asList(1, 2, 3))
            .build();
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    void formTemplate() {

        assertThat(testFormTemplate.getFormType(), is("CC01"));
        assertThat(testFormTemplate.getOrderIndex(), is(1));
        assertThat(testFormTemplate.getFormName(), is("Form01"));
        assertThat(testFormTemplate.getFormCategory(), is("CC"));
        assertThat(testFormTemplate.getFee(), is("100"));
        assertThat(testFormTemplate.isAuthenticationRequired(), is(true));
        assertThat(testFormTemplate.isFesEnabled(), is(true));
        assertThat(testFormTemplate.isSameDay(), is(true));
        assertThat(testFormTemplate.getMessageTextIdList(), contains(1, 2, 3));
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
                "FormTemplate[formType=CC01,orderIndex=1,formName=Form01,formCategory=CC,fee=100,"
                    + "isAuthenticationRequired=true,isFesEnabled=true,fesDocType=FES," 
                    + "sameDay=true,messageTextIdList=[1, 2, 3]]"
                //@formatter:on
        ));
    }

}
