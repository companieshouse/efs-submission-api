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
        String categoryId = "CC01";
        FormTemplate formTemplate = new FormTemplate("IN01", "New Incorporation", "NEWINC", "12",
                false, false, true, null);
        FormTemplateApi mappedForm = new FormTemplateApi("IN01", "New Incorporation", "NEWINC", "12",
                false, false, true,null);

        when(formRepository.findById(categoryId)).thenReturn(Optional.of(formTemplate));
        when(mapper.map(formTemplate)).thenReturn(mappedForm);

        //when
        FormTemplateApi actual = service.getFormTemplate(categoryId);

        //then
        assertEquals(mappedForm, actual);
    }

    @Test
    void testFetchEntityWhenFormTemplateApiObjectNotFound() {

        //given
        String categoryId = "CC01";

        when(formRepository.findById(categoryId)).thenReturn(Optional.empty());
        //when
        FormTemplateApi actual = service.getFormTemplate(categoryId);

        //then
        assertThat(actual, is(nullValue()));
        verifyNoInteractions(mapper);
    }

    @Test
    void testFetchEntityAndMapToFormTemplateListApiObjectByCategory() {

        //given
        String categoryId = "CC01";
        FormTemplate mappedForm = new FormTemplate("IN01", "New Incorporation", "NEWINC", "12",
                false, false, true, null);

        List<FormTemplate> listForm = new ArrayList<>();
        listForm.add(mappedForm);

        when(formRepository.findByFormCategory(categoryId)).thenReturn(listForm);
        when(mapper.map(anyList())).thenReturn(formList);

        //when
        FormTemplateListApi actual = service.getFormTemplatesByCategory(categoryId);

        //then
        assertEquals(formList, actual);
    }
}
