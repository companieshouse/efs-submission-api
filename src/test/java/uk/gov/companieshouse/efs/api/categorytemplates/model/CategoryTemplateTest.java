package uk.gov.companieshouse.efs.api.categorytemplates.model;

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
class CategoryTemplateTest {

    private CategoryTemplate testCategoryTemplate;

    @BeforeEach
    void setUp() {
        testCategoryTemplate = new CategoryTemplate("CC01", "FILE", "Category01", "parent", "hint");
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    void categoryTemplate() {

        assertThat(testCategoryTemplate.getCategoryType(), is("CC01"));
        assertThat(testCategoryTemplate.getCategoryFamily(), is("FILE"));
        assertThat(testCategoryTemplate.getCategoryName(), is("Category01"));
        assertThat(testCategoryTemplate.getParent(), is("parent"));
        assertThat(testCategoryTemplate.getCategoryHint(), is("hint"));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CategoryTemplate.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
        // EqualsVerifier does the asserts
    }

    @Test
    void toStringTest() {
        assertThat(testCategoryTemplate.toString(), containsString("categoryType=CC01"));
        assertThat(testCategoryTemplate.toString(), containsString("categoryFamily=FILE"));
        assertThat(testCategoryTemplate.toString(), containsString("parent=parent"));
        assertThat(testCategoryTemplate.toString(), containsString("categoryHint=hint"));
    }
}
