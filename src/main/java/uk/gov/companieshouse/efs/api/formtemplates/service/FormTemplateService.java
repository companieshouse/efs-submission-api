package uk.gov.companieshouse.efs.api.formtemplates.service;

import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;

/**
 * Stores and retrieves the form template information.
 */
public interface FormTemplateService {

    /**
     * Retrieve all submission form types.
     *
     * @return list of submission form templates
     */
    default FormTemplateListApi getFormTemplates() {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve a submission form type.
     *
     * @param id form type id
     *
     * @return a submission form template
     */
    default FormTemplateApi getFormTemplate(String id) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve a list of submission forms belonging to a form category.
     *
     * @param id form category id
     * @return a submission form template
     */
    default FormTemplateListApi getFormTemplatesByCategory(String id) {
        throw new UnsupportedOperationException("not implemented");
    }
}
