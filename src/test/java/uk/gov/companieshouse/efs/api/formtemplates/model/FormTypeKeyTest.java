package uk.gov.companieshouse.efs.api.formtemplates.model;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isA;

import java.io.Serializable;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormTypeKeyTest {
    private FormTemplate.FormTypeKey testKey;

    @BeforeEach
    void setUp() {
        testKey = new FormTemplate.FormTypeKey("form", "category");
    }
    
    @Test
    void isSerializable() {
        assertThat(new FormTemplate.FormTypeKey(), isA(Serializable.class));    
    }
    
    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(FormTemplate.FormTypeKey.class).usingGetClass().verify();
    }
    
    @Test
    void getFormType() {
        assertThat(testKey.getFormType(), is("form"));
    }

    @Test
    void getFormCategory() {
        assertThat(testKey.getFormCategory(), is("category"));
    }

    @Test
    void testToString() {
        assertThat(testKey.toString(), containsString("formType=form"));
        assertThat(testKey.toString(), containsString("formCategory=category"));
    }
}