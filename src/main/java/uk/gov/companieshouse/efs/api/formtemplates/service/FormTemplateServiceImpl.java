package uk.gov.companieshouse.efs.api.formtemplates.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.api.config.Config;
import uk.gov.companieshouse.efs.api.formtemplates.mapper.FormTemplateMapper;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;

/**
 * Stores and retrieves the form template information.
 */
@Service
@Import(Config.class)
public class FormTemplateServiceImpl implements FormTemplateService {

    private FormTemplateRepository repository;
    private FormTemplateMapper mapper;

    /**
     * FormTemplateService constructor.
     *
     * @param repository the {@link FormTemplateRepository}
     */
    @Autowired
    public FormTemplateServiceImpl(final FormTemplateRepository repository, final FormTemplateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public FormTemplateListApi getFormTemplates() {
        List<FormTemplate> formTemplates = repository.findAll();
        return mapper.map(formTemplates);
    }

    @Override
    public FormTemplateApi getFormTemplate(String id) {
        FormTemplate formTemplate = repository.findById(id).orElse(null);
        return formTemplate == null ? null : mapper.map(formTemplate);
    }

    @Override
    public FormTemplateListApi getFormTemplatesByCategory(final String id) {
        final List<FormTemplate> byCategory = repository.findByFormCategory(id);
        return mapper.map(byCategory);
    }
}
