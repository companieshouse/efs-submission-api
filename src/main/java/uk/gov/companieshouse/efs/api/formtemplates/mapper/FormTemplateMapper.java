package uk.gov.companieshouse.efs.api.formtemplates.mapper;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.MessageTextListApi;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;

@Component
public class FormTemplateMapper {

    public FormTemplateListApi map(List<FormTemplate> formTemplates) {
        return formTemplates.stream()
                .map(form -> new FormTemplateApi(
                        form.getFormType(),
                        form.getFormName(),
                        form.getFormCategory(),
                        form.getFee(),
                        form.isAuthenticationRequired(),
                        form.isFesEnabled(),
                        form.getFesDocType(),
                        new MessageTextListApi(form.getMessageTextIdList())))
                            .collect(Collectors.toCollection(FormTemplateListApi::new));
    }

    public FormTemplateApi map(FormTemplate formTemplate) {
        return new FormTemplateApi(
                        formTemplate.getFormType(),
                        formTemplate.getFormName(),
                        formTemplate.getFormCategory(),
                        formTemplate.getFee(),
                        formTemplate.isAuthenticationRequired(),
                        formTemplate.isFesEnabled(),
                        formTemplate.getFesDocType(),
                        new MessageTextListApi(formTemplate.getMessageTextIdList()));
    }
}
