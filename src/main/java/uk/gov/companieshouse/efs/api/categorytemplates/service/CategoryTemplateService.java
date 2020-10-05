package uk.gov.companieshouse.efs.api.categorytemplates.service;

import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;

/**
 * Stores and retrieves the form category template information.
 */
public interface CategoryTemplateService {

    /**
     * Retrieve all submission form categories.
     *
     * @return list of category templates
     */
    default CategoryTemplateListApi getCategoryTemplates() {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve a submission form category.
     *
     * @param id form category id
     *
     * @return a category template
     */
    default CategoryTemplateApi getCategoryTemplate(String id) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve a list of form categories belonging to a form category.
     *
     * @param id form category id
     *
     * @return a form category
     */
    default CategoryTemplateListApi getCategoryTemplatesByCategory(String id) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Given a form category below the top level, work out its top level ancestor.
     *
     * @param category the form category
     * @return top level category or root if it already is a top level category or the root category
     */
    default CategoryTypeConstants getTopLevelCategory(String category) {
        throw new UnsupportedOperationException("not implemented");
    }

}
