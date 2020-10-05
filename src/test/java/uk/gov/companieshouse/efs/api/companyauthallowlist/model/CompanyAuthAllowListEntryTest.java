package uk.gov.companieshouse.efs.api.companyauthallowlist.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
class CompanyAuthAllowListEntryTest {

    private CompanyAuthAllowListEntry testCompanyAuthAllowListEntry;

    @BeforeEach
    void setUp() {
        testCompanyAuthAllowListEntry = new CompanyAuthAllowListEntry("test@email.co.uk");
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    public void companyAuthAllowListEntity() {

        assertThat(testCompanyAuthAllowListEntry.getEmailAddress(), is("test@email.co.uk"));
    }

    @Test
    public void setEmailAddress() {

        testCompanyAuthAllowListEntry.setEmailAddress("test@email.co.uk");
        assertThat(testCompanyAuthAllowListEntry.getEmailAddress(), is("test@email.co.uk"));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CompanyAuthAllowListEntry
                .class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
        // EqualsVerifier does the asserts
    }

    @Test
    void toStringTest() {
        assertThat(testCompanyAuthAllowListEntry.toString(), Matchers.is(
                //@formatter:off
                "CompanyAuthAllowListEntry[emailAddress=test@email.co.uk]"
                //@formatter:on
        ));
    }
}