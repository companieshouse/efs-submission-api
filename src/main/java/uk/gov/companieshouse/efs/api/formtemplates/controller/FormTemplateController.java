package uk.gov.companieshouse.efs.api.formtemplates.controller;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.api.formtemplates.service.FormTemplateService;
import uk.gov.companieshouse.logging.Logger;

/**
 * Form template controller which handles form data.
 */
@RestController
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/efs-submission-api")
public class FormTemplateController {

    private FormTemplateService formService;
    private Logger logger;

    /**
     * Form template controller constructor.
     * @param formService service used to store the form template
     */
    @Autowired
    public FormTemplateController(final FormTemplateService formService, final Logger logger) {
        this.formService = formService;
        this.logger = logger;
    }

    /**
     * Returns a responseEntity which contains a list of form templates belonging to an optional form category, or
     * all templates if category is omitted.
     * Will return a status of not found if the category is not found.
     *
     * @return responseEntity
     */
    @GetMapping(value = "/form-templates", produces = {"application/json"})
    public ResponseEntity<FormTemplateListApi> getFormTemplates(
        @RequestParam(value = "category", required = false) String categoryId, HttpServletRequest request) {

        try {
            return categoryId == null
                ? ResponseEntity.ok(formService.getFormTemplates())
                : ResponseEntity.ok(formService.getFormTemplatesByCategory(categoryId));
        }
        catch (Exception ex) {
            Map<String, Object> debug = new HashMap<>();

            debug.put("categoryId", categoryId);
            logger.error("Failed to get form templates", ex, debug);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns a responseEntity which contains a form template.
     * Will return a status of not found if the form template is not found.
     *
     * @return responseEntity
     */
    @GetMapping(value = "/form-template", produces = {"application/json"})
    public ResponseEntity<FormTemplateApi> getFormTemplate(@RequestParam("type") String id,
        HttpServletRequest request) {

        try {
            return ResponseEntity.ok().body(formService.getFormTemplate(id));
        } catch (Exception ex) {
            Map<String, Object> debug = new HashMap<>();

            debug.put("id", id);
            logger.error("Failed to get form template", ex, debug);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
