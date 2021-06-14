package uk.gov.companieshouse.efs.api.categorytemplates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Builder class for the {@code CategoryTemplate}.
 */
@Document(collection = "category_templates")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryTemplate {

    private CategoryTemplate() {
        // required by Spring Data
    }

    @JsonProperty("category_type")
    @Id
    private String categoryType;

    @JsonProperty("category_name")
    @Field
    private String categoryName;

    @JsonProperty("parent")
    @Field
    private String parent;

    @JsonProperty("category_hint")
    @Field
    private String categoryHint;

    /**
     * Constructor which sets the submission form category data.
     * @param categoryType the category type
     * @param categoryName the category name
     * @param parent used when the category has a parent category
     * @param categoryHint the category hint
     */
    public CategoryTemplate(String categoryType, String categoryName, String parent, String categoryHint) {
        this.categoryType = categoryType;
        this.categoryName = categoryName;
        this.parent = parent;
        this.categoryHint = categoryHint;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getParent() {
        return parent;
    }

    public String getCategoryHint() {
        return categoryHint;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CategoryTemplate that = (CategoryTemplate) o;
        return Objects.equals(getCategoryType(), that.getCategoryType()) && Objects
            .equals(getCategoryName(), that.getCategoryName()) && Objects
                   .equals(getParent(), that.getParent()) && Objects
                   .equals(getCategoryHint(), that.getCategoryHint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCategoryType(), getCategoryName(), getParent(), getCategoryHint());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("categoryType", getCategoryType())
                .append("categoryName", getCategoryName())
                .append("parent", getParent())
                .append("categoryHint", getCategoryHint())
                .toString();
    }
}
