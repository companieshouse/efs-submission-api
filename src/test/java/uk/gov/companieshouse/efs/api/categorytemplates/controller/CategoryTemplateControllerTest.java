package uk.gov.companieshouse.efs.api.categorytemplates.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    void testGetAllFormsReturnsArrayOfForms() {

        //given
        CategoryTemplateListApi expected = new CategoryTemplateListApi();
        when(service.getCategoryTemplates()).thenReturn(expected);

        //when
        final var actual = controller.getCategoryTemplates(null);

        //then
        assertEquals(expected, actual.getBody());
        assertEquals(HttpStatus.OK,  actual.getStatusCode());
    }

    @Test
    void testGetCategoriesByCategoryReturnsArrayOfForms() {

        //given
        CategoryTemplateListApi expected = new CategoryTemplateListApi();
        final String categoryId = "category";
        when(service.getCategoryTemplatesByCategory(categoryId)).thenReturn(expected);

        //when
        final var actual = controller.getCategoryTemplates(categoryId);

        //then
        assertEquals(expected, actual.getBody());
        assertEquals(HttpStatus.OK,  actual.getStatusCode());
    }

    @Test
    void testGetCategoriesByCategoryExceptionThrown() {
        HttpStatusCode status = HttpStatusCode.valueOf(500);
        //given
        final String categoryId = "category";
        when(service.getCategoryTemplatesByCategory(categoryId)).thenThrow(new RuntimeException("Test exception scenario"));

        //when
        final var actual = controller.getCategoryTemplates(categoryId);

        //then
        assertEquals(status, actual.getStatusCode());
    }

    @Test
    void testGetCategoryTemplateByCategory() {

        //given
        CategoryTemplateApi expected = new CategoryTemplateApi();
        final String categoryId = "category";
        when(service.getCategoryTemplate(categoryId)).thenReturn(expected);

        //when
        final var actual = controller.getCategoryTemplate(categoryId);

        //then
        assertEquals(expected, actual.getBody());
        assertEquals(HttpStatus.OK,  actual.getStatusCode());
    }

    @Test
    void testGetCategoryTemplateByCategoryExceptionThrown() {
        HttpStatusCode status = HttpStatusCode.valueOf(500);
        //given
        final String categoryId = "category";
        when(service.getCategoryTemplate(categoryId)).thenThrow(new RuntimeException("Test exception scenario"));

        //when
        final var actual = controller.getCategoryTemplate(categoryId);

        //then
        assertEquals(status, actual.getStatusCode());
    }

    @Test
    void testGetRootCategoryTest() {
        controller.getRootCategory();

        verify(service).getCategoryTemplate("ROOT");
        verifyNoMoreInteractions(service);
    }
}