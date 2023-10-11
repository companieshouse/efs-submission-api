package uk.gov.companieshouse.efs.api.categorytemplates.controller;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.service.CategoryTemplateService;
import uk.gov.companieshouse.logging.Logger;

/**
 * Category template controller which handles category data.
 */
@RestController
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/efs-submission-api")
public class CategoryTemplateController {

    private CategoryTemplateService categoryService;
    private Logger logger;

    /**
     * Category template controller constructor.
     * @param categoryService service used to store the category template
     * @param logger the service logger
     */
    @Autowired
    public CategoryTemplateController(final CategoryTemplateService categoryService, final Logger logger) {
        this.categoryService = categoryService;
        this.logger = logger;
    }

    /**
     * Returns a responseEntity which contains a list of category types belonging to an optional
     * parent form category, or all categories if omitted.
     * Will return a status of not found if the list is not found.
     *
     * @return responseEntity
     */
    @GetMapping(value = "/category-templates", produces = {"application/json"})
    public ResponseEntity<CategoryTemplateListApi> getCategoryTemplates(
        @RequestParam(value = "parent", required = false) String categoryId, final HttpServletRequest request) {

        try {
            return categoryId == null
                ? ResponseEntity.ok(categoryService.getCategoryTemplates())
                : ResponseEntity.ok(categoryService.getCategoryTemplatesByCategory(categoryId));
        }
        catch (Exception ex) {
            Map<String, Object> debug = new HashMap<>();

            debug.put("categoryId", categoryId);
            logger.error("Failed to get category template", ex, debug);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns a responseEntity which contains a category template.
     * Will return a status of not found if the category template is not found.
     *
     * @return responseEntity
     */
    @GetMapping(value = "/category-template/{id}", produces = {"application/json"})
    public ResponseEntity<CategoryTemplateApi> getCategoryTemplate(@PathVariable String id, HttpServletRequest request) {
        try {
            return ResponseEntity.ok().body(categoryService.getCategoryTemplate(id));
        } catch (Exception ex) {
            Map<String, Object> debug = new HashMap<>();

            debug.put("id", id);
            logger.error("Failed to get category template", ex, debug);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/category-template/", produces = {"application/json"})
    public ResponseEntity<CategoryTemplateApi> getRootCategory(HttpServletRequest request) {
        final String id = "ROOT";
        return getCategoryTemplate(id, request);
    }
}
