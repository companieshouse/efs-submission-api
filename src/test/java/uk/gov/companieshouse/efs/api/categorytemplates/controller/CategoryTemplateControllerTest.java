package uk.gov.companieshouse.efs.api.categorytemplates.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
        ResponseEntity<CategoryTemplateListApi> actual = controller.getCategoryTemplates(null, request);

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
        ResponseEntity<CategoryTemplateListApi> actual = controller.getCategoryTemplates(categoryId, request);

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
        final ResponseEntity<CategoryTemplateListApi> actual = controller.getCategoryTemplates(categoryId, request);

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
        ResponseEntity<CategoryTemplateApi> actual = controller.getCategoryTemplate(categoryId, request);

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
        final ResponseEntity<CategoryTemplateApi> actual = controller.getCategoryTemplate(categoryId, request);

        //then
        assertEquals(status, actual.getStatusCode());
    }

    @Test
    void testGetRootCategoryTest() {
        controller.getRootCategory(request);

        verify(service).getCategoryTemplate("ROOT");
        verifyNoMoreInteractions(service);
    }
}