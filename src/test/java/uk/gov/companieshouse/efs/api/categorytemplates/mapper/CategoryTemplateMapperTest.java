package uk.gov.companieshouse.efs.api.categorytemplates.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTemplate;

class CategoryTemplateMapperTest {

    private CategoryTemplateMapper mapper;

    @BeforeEach
    void setUp(){
        this.mapper = new CategoryTemplateMapper();
    }

    @Test
    void testMapperMapsListOfCategoriesToCategoryListApi() {
        //given
        List<CategoryTemplate> categoryTemplates = Collections.singletonList(getCategory());

        //when
        CategoryTemplateListApi actual = mapper.map(categoryTemplates);

        //then
        assertEquals(expectedList(), actual);
    }

    @Test
    void testMapperMapsCategoryToCategoryTemplateApi() {
        //given
        CategoryTemplate categoryTemplate = getCategory();

        //when
        CategoryTemplateApi actual = mapper.map(categoryTemplate);

        //then
        assertEquals(expectedSingle(), actual);
    }

    private CategoryTemplate getCategory() {
        return new CategoryTemplate("MA", "INC", "New Incorporation", "", "");
    }

    private CategoryTemplateListApi expectedList() {
        CategoryTemplateApi element =
            new CategoryTemplateApi("MA", "INC", "New Incorporation", "", "");
        return new CategoryTemplateListApi(Collections.singletonList(element));
    }

    private CategoryTemplateApi expectedSingle() {
        CategoryTemplateApi element =
            new CategoryTemplateApi("MA", "INC", "New Incorporation", "", "");
        return new CategoryTemplateApi(element);
    }
}