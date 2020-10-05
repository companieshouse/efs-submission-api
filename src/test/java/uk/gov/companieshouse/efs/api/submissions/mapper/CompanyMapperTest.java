package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.efs.api.submissions.model.Company;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompanyMapperTest {

    private CompanyMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new CompanyMapper();
    }

    @Test
    void testCompanyMapperMapsRequestEntityToDataEntity() {
        //given
        CompanyApi request = new CompanyApi("12345678", "ACME");

        //when
        Company actual = mapper.map(request);

        //then
        assertEquals(new Company("12345678", "ACME"), actual);
    }
}
