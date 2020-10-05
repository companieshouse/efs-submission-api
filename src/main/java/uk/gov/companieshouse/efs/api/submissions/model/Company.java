package uk.gov.companieshouse.efs.api.submissions.model;

import java.util.Objects;

import org.springframework.data.mongodb.core.mapping.Field;

public class Company {

    @Field("company_number")
    private String companyNumber;
    @Field("company_name")
    private String companyName;

    public Company(String companyNumber, String companyName) {
        this.companyNumber = companyNumber;
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName, companyNumber);
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Company other = (Company) obj;
        return Objects.equals(companyName, other.companyName) && Objects.equals(companyNumber, other.companyNumber);
    }

}
