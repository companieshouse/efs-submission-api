package uk.gov.companieshouse.efs.api.companyauthallowlist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.efs.api.companyauthallowlist.service.CompanyAuthAllowListService;
import uk.gov.companieshouse.logging.Logger;

/**
 * Company authentication allow list controller which handles incoming requests.
 */
@RestController
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/efs-submission-api")
public class CompanyAuthAllowListController {

    private final Logger logger;

    private CompanyAuthAllowListService service;

    /**
     * Company authentication allow list controller constructor.
     * @param service service used to query the allow list store
     * @param logger the logger
     */
    @Autowired
    public CompanyAuthAllowListController(final CompanyAuthAllowListService service, final Logger logger) {
        this.service = service;
        this.logger = logger;
    }

    /**
     * Returns a responseEntity which contains a Boolean value.
     * @return responseEntity containing the response.
     */
    @GetMapping(value = "/company-authentication/allow-list/{emailAddress}",
        produces = {"application/json"})
    public ResponseEntity<Boolean> getIsOnAllowList(@PathVariable String emailAddress) {
        try {
            return ResponseEntity.ok().body(service.isOnAllowList(emailAddress));
        } catch (Exception ex) {
            logger.error("Failure of company auth allow list check", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}