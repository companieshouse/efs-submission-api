package uk.gov.companieshouse.efs.api.categorytemplates.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@ExtendWith(SpringExtension.class)
class CategoryTemplateTest {

    private CategoryTemplate testCategoryTemplate;

    @BeforeEach
    void setUp() {
        testCategoryTemplate = new CategoryTemplate("CC01", 5, "Category01", "", "", null);
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    void categoryTemplate() {
        assertThat(testCategoryTemplate.categoryType(), is("CC01"));
        assertThat(testCategoryTemplate.orderIndex(), is(5));
        assertThat(testCategoryTemplate.categoryName(), is("Category01"));
        assertThat(testCategoryTemplate.parent(), is(""));
        assertThat(testCategoryTemplate.categoryHint(), is(""));
        assertThat(testCategoryTemplate.guidanceTexts(), is(Collections.emptyList()));
    }

    @Test
    void guidanceTextsIsNeverNull() {
        CategoryTemplate categoryTemplate = new CategoryTemplate("CC02", 10, "Category02", "", "", null);
        assertThat(categoryTemplate.guidanceTexts(), is(Collections.emptyList()));
    }

    @Test
    void recordDefaultBehavior() {
        CategoryTemplate a = new CategoryTemplate("CC01", 5, "Category01", "", "", null);
        CategoryTemplate b = new CategoryTemplate("CC01", 5, "Category01", "", "", null);
        CategoryTemplate c = new CategoryTemplate("CC02", 10, "Category02", "", "", null);

        // equals and hashCode
        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
        assertThat(a, Matchers.not(c));

        // toString
        assertThat(a.toString(), is(b.toString()));
        assertThat(a.toString(), Matchers.not(c.toString()));
    }
}

