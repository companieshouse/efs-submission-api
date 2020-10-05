package uk.gov.companieshouse.efs.api.submissions.model;

import java.util.Objects;

public class Presenter {

    private String email;

    public Presenter(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
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
        Presenter other = (Presenter) obj;
        return Objects.equals(email, other.email);
    }

}
