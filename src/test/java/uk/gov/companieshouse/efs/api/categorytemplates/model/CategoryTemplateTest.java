package uk.gov.companieshouse.efs.api.categorytemplates.model;

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
class CategoryTemplateTest {

    private CategoryTemplate testCategoryTemplate;

    @BeforeEach
    void setUp() {
        testCategoryTemplate = new CategoryTemplate("CC01", "Category01", "", "");
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    void categoryTemplate() {

        assertThat(testCategoryTemplate.getCategoryType(), is("CC01"));
        assertThat(testCategoryTemplate.getCategoryName(), is("Category01"));
        assertThat(testCategoryTemplate.getParent(), is(""));
        assertThat(testCategoryTemplate.getCategoryHint(), is(""));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CategoryTemplate.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
        // EqualsVerifier does the asserts
    }

    @Test
    void toStringTest() {
        assertThat(testCategoryTemplate.toString(), Matchers.is(
                //@formatter:off
                "CategoryTemplate[categoryType=CC01,categoryName=Category01,parent=,categoryHint=]"
                //@formatter:on
        ));
    }
}
