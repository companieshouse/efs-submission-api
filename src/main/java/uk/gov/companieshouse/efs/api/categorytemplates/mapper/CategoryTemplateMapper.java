package uk.gov.companieshouse.efs.api.categorytemplates.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTemplate;

/**
 * Mapper for converting CategoryTemplate model objects to API representations.
 * <p>
 * This class provides methods to map a list of {@link CategoryTemplate} objects to a {@link CategoryTemplateListApi},
 * and a single {@link CategoryTemplate} to a {@link CategoryTemplateApi}. It is used to bridge the internal data model
 * and the external API layer, ensuring that the data is transformed appropriately for external consumption.
 * </p>
 *
 * <ul>
 *   <li>{@link #map(List)}: Maps a list of CategoryTemplate objects to a CategoryTemplateListApi.</li>
 *   <li>{@link #map(CategoryTemplate)}: Maps a single CategoryTemplate object to a CategoryTemplateApi.</li>
 * </ul>
 */
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
                categoryTemplate.categoryType(),
                categoryTemplate.categoryName(),
                categoryTemplate.parent(),
                categoryTemplate.categoryHint(),
                categoryTemplate.guidanceTexts());
    }
}
