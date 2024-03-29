package uk.gov.companieshouse.efs.api.categorytemplates.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        final CategoryTemplate category =
                new CategoryTemplate("MA", 1, "New Incorporation", "", "", null);

        category.setGuidanceTexts(Collections.singletonList(3000));

        return category;
    }

    private CategoryTemplateListApi expectedList() {
        CategoryTemplateApi element = new CategoryTemplateApi("MA", "New Incorporation", "", "",
                Collections.singletonList(3000));
        return new CategoryTemplateListApi(Collections.singletonList(element));
    }

    private CategoryTemplateApi expectedSingle() {
        CategoryTemplateApi element = new CategoryTemplateApi("MA", "New Incorporation", "", "",
                Collections.singletonList(3000));
        return new CategoryTemplateApi(element);
    }
}
