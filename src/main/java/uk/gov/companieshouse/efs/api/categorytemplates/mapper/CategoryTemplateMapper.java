package uk.gov.companieshouse.efs.api.categorytemplates.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTemplate;

@Component
public class CategoryTemplateMapper {

    /**
     * Creates List of CategoryTemplateApi from CategoryTemplate model.
     *
     * @param categoryTemplates the list of models
     * @return CategoryTemplateListApi
     */
    public CategoryTemplateListApi map(List<CategoryTemplate> categoryTemplates) {
        return categoryTemplates.stream()
                .map(this::map)
                .collect(Collectors.toCollection(CategoryTemplateListApi::new));
    }

    /**
     * Creates CategoryTemplateApi from CategoryTemplate model.
     *
     * @param categoryTemplate  the model
     * @return CategoryTemplateApi
     */
    public CategoryTemplateApi map(CategoryTemplate categoryTemplate) {
        return new CategoryTemplateApi(
                categoryTemplate.getCategoryType(),
                categoryTemplate.getCategoryName(),
                categoryTemplate.getParent(),
                categoryTemplate.getCategoryHint(),
                categoryTemplate.getGuidanceTexts());
    }
}
