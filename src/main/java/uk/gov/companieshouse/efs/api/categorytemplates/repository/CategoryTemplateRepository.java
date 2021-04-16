package uk.gov.companieshouse.efs.api.categorytemplates.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTemplate;

/**
 * Store and retrieve category template information.
 */
public interface CategoryTemplateRepository extends MongoRepository<CategoryTemplate, String> {

    /**
     * Finds all form categories by category family.
     *
     * @return List&lt;CategoryTemplate&gt;
     */
    List<CategoryTemplate> findByCategoryFamily(String family);
    
    /**
     * Finds all form categories by parent category.
     *
     * @return List&lt;CategoryTemplate&gt;
     */
    List<CategoryTemplate> findByParent(String category);
}               
