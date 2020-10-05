package uk.gov.companieshouse.efs.api.categorytemplates.service;

import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.OTHER;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.ROOT;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.mapper.CategoryTemplateMapper;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTemplate;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.repository.CategoryTemplateRepository;
import uk.gov.companieshouse.efs.api.config.Config;

/**
 * Stores and retrieves the category template information.
 */
@Service
@Import(Config.class)
public class CategoryTemplateServiceImpl implements CategoryTemplateService {

    private CategoryTemplateRepository repository;
    private CategoryTemplateMapper mapper;

    /**
     * FormTemplateService constructor.
     *
     * @param repository the {@link CategoryTemplateRepository}
     */
    @Autowired
    public CategoryTemplateServiceImpl(final CategoryTemplateRepository repository, final CategoryTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CategoryTemplateListApi getCategoryTemplates() {
        List<CategoryTemplate> categoryTemplates = repository.findAll();
        return mapper.map(categoryTemplates);
    }

    @Override
    public CategoryTemplateApi getCategoryTemplate(String id) {
        CategoryTemplate categoryTemplate = repository.findById(id).orElse(null);
        return categoryTemplate == null ? null : mapper.map(categoryTemplate);
    }

    @Override
    public CategoryTemplateListApi getCategoryTemplatesByCategory(final String id) {
        final List<CategoryTemplate> byParent = repository.findByParent(id);
        return mapper.map(byParent);
    }

    @Override
    public CategoryTypeConstants getTopLevelCategory(final String category) {
        CategoryTypeConstants result = CategoryTypeConstants.nameOf(category).orElse(OTHER);
        String currentCategory = category;

        while (true) {
            CategoryTemplate categoryTemplate = repository.findById(currentCategory).orElse(null);

            if (categoryTemplate != null) {
                final String parentCategory = categoryTemplate.getParent();
                final CategoryTypeConstants parentCategoryType =
                    CategoryTypeConstants.nameOf(parentCategory).orElse(OTHER);

                if (parentCategoryType == ROOT) {
                    return result;
                } else {
                    result = parentCategoryType;
                    currentCategory = parentCategory;
                }
            } else {
                return null;
            }
        }
    }

}
