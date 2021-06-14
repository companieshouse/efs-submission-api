package uk.gov.companieshouse.efs.api.companyauthallowlist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.companyauthallowlist.repository.CompanyAuthAllowListRepository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAuthAllowListServiceImplTest {

    private static final String USER1_EMAIL_ADDRESS = "user1@ch.gov.uk";

    @Mock
    private CompanyAuthAllowListRepository repository;

    private CompanyAuthAllowListServiceImpl testService;

    @BeforeEach
    void setUp() {
        this.testService = new CompanyAuthAllowListServiceImpl(repository);
    }

    @Test
    void testIsOnAllowList() {

        when(testService.isOnAllowList(USER1_EMAIL_ADDRESS)).thenReturn(true);

        assertThat(testService.isOnAllowList(USER1_EMAIL_ADDRESS), is(true));
    }

}