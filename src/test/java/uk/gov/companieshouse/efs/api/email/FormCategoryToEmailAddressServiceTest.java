package uk.gov.companieshouse.efs.api.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;

@ExtendWith(MockitoExtension.class)
class FormCategoryToEmailAddressServiceTest {

    protected static final String EMAIL_CC = "internal_CC_demo@ch.gov.uk";
    protected static final String EMAIL_RP = "internal_RP_demo@ch.gov.uk";
    protected static final String EMAIL_SCOT = "internal_SCOT_demo@ch.gov.uk";
    protected static final String EMAIL_NI = "internal_NI_demo@ch.gov.uk";
    protected static final String EMAIL_SP = "internal_SP_demo@ch.gov.uk";
    protected static final String EMAIL_INS = "internal_INS_demo@ch.gov.uk";
    protected static final String EMAIL_SH = "internal_SH_demo@ch.gov.uk";
    @Mock
    private FormTemplateRepository formTemplateRepository;
    @Mock
    private CategoryTemplateService categoryTemplateService;
    @Mock
    private FormTemplate formTemplate;

    private FormCategoryToEmailAddressService formCategoryToEmailAddressService;

    @BeforeEach
    void setUp() {
        this.formCategoryToEmailAddressService =
            new FormCategoryToEmailAddressService(formTemplateRepository, categoryTemplateService, EMAIL_CC, EMAIL_RP,
                EMAIL_SCOT, EMAIL_NI, EMAIL_SP, EMAIL_INS, EMAIL_SH);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForConstitutionForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("CC");
        when(formTemplate.getFormType()).thenReturn("CC01");
        when(categoryTemplateService.getTopLevelCategory("CC"))
            .thenReturn(CategoryTypeConstants.CHANGE_OF_CONSTITUTION);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("CC01");

        //then
        assertEquals(EMAIL_CC, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForSLPForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("SLP");
        when(formTemplate.getFormType()).thenReturn("SLPPSC01");
        when(categoryTemplateService.getTopLevelCategory("SLP"))
            .thenReturn(CategoryTypeConstants.SCOTTISH_LIMITED_PARTNERSHIP);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("SLPPSC01");

        //then
        assertEquals(EMAIL_SP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForSQPForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("SQP");
        when(formTemplate.getFormType()).thenReturn("SQP2");
        when(categoryTemplateService.getTopLevelCategory("SQP"))
            .thenReturn(CategoryTypeConstants.SCOTTISH_QUALIFYING_PARTNERSHIP);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("SQP2");

        //then
        assertEquals(EMAIL_SP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForNonConstitutionForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("RP");
        when(formTemplate.getFormType()).thenReturn("RP02A");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.OTHER);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("RP02A");

        //then
        assertEquals(EMAIL_RP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForRegPowersForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("RP");
        when(formTemplate.getFormType()).thenReturn("RP02A");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.OTHER);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "12345678");

        //then
        assertEquals(EMAIL_RP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForScotlandRegPowersForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("RP");
        when(formTemplate.getFormType()).thenReturn("RP02A");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.OTHER);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "SC123456");

        //then
        assertEquals(EMAIL_SCOT, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForNIRegPowersForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("RP");
        when(formTemplate.getFormType()).thenReturn("RP02A");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.OTHER);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "NI123456");

        //then
        assertEquals(EMAIL_NI, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForInsolvencyForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("CIGA2000");
        when(formTemplate.getFormType()).thenReturn("MT01");
        when(categoryTemplateService.getTopLevelCategory("CIGA2000")).thenReturn(CategoryTypeConstants.INSOLVENCY);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("MT01");

        //then
        assertEquals(EMAIL_INS, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForShareCapitalForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("SH");
        when(formTemplate.getFormType()).thenReturn("SH02");
        when(categoryTemplateService.getTopLevelCategory("SH"))
            .thenReturn(CategoryTypeConstants.SHARE_CAPITAL);

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("SH02");

        //then
        assertEquals(EMAIL_SH, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForNonConstitutionFormIfFormAbsent() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.emptyList());

        //when
        formCategoryToEmailAddressService.cacheFormTemplates();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("AD01");

        //then
        assertEquals(EMAIL_RP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsExceptionIfFormInvalid() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn(null);

        //when
        Executable actual = () -> formCategoryToEmailAddressService.cacheFormTemplates();

        //then
        Exception ex = assertThrows(Exception.class, actual);
    }
}