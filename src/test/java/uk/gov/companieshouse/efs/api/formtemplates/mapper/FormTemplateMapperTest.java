package uk.gov.companieshouse.efs.api.formtemplates.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.MessageTextListApi;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;

class FormTemplateMapperTest {
    private FormTemplateMapper mapper;

    @BeforeEach
    void setUp(){
        this.mapper = new FormTemplateMapper();
    }

    @Test
    void testMapperMapsListOfFormsToFormListApi() {
        //given
        List<FormTemplate> formTemplates =
            Collections.singletonList(getForm(Arrays.asList(1, 2, 3), false));

        //when
        FormTemplateListApi actual = mapper.map(formTemplates);

        //then
        assertEquals(expectedList(Arrays.asList(1, 2, 3), false), actual);
    }

    @Test
    void testMapperMapsCategoryToCategoryTemplateApi() {
        //given
        final List<Integer> messageTextIdList = Arrays.asList(1, 2, 3);
        FormTemplate formTemplate = getForm(messageTextIdList, false);

        //when
        FormTemplateApi actual = mapper.map(formTemplate);

        //then
        assertEquals(expectedSingle(messageTextIdList, false), actual);
    }

    @Test
    void testMapperMapsCategoryToCategoryTemplateApiWhenFormTemplateMessageTextListNull() {
        //given
        FormTemplate formTemplate = getForm(null, false);

        //when
        FormTemplateApi actual = mapper.map(formTemplate);

        //then
        assertEquals(expectedSingle(null, false), actual);
    }

    @Test
    void testMapperMapsCategoryToCategoryTemplateApiWhenFormTemplateMessageTextListEmpty() {
        //given
        FormTemplate formTemplate = getForm(Collections.emptyList(), false);

        //when
        FormTemplateApi actual = mapper.map(formTemplate);

        //then
        assertEquals(expectedSingle(Collections.singletonList(0), false), actual);
    }

    private FormTemplate getForm(final List<Integer> messageTextIdList, final boolean sameDay) {
        return FormTemplate.builder()
            .withFormType("IN01")
            .withFormName("New Incorporation")
            .withFormCategory("NEWINC")
            .withFee("12")
            .withSameDay(sameDay)
            .withMessageTextIdList(messageTextIdList).build();
    }

    private FormTemplateListApi expectedList(final List<Integer> messageTextIdList,
        final boolean sameDay) {
        FormTemplateApi element =
            new FormTemplateApi("IN01", "New Incorporation", "NEWINC", "12", false, false, null,
                sameDay, new MessageTextListApi(messageTextIdList));
        return new FormTemplateListApi(Collections.singletonList(element));
    }

    private FormTemplateApi expectedSingle(final List<Integer> messageTextIdList,
        final boolean sameDay) {
        FormTemplateApi element =
            new FormTemplateApi("IN01", "New Incorporation", "NEWINC", "12", false, false, null,
                sameDay, new MessageTextListApi(messageTextIdList));
        return new FormTemplateApi(element);
    }
}