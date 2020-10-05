package uk.gov.companieshouse.efs.api.formtemplates.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;

public class FormTemplateMapperTest {
    private FormTemplateMapper mapper;

    @BeforeEach
    void setUp(){
        this.mapper = new FormTemplateMapper();
    }

    @Test
    void testMapperMapsListOfFormsToFormListApi() {
        //given
        List<FormTemplate> formTemplates = Collections.singletonList(getForm());

        //when
        FormTemplateListApi actual = mapper.map(formTemplates);

        //then
        assertEquals(expectedList(), actual);
    }

    @Test
    void testMapperMapsCategoryToCategoryTemplateApi() {
        //given
        FormTemplate formTemplate = getForm();

        //when
        FormTemplateApi actual = mapper.map(formTemplate);

        //then
        assertEquals(expectedSingle(), actual);
    }

    private FormTemplate getForm() {
        return new FormTemplate("IN01", "New Incorporation", "NEWINC", "12",
                false, false);
    }

    private FormTemplateListApi expectedList() {
        FormTemplateApi element = new FormTemplateApi("IN01", "New Incorporation", "NEWINC", "12",
                false, false);
        return new FormTemplateListApi(Collections.singletonList(element));
    }

    private FormTemplateApi expectedSingle() {
        FormTemplateApi element = new FormTemplateApi("IN01", "New Incorporation", "NEWINC", "12",
                false, false);
        return new FormTemplateApi(element);
    }
}