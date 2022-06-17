package uk.gov.companieshouse.efs.api.categorytemplates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    @JsonProperty("order_index")
    @Field
    private Integer orderIndex;

    @JsonProperty("category_name")
    @Field
    private String categoryName;

    @JsonProperty("parent")
    @Field
    private String parent;

    @JsonProperty("category_hint")
    @Field
    private String categoryHint;

    @JsonProperty("guidance_text_list")
    @Field
    private List<Integer> guidanceTexts;

    /**
     * Constructor which sets the submission form category data.
     *
     * @param categoryType  the category type
     * @param orderIndex    the ordering within the category
     * @param categoryName  the category name
     * @param parent        used when the category has a parent category
     * @param categoryHint  the category hint
     * @param guidanceTexts a list of id's of guidance fragments to show on the category
     *                      selection screen
     */
    public CategoryTemplate(String categoryType, final Integer orderIndex, String categoryName,
            String parent, String categoryHint, final List<Integer> guidanceTexts) {
        this.categoryType = categoryType;
        this.orderIndex = orderIndex;
        this.categoryName = categoryName;
        this.parent = parent;
        this.categoryHint = categoryHint;
        this.guidanceTexts = guidanceTexts;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public Integer getOrderIndex() {
        return orderIndex;
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

    public List<Integer> getGuidanceTexts() {
        return Optional.ofNullable(guidanceTexts)
                .orElse(Collections.emptyList());
    }

    public void setGuidanceTexts(List<Integer> guidanceTexts) {
        this.guidanceTexts = guidanceTexts;
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
        return Objects.equals(getCategoryType(), that.getCategoryType()) &&
                Objects.equals(getOrderIndex(), that.getOrderIndex()) &&
                Objects.equals(getCategoryName(), that.getCategoryName()) &&
                Objects.equals(getParent(), that.getParent()) &&
                Objects.equals(getCategoryHint(), that.getCategoryHint()) &&
                Objects.equals(getGuidanceTexts(), that.getGuidanceTexts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCategoryType(), getOrderIndex(), getCategoryName(), getParent(),
                getCategoryHint(), getGuidanceTexts());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("categoryType", getCategoryType())
                .append("orderIndex", getOrderIndex())
                .append("categoryName", getCategoryName())
                .append("parent", getParent())
                .append("categoryHint", getCategoryHint())
                .append("guidanceTexts", getGuidanceTexts())
                .toString();
    }
}
