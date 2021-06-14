package uk.gov.companieshouse.efs.api.formtemplates.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Builder class for the {@code FormTemplate}.
 */
@Document(collection = "form_templates")
public class FormTemplate implements Serializable {

    @EmbeddedId
    private FormTypeKey id;
 
    @JsonProperty("form_name")
    @Field
    private String formName;


    @JsonProperty("fee")
    @Field
    private String fee;

    @JsonProperty("requires_authentication")
    @Field
    private boolean isAuthenticationRequired;

    @JsonProperty("fes_enabled")
    @Field
    private boolean isFesEnabled;

    @JsonProperty("message_text_list")
    @Field
    private List<Integer> messageTextIdList;

    /**
     * Constructor which sets the submission form data.
     * @param id
     * @param formName the form name
     * @param fee the form submission fee
     * @param isAuthenticationRequired is authentication required
     * @param isFesEnabled is fes enabled
     * @param messageTextIdList list of message textids
     */
    public FormTemplate(final FormTypeKey id, final String formName, final String fee, final boolean isAuthenticationRequired,
        final boolean isFesEnabled, final List<Integer> messageTextIdList) {
        this.id = id;
        this.formName = formName;
        this.fee = fee;
        this.isAuthenticationRequired = isAuthenticationRequired;
        this.isFesEnabled = isFesEnabled;
        this.messageTextIdList = messageTextIdList;
    }

    public FormTypeKey getId() {
        return id;
    }

    public String getFormType() {
        return Optional.ofNullable(id).map(FormTypeKey::getFormType).orElse(null);
    }

    public String getFormCategory() {
        return Optional.ofNullable(id).map(FormTypeKey::getFormCategory).orElse(null);
    }

    public String getFormName() {
        return formName;
    }

    public String getFee() {
        return fee;
    }

    public boolean isAuthenticationRequired() {
        return isAuthenticationRequired;
    }

    public boolean isFesEnabled() {
        return isFesEnabled;
    }

    public List<Integer> getMessageTextIdList() {
        return messageTextIdList;
    }

    public void setMessageTextIdList(final List<Integer> messageTextIdList) {
        this.messageTextIdList = messageTextIdList;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FormTemplate that = (FormTemplate) o;
        return isAuthenticationRequired() == that.isAuthenticationRequired() && isFesEnabled() == that.isFesEnabled()
            && Objects.equals(getFormName(), that.getFormName()) && Objects.equals(getFee(), that.getFee())
            && Objects.equals(getMessageTextIdList(), that.getMessageTextIdList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFormName(), getFee(), isAuthenticationRequired(), isFesEnabled(),
            getMessageTextIdList());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("formType", getFormType())
                .append("formCategory", getFormCategory())
                .append("formName", getFormName())
                .append("fee", getFee())
                .append("isAuthenticationRequired", isAuthenticationRequired())
                .append("isFesEnabled", isFesEnabled())
                .append("messageTextIdList", messageTextIdList)
                .toString();
    }

    /**
     * Composite key identifying a unique form template.
     * <p>
     * The composite primary key class must be public, contains a no-argument constructor,
     * defines both equals() and hashCode() methods, and implements the Serializable interface.
     */
    @Embeddable
    public static class FormTypeKey implements Serializable {
        @JsonProperty("form_type")
        @Field
        private String formType;
        
        @JsonProperty("form_category")
        @Field
        private String formCategory;

        public FormTypeKey() {
            // required by Spring Data
        }

        public FormTypeKey(final String formType, final String formCategory) {
            this.formType = formType;
            this.formCategory = formCategory;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final FormTypeKey that = (FormTypeKey) o;
            return Objects.equals(formType, that.formType) && Objects.equals(formCategory, that.formCategory);
        }

        public String getFormType() {
            return formType;
        }

        public String getFormCategory() {
            return formCategory;
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(formType, formCategory);
        }
    
        @Override
        public String toString() {
            return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
        }
    
    }
}
