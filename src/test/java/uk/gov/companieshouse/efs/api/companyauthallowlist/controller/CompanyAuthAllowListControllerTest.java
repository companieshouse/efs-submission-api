package uk.gov.companieshouse.efs.api.companyauthallowlist.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.efs.api.companyauthallowlist.service.CompanyAuthAllowListServiceImpl;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class CompanyAuthAllowListControllerTest {

    private static final String USER1_EMAIL_ADDRESS = "user1@ch.gov.uk";

    private CompanyAuthAllowListController testController;
    
    @Mock
    private CompanyAuthAllowListServiceImpl testService;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        this.testController = new CompanyAuthAllowListController(testService, logger);
    }

    @Test
    void testWhenAllowListDoesNotContainEmailAddress() {

        when(testService.isOnAllowList(USER1_EMAIL_ADDRESS)).thenReturn(false);

        final ResponseEntity<Boolean> emailAddressOnAllowList = testController.getIsOnAllowList(
            USER1_EMAIL_ADDRESS);

        MatcherAssert.assertThat(emailAddressOnAllowList.getBody(), is(false));
    }

    @Test
    void testWhenAllowListDoesContainEmailAddress() {

        when(testService.isOnAllowList(USER1_EMAIL_ADDRESS)).thenReturn(true);

        final ResponseEntity<Boolean> emailAddressOnAllowList = testController.getIsOnAllowList(
            USER1_EMAIL_ADDRESS);

        MatcherAssert.assertThat(emailAddressOnAllowList.getBody(), is(true));
    }

    @Test
    void testWhenExceptionIsThrown() {
        HttpStatusCode status = HttpStatusCode.valueOf(500);

        when(testService.isOnAllowList(USER1_EMAIL_ADDRESS)).thenThrow(new RuntimeException("Test exception scenario"));

        final ResponseEntity<Boolean> emailAddressOnAllowList = testController.getIsOnAllowList(
            USER1_EMAIL_ADDRESS);

        Assertions.assertEquals(emailAddressOnAllowList.getStatusCode(), status);
    }
}