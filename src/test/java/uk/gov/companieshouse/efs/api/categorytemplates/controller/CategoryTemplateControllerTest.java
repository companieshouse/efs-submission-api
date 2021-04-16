package uk.gov.companieshouse.efs.api.categorytemplates.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class CategoryTemplateControllerTest {

    @Mock
    private CategoryTemplateService service;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;

    private CategoryTemplateController controller;

    @BeforeEach
    void setUp() {
        this.controller = new CategoryTemplateController(service, logger);
    }

    @Test
    void testGetAllFormsReturnsList() {

        //given
        CategoryTemplateListApi expected = new CategoryTemplateListApi();
        when(service.getCategoryTemplates()).thenReturn(expected);

        //when
        ResponseEntity<CategoryTemplateListApi> actual = controller.getCategoryTemplates(null, null, request);

        //then
        assertThat(actual.getBody(), is(expected));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void testGetCategoriesByCategoryReturnsList() {

        //given
        CategoryTemplateListApi expected = new CategoryTemplateListApi();
        final String categoryId = "category";
        when(service.getCategoryTemplatesByCategory(categoryId)).thenReturn(expected);

        //when
        ResponseEntity<CategoryTemplateListApi> actual = controller.getCategoryTemplates(null, categoryId, request);

        //then
        assertThat(actual.getBody(), is(expected));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void testGetCategoryTemplatesByFamilyReturnsList() {

        //given
        CategoryTemplateListApi expected = new CategoryTemplateListApi();
        final String id = "family";
        when(service.getCategoryTemplatesByFamily(id)).thenReturn(expected);

        //when
        ResponseEntity<CategoryTemplateListApi> actual =
            controller.getCategoryTemplates(id, null, request);

        //then
        assertThat(actual.getBody(), is(expected));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void testGetFamilyCategoryTemplatesExceptionThrown() {

        //given
        final String id = "family";
        when(service.getCategoryTemplatesByFamily(id)).thenThrow(new RuntimeException("Test exception scenario"));

        //when
        final ResponseEntity<CategoryTemplateListApi> actual =
            controller.getCategoryTemplates(id, null, request);

        //then
        assertThat(actual.getStatusCodeValue(), is(500));
    }

    @Test
    void testGetCategoriesByCategoryExceptionThrown() {

        //given
        final String categoryId = "category";
        when(service.getCategoryTemplatesByCategory(categoryId)).thenThrow(new RuntimeException("Test exception scenario"));

        //when
        final ResponseEntity<CategoryTemplateListApi> actual = controller.getCategoryTemplates(null, categoryId, request);

        //then
        assertThat(actual.getStatusCodeValue(), is(500));
    }

    @Test
    void testGetCategoryTemplateByCategory() {

        //given
        CategoryTemplateApi expected = new CategoryTemplateApi();
        final String categoryId = "category";
        when(service.getCategoryTemplate(categoryId)).thenReturn(expected);

        //when
        ResponseEntity<CategoryTemplateApi> actual = controller.getCategoryTemplate(categoryId, request);

        //then
        assertThat(actual.getBody(), is(expected));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void testGetCategoryTemplateByCategoryExceptionThrown() {

        //given
        final String categoryId = "category";
        when(service.getCategoryTemplate(categoryId)).thenThrow(new RuntimeException("Test exception scenario"));

        //when
        final ResponseEntity<CategoryTemplateApi> actual = controller.getCategoryTemplate(categoryId, request);

        //then
        assertThat(actual.getStatusCodeValue(), is(500));
    }
}