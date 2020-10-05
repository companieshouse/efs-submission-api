package uk.gov.companieshouse.efs.api.email;

import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.CHANGE_OF_CONSTITUTION;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.INSOLVENCY;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.SCOTTISH_LIMITED_PARTNERSHIP;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.SCOTTISH_QUALIFYING_PARTNERSHIP;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.SHARE_CAPITAL;

import java.util.EnumMap;
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

@Component
public class FormCategoryToEmailAddressService {

    private FormTemplateRepository formTemplateRepository;
    private final CategoryTemplateService categoryTemplateService;

    private String internalConstitutionEmailAddress;
    private String internalRegistryFunctionEmailAddress;
    private String internalScottishPartnershipsEmailAddress;
    private String internalInsolvencyEmailAddress;
    private String internalSharedCapitalEmailAddress;
    private Map<String, String> formTypeEmailMap;

    private class EmailSupplier {
        private EnumMap<CategoryTypeConstants, String> categories;

        public EmailSupplier() {
            categories = new EnumMap<>(CategoryTypeConstants.class);

            categories.put(SCOTTISH_LIMITED_PARTNERSHIP, internalScottishPartnershipsEmailAddress);
            categories.put(SCOTTISH_QUALIFYING_PARTNERSHIP, internalScottishPartnershipsEmailAddress);
            categories.put(CHANGE_OF_CONSTITUTION, internalConstitutionEmailAddress);
            categories.put(INSOLVENCY, internalInsolvencyEmailAddress);
            categories.put(SHARE_CAPITAL, internalSharedCapitalEmailAddress);
        }

        public String supplyEmail(CategoryTypeConstants categoryType) {
            return categories.getOrDefault(categoryType, internalRegistryFunctionEmailAddress);
        }

    }

    @Autowired
    public FormCategoryToEmailAddressService(FormTemplateRepository formTemplateRepository,
        CategoryTemplateService categoryTemplateService,
        @Value("${internal.constitution.email.address}") String internalConstitutionEmailAddress,
        @Value("${internal.regfunc.email.address}") String internalRegistryFunctionEmailAddress,
        @Value("${internal.scottishpartnerships.email.address}") String internalScottishPartnershipsEmailAddress,
        @Value("${internal.insolvency.email.address}") String internalInsolvencyEmailAddress,
        @Value("${internal.sharecapital.email.address}") String internalSharedCapitalEmailAddress) {
        this.formTemplateRepository = formTemplateRepository;
        this.categoryTemplateService = categoryTemplateService;
        this.internalConstitutionEmailAddress = internalConstitutionEmailAddress;
        this.internalRegistryFunctionEmailAddress = internalRegistryFunctionEmailAddress;
        this.internalScottishPartnershipsEmailAddress = internalScottishPartnershipsEmailAddress;
        this.internalInsolvencyEmailAddress = internalInsolvencyEmailAddress;
        this.internalSharedCapitalEmailAddress = internalSharedCapitalEmailAddress;
    }

    @PostConstruct
    public void cacheFormTemplates() {
        final EmailSupplier emailSupplier = new EmailSupplier();

        this.formTypeEmailMap = formTemplateRepository.findAll().stream().collect(Collectors
            .toConcurrentMap(FormTemplate::getFormType, formTemplate -> emailSupplier
                .supplyEmail(categoryTemplateService.getTopLevelCategory(formTemplate.getFormCategory()))));
    }

    public String getEmailAddressForFormCategory(String formType) {
        return this.formTypeEmailMap.getOrDefault(formType, internalRegistryFunctionEmailAddress);
    }

}


