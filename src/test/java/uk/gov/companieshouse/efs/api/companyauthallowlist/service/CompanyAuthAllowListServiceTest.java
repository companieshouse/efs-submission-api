package uk.gov.companieshouse.efs.api.companyauthallowlist.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CompanyAuthAllowListServiceTest {

    private class TestEFSServiceImpl implements CompanyAuthAllowListService {

    }

    private TestEFSServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TestEFSServiceImpl();
    }

    @Test
    void isOnAllowList() {
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
                () -> testService.isOnAllowList("test@email.co.uk"));

        assertThat(thrown.getMessage(), is("not implemented"));
    }
}