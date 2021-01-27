package uk.gov.companieshouse.efs.api.formtemplates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Builder class for the {@code FormTemplate}.
 */
@Document(collection = "form_templates")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormTemplate {

    private FormTemplate() {
        // required by Spring Data
    }

    @JsonProperty("form_type")
    @Id
    private String formType;

    @JsonProperty("form_name")
    @Field
    private String formName;

    @JsonProperty("form_category")
    @Field
    private String formCategory;

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
     * @param formType the form type
     * @param formName the form name
     * @param formCategory the form category
     * @param fee the form submission fee
     * @param isAuthenticationRequired is authentication required
     * @param isFesEnabled is fes enabled
     * @param messageTextIdList list of message textids
     */
    public FormTemplate(final String formType, final String formName, final String formCategory,
        final String fee, final boolean isAuthenticationRequired, final boolean isFesEnabled,
        final List<Integer> messageTextIdList) {
        this.formType = formType;
        this.formName = formName;
        this.formCategory = formCategory;
        this.fee = fee;
        this.isAuthenticationRequired = isAuthenticationRequired;
        this.isFesEnabled = isFesEnabled;
        this.messageTextIdList = messageTextIdList;
    }

    public String getFormType() {
        return formType;
    }

    public String getFormName() {
        return formName;
    }

    public String getFormCategory() {
        return formCategory;
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
        return isAuthenticationRequired() == that.isAuthenticationRequired()
               && isFesEnabled() == that.isFesEnabled()
               && Objects.equals(getFormType(), that.getFormType())
               && Objects.equals(getFormName(), that.getFormName())
               && Objects.equals(getFormCategory(), that.getFormCategory())
               && Objects.equals(getFee(), that.getFee())
               && Objects.equals(getMessageTextIdList(), that.getMessageTextIdList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFormType(), getFormName(), getFormCategory(), getFee(),
            isAuthenticationRequired(), isFesEnabled(), getMessageTextIdList());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("formType", getFormType())
                .append("formName", getFormName())
                .append("formCategory", getFormCategory())
                .append("fee", getFee())
                .append("isAuthenticationRequired", isAuthenticationRequired())
                .append("isFesEnabled", isFesEnabled())
                .append("messageTextIdList", messageTextIdList)
                .toString();
    }
}
