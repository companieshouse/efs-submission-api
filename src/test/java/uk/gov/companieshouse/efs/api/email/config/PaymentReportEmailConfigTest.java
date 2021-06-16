package uk.gov.companieshouse.efs.api.email.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentReportEmailConfigTest {
    private PaymentReportEmailConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new PaymentReportEmailConfig();
    }

    @Test
    void setGetFinanceEmailAddress() {
        testConfig.setFinanceEmailAddress("finance");

        assertThat(testConfig.getFinanceEmailAddress(), is("finance"));
    }

    @Test
    void setGetScottishEmailAddress() {
        testConfig.setScottishEmailAddress("scottish");

        assertThat(testConfig.getScottishEmailAddress(), is("scottish"));
    }

    @Test
    void setGetSpecialCapitalEmailAddress() {
        testConfig.setSpecialCapitalEmailAddress("sh19");

        assertThat(testConfig.getSpecialCapitalEmailAddress(), is("sh19"));
    }

    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(PaymentReportEmailConfig.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }
}