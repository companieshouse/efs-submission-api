package uk.gov.companieshouse.efs.api.formtemplates.service;

import java.util.List;
import java.util.Optional;
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

    private final FormTemplateRepository repository;
    private final FormTemplateMapper mapper;

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
    public FormTemplateApi getFormTemplateById(final FormTemplate.FormTypeKey id) {
        return repository.findById(id).map(mapper::map).orElse(null);
    }

    @Override
    public FormTemplateListApi getFormTemplates() {
        List<FormTemplate> formTemplates = repository.findAll();
        return mapper.map(formTemplates);
    }

    @Override
    public FormTemplateApi getFormTemplate(String formType) {
        List<FormTemplate> formTemplates = repository.findByIdFormType(formType);
        return Optional.of(formTemplates).flatMap(list -> list.stream().findFirst())
            .map(mapper::map)
            .orElse(null);
    }

    @Override
    public FormTemplateListApi getFormTemplatesByCategory(final String id) {
        final List<FormTemplate> byCategory = repository.findByIdFormCategory(id);
        return mapper.map(byCategory);
    }
}
