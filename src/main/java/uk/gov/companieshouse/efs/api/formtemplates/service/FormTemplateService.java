package uk.gov.companieshouse.efs.api.formtemplates.service;

import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;

/**
 * Stores and retrieves the form template information.
 */
public interface FormTemplateService {

    /**
     * Retrieve a submission form type by composite id.
     *
     * @param id form type id
     *
     * @return a submission form template
     */
    default FormTemplateApi getFormTemplateById(FormTemplate.FormTypeId id) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve all submission form types.
     *
     * @return list of submission form templates list
     */
    default FormTemplateListApi getFormTemplates() {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve a submission form type by form type only.
     * Assumes multiple results are equivalent, and returns the first one.
     *
     * @param formType form type
     *
     * @return a submission form template
     */
    default FormTemplateApi getFormTemplate(String formType) {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * Retrieve a list of submission forms belonging to a form category.
     *
     * @param id form category id
     * @return a submission form template list
     */
    default FormTemplateListApi getFormTemplatesByCategory(String id) {
        throw new UnsupportedOperationException("not implemented");
    }
}
