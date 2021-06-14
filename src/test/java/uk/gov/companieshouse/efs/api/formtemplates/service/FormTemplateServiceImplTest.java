package uk.gov.companieshouse.efs.api.formtemplates.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.api.formtemplates.mapper.FormTemplateMapper;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;

@ExtendWith(MockitoExtension.class)
class FormTemplateServiceImplTest {
    @Mock
    private FormTemplateRepository formRepository;

    @Mock
    private FormTemplateMapper mapper;

    @Mock
    private FormTemplateListApi formList;

    @Mock
    private FormTemplate formTemplate;

    private FormTemplateServiceImpl service;
    public static final String FORM_TYPE = "IN01";
    public static final String CATEGORY_TYPE = "NEWINC";
    public static final FormTemplate.FormTypeId FORM_TYPE_KEY = new FormTemplate.FormTypeId(FORM_TYPE, CATEGORY_TYPE);
    public static final FormTemplateApi FORM_TEMPLATE =
        new FormTemplateApi(FORM_TYPE, "New Incorporation", CATEGORY_TYPE, "12", false, false, null);
    public static final FormTemplate MAPPED_FORM =
        new FormTemplate(FORM_TYPE_KEY, "New Incorporation", "12", false, false, null);

    @BeforeEach
    void setUp() {
        this.service = new FormTemplateServiceImpl((FormTemplateRepository) formRepository, mapper);
    }

    @Test
    void testFetchEntityAndMapToFormListApiObject() {
        //given
        when(formRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(mapper.map(anyList())).thenReturn(formList);

        //when
        FormTemplateListApi actual = service.getFormTemplates();

        //then
        assertEquals(formList, actual);
    }

    @Test
    void testFetchEntityWhenFormListApiObjectNotFound() {

        //when
        when(formRepository.findAll()).thenReturn(Collections.emptyList());
        FormTemplateListApi actual = service.getFormTemplates();

        //then
        assertThat(actual, is(nullValue()));
    }

    @Test
    void testFetchEntityAndMapToFormTemplateApiObject() {

        //given
        final FormTemplate.FormTypeId formTypeId = new FormTemplate.FormTypeId(FORM_TYPE, CATEGORY_TYPE);
        FormTemplate formTemplate = new FormTemplate(formTypeId, "New Incorporation", "12", false, false, null);
        FormTemplateApi mappedForm =
            new FormTemplateApi(formTypeId.getFormType(), "New Incorporation", formTypeId.getFormCategory(), "12",
                false, false, null);

        when(formRepository.findById(FORM_TYPE_KEY)).thenReturn(Optional.of(formTemplate));
        when(mapper.map(formTemplate)).thenReturn(mappedForm);

        //when
        FormTemplateApi actual = service.getFormTemplateById(FORM_TYPE_KEY);

        //then
        assertEquals(mappedForm, actual);
    }

    @Test
    void testFetchEntityWhenFormTemplateApiObjectNotFound() {

        //given
        when(formRepository.findById(FORM_TYPE_KEY)).thenReturn(Optional.empty());
        //when
        FormTemplateApi actual = service.getFormTemplateById(FORM_TYPE_KEY);

        //then
        assertThat(actual, is(nullValue()));
        verifyNoInteractions(mapper);
    }

    @Test
    void testFetchEntityAndMapToFormTemplateListApiObjectByCategory() {

        //given
        String categoryId = "CC01";

        List<FormTemplate> listForm = new ArrayList<>();
        listForm.add(MAPPED_FORM);

        when(formRepository.findByIdFormCategory(categoryId)).thenReturn(listForm);
        when(mapper.map(anyList())).thenReturn(formList);

        //when
        FormTemplateListApi actual = service.getFormTemplatesByCategory(categoryId);

        //then
        assertEquals(formList, actual);
    }

    @Test
    void fetchEntityAndMapToFormTemplateApiObjectByFormType() {
        List<FormTemplate> listForm = new ArrayList<>();
        listForm.add(MAPPED_FORM);

        //given
        when(formRepository.findByIdFormType(FORM_TYPE)).thenReturn(listForm);
        when(mapper.map(listForm.get(0))).thenReturn(FORM_TEMPLATE);

        //when
        FormTemplateApi actual = service.getFormTemplate(FORM_TYPE);

        //then
        assertThat(actual, is(FORM_TEMPLATE));
    }

    @Test
    void fetchEntityAndMapToFormTemplateApiObjectNotFound() {
        List<FormTemplate> listForm = new ArrayList<>();

        //given
        when(formRepository.findByIdFormType(FORM_TYPE)).thenReturn(listForm);

        //when
        FormTemplateApi actual = service.getFormTemplate(FORM_TYPE);

        //then
        assertThat(actual, is(nullValue()));
        verifyNoInteractions(mapper);
    }

}
