package uk.gov.companieshouse.efs.api.formtemplates.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;

/**
 * Store and retrieve form template information.
 */
public interface FormTemplateRepository extends MongoRepository<FormTemplate, String> {

    /**
     * Finds all form template details by category.
     * Applies ordering by optional property 'orderIndex'.
     *
     * @return List&lt;FormTemplate&gt;
     */
    List<FormTemplate> findByFormCategoryOrderByOrderIndex(String category);

}

