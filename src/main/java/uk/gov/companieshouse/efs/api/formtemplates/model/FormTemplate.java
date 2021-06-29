package uk.gov.companieshouse.efs.api.formtemplates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity class for the {@code FormTemplate}.
 * formType: the form type
 * formName: the form name
 * formCategory: the form category
 * fee: the form submission fee
 * isAuthenticationRequired: is authentication required
 * isFesEnabled: is fes enabled
 * fesDocType: FES doc type (if different from formType, otherwise null)
 * sameDay: is fes same day indicator required
 * messageTextIdList: list of message textids
 */
@Document(collection = "form_templates")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormTemplate {

    private FormTemplate() {
        // no direct instantiation
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
    
    @JsonProperty("fes_doc_type")
    @Field
    private String fesDocType;
    
    @JsonProperty("same_day")
    @Field
    private boolean sameDay;

    @JsonProperty("message_text_list")
    @Field
    private List<Integer> messageTextIdList;

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

    public String getFesDocType() {
        return fesDocType;
    }

    public boolean isSameDay() {
        return sameDay;
    }

    public List<Integer> getMessageTextIdList() {
        return Optional.ofNullable(messageTextIdList).map(ArrayList::new).orElse(null);
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
            && isFesEnabled() == that.isFesEnabled() && isSameDay() == that.isSameDay()
            && Objects.equals(getFormType(), that.getFormType()) && Objects.equals(getFormName(),
            that.getFormName()) && Objects.equals(getFormCategory(), that.getFormCategory())
            && Objects.equals(getFee(), that.getFee()) && Objects.equals(getFesDocType(),
            that.getFesDocType()) && Objects.equals(getMessageTextIdList(),
            that.getMessageTextIdList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFormType(), getFormName(), getFormCategory(), getFee(),
            isAuthenticationRequired(), isFesEnabled(), getFesDocType(), isSameDay(),
            getMessageTextIdList());
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
                .append("fesDocType", getFesDocType())
                .append("sameDay", isSameDay())
                .append("messageTextIdList", messageTextIdList)
                .toString();
    }
    
    public static FormTemplate.Builder builder() {
        return new FormTemplate.Builder();
    }
    
    public static class Builder {
        private final List<Consumer<FormTemplate>> buildSteps;
        
        private Builder() {
            buildSteps = new ArrayList<>();
        }
        
        public FormTemplate.Builder withFormType(final String formType) {
            buildSteps.add(data -> data.formType = formType);
            return this;
        }
        
        public FormTemplate.Builder withFormName(final String formName) {
            buildSteps.add(data -> data.formName = formName);
            return this;
        }
        
        public FormTemplate.Builder withFormCategory(final String formCategory) {
            buildSteps.add(data -> data.formCategory = formCategory);
            return this;
        }
        
        public FormTemplate.Builder withFee(final String fee) {
            buildSteps.add(data -> data.fee = fee);
            return this;
        }

        public FormTemplate.Builder withAuthenticationRequired(
            final boolean authenticationRequired) {
            buildSteps.add(data -> data.isAuthenticationRequired = authenticationRequired);
            return this;
        }
        
        public FormTemplate.Builder withFesEnabled(final boolean isFesEnabled) {
            buildSteps.add(data -> data.isFesEnabled = isFesEnabled);
            return this;
        }
        
        public FormTemplate.Builder withFesDocType(final String fesDocType) {
            buildSteps.add(data -> data.fesDocType = fesDocType);
            return this;
        }

        public FormTemplate.Builder withSameDay(final boolean sameDay) {
            buildSteps.add(data -> data.sameDay = sameDay);
            return this;
        }

        public FormTemplate.Builder withMessageTextIdList(final List<Integer> messageTextIdList) {
            buildSteps.add(data -> data.messageTextIdList =
                Optional.ofNullable(messageTextIdList).map(ArrayList::new).orElse(null));
            return this;
        }
        
        public FormTemplate build() {
            FormTemplate data = new FormTemplate();
            
            buildSteps.forEach(step -> step.accept(data));
            
            return data;
        }
    }
}
