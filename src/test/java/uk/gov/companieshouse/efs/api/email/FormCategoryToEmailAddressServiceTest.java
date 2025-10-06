package uk.gov.companieshouse.efs.api.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
    protected static final String EMAIL_SH_RED = "internal_SH-RED_demo@ch.gov.uk";
    private static final List<String> SCOTLAND_COMPANY_PREFIXES = Arrays.asList("SC","SL","SO","SG","SF");
    private static final List<String> NORTHERN_IRELAND_COMPANY_PREFIXES = Arrays.asList("NI","NC","R");
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
            new FormCategoryToEmailAddressService(formTemplateRepository, categoryTemplateService);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "scotlandCompanyPrefixes", SCOTLAND_COMPANY_PREFIXES);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "northernIrelandCompanyPrefixes", NORTHERN_IRELAND_COMPANY_PREFIXES);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalConstitutionEmailAddress", EMAIL_CC);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalRegistryFunctionEmailAddress", EMAIL_RP);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalScotEmailAddress", EMAIL_SCOT);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalNIEmailAddress", EMAIL_NI);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalScottishPartnershipsEmailAddress", EMAIL_SP);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalInsolvencyEmailAddress", EMAIL_INS);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalShareCapitalEmailAddress", EMAIL_SH);
        ReflectionTestUtils.setField(formCategoryToEmailAddressService, "internalShareCapitalReductionEmailAddress", EMAIL_SH_RED);
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
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
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
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
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
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("SQP2");

        //then
        assertEquals(EMAIL_SP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForNonConstitutionForm() {
        //given
        setUpRegPowersCategoryAndForm();

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("RP02A");

        //then
        assertEquals(EMAIL_RP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForRegPowersForm() {
        //given
        setUpRegPowersCategoryAndForm();

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "12345678");

        //then
        assertEquals(EMAIL_RP, actual);
    }

    @Test
    void testEmailAddressServiceReturnsScotlandEmailAddressForPrefixSC() {
        //given
        setUpRegPowersCategoryAndForm();

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "SC123456");

        //then
        assertEquals(EMAIL_SCOT, actual);
    }

    @Test
    void testEmailAddressServiceReturnsScotlandEmailAddressForPrefixSL() {
        //given
        setUpRegPowersCategoryAndForm();

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "SL123456");

        //then
        assertEquals(EMAIL_SCOT, actual);
    }

    @Test
    void testEmailAddressServiceReturnsNorthernIrelandEmailAddressForPrefixNI() {
        //given
        setUpRegPowersCategoryAndForm();

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "NI123456");

        //then
        assertEquals(EMAIL_NI, actual);
    }

    @Test
    void testEmailAddressServiceReturnsNorthernIrelandEmailAddressForPrefixR() {
        //given
        setUpRegPowersCategoryAndForm();

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForRegPowersFormCategory("RP02A", "R123456");

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
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
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
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("SH02");

        //then
        assertEquals(EMAIL_SH, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForShareCapitalReductionForm() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("SH-RED");
        when(formTemplate.getFormType()).thenReturn("SH19");

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
        String actual = formCategoryToEmailAddressService.getEmailAddressForFormCategory("SH19");

        //then
        assertEquals(EMAIL_SH_RED, actual);
    }

    @Test
    void testEmailAddressServiceReturnsEmailAddressForNonConstitutionFormIfFormAbsent() {
        //given
        when(formTemplateRepository.findAll()).thenReturn(Collections.emptyList());

        //when
        formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();
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
        Executable actual = () -> formCategoryToEmailAddressService.cacheEmailAddressByFormCategory();

        //then
        assertThrows(Exception.class, actual);
    }

    private void setUpRegPowersCategoryAndForm() {
        when(formTemplateRepository.findAll()).thenReturn(Collections.singletonList(formTemplate));
        when(formTemplate.getFormCategory()).thenReturn("RP");
        when(formTemplate.getFormType()).thenReturn("RP02A");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.OTHER);
    }
}