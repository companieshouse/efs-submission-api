package uk.gov.companieshouse.efs.api.email;

import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.CHANGE_OF_CONSTITUTION;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.INSOLVENCY;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.OTHER;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.SCOTTISH_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.SCOTTISH_QUALIFYING_PARTNERSHIP;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.SHARE_CAPITAL;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.SHARE_CAPITAL_REDUCTION;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;
import uk.gov.companieshouse.efs.api.formtemplates.repository.FormTemplateRepository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FormCategoryToEmailAddressService {

    private FormTemplateRepository formTemplateRepository;
    private final CategoryTemplateService categoryTemplateService;

    @Value("${internal.constitution.email.address}")
    private String internalConstitutionEmailAddress;
    @Value("${internal.regfunc.email.address}")
    private String internalRegistryFunctionEmailAddress;
    @Value("${internal.scot.email.address}")
    private String internalScotEmailAddress;
    @Value("${internal.ni.email.address}")
    private String internalNIEmailAddress;
    @Value("${internal.scottishpartnerships.email.address}")
    private String internalScottishPartnershipsEmailAddress;
    @Value("${internal.insolvency.email.address}") 
    private String internalInsolvencyEmailAddress;
    @Value("${internal.sharecapital.email.address}")
    private String internalShareCapitalEmailAddress;
    @Value("${internal.sharecapitalreduction.email.address}")
    private String internalShareCapitalReductionEmailAddress;
    @Value("${scotland.company.prefixes}")
    private List<String> scotlandCompanyPrefixes;
    @Value("${northernIreland.company.prefixes}")
    private List<String> northernIrelandCompanyPrefixes;

    private Map<String, String> categoryTypeEmailMap;
    private static final Logger LOGGER = LoggerFactory.getLogger("efs-submission-api");

    private class EmailSupplier {
        private EnumMap<CategoryTypeConstants, String> categories;

        public EmailSupplier() {
            categories = new EnumMap<>(CategoryTypeConstants.class);

            categories.put(SCOTTISH_LIMITED_PARTNERSHIP, internalScottishPartnershipsEmailAddress);
            categories.put(SCOTTISH_QUALIFYING_PARTNERSHIP, internalScottishPartnershipsEmailAddress);
            categories.put(CHANGE_OF_CONSTITUTION, internalConstitutionEmailAddress);
            categories.put(INSOLVENCY, internalInsolvencyEmailAddress);
            categories.put(SHARE_CAPITAL, internalShareCapitalEmailAddress);
            categories.put(SHARE_CAPITAL_REDUCTION, internalShareCapitalReductionEmailAddress);
        }

        public String supplyEmail(CategoryTypeConstants categoryType) {
            return categories.getOrDefault(categoryType, internalRegistryFunctionEmailAddress);
        }

    }

    @Autowired
    public FormCategoryToEmailAddressService(FormTemplateRepository formTemplateRepository,
        CategoryTemplateService categoryTemplateService) {
        this.formTemplateRepository = formTemplateRepository;
        this.categoryTemplateService = categoryTemplateService;
    }

    @PostConstruct
    public void cacheEmailAddressByFormCategory() {
        final EmailSupplier emailSupplier = new EmailSupplier();

        try {
            this.categoryTypeEmailMap = formTemplateRepository.findAll()
                .stream()
                .collect(Collectors.toConcurrentMap(FormTemplate::getFormType, t -> {
                    final String formCategory = t.getFormCategory();

                    final CategoryTypeConstants formCategoryType =
                        CategoryTypeConstants.nameOf(formCategory).orElse(OTHER);

                    final CategoryTypeConstants categoryType =
                        formCategoryType != SHARE_CAPITAL_REDUCTION
                            ? categoryTemplateService.getTopLevelCategory(formCategory)
                            : SHARE_CAPITAL_REDUCTION;

                    return emailSupplier.supplyEmail(categoryType);
                }));
        }
        catch (Exception ex) {
            LOGGER.error("Invalid Form data exists in form_templates collection", ex);
            throw ex;
        }
    }

    public String getEmailAddressForFormCategory(String categoryType) {
        return this.categoryTypeEmailMap.getOrDefault(categoryType, internalRegistryFunctionEmailAddress);
    }

    public String getEmailAddressForRegPowersFormCategory(String formType, String companyNumber) {

        if (scotlandCompanyPrefixes.stream().anyMatch(companyNumber::startsWith)) {
            return internalScotEmailAddress;

        } else if (northernIrelandCompanyPrefixes.stream().anyMatch(companyNumber::startsWith)) {
            return internalNIEmailAddress;

        } else {
            return this.categoryTypeEmailMap.getOrDefault(formType, internalRegistryFunctionEmailAddress);
        }
    }

}


