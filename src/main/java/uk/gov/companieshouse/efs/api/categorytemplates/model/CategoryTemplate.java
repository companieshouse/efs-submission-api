package uk.gov.companieshouse.efs.api.categorytemplates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a category template document stored in the MongoDB collection "category_templates".
 * <p>
 * This record is used to model category metadata for forms, including type, name, parent category,
 * display order, hints, and associated guidance text IDs. It is annotated for use with Spring Data MongoDB
 * and Jackson for serialization/deserialization.
 * </p>
 *
 * <ul>
 *   <li>{@code categoryType}: Unique identifier for the category type.</li>
 *   <li>{@code orderIndex}: Display order index for sorting categories.</li>
 *   <li>{@code categoryName}: Human-readable name of the category.</li>
 *   <li>{@code parent}: Parent category type, if applicable.</li>
 *   <li>{@code categoryHint}: Optional hint or description for the category.</li>
 *   <li>{@code guidanceTexts}: List of guidance text IDs associated with the category and converted to an empty list if null.</li>
 * </ul>
 */
@Document(collection = "category_templates")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryTemplate(
    @JsonProperty("category_type")
    @Id
    String categoryType,

    @JsonProperty("order_index")
    @Field
    Integer orderIndex,

    @JsonProperty("category_name")
    @Field
    String categoryName,

    @JsonProperty("parent")
    @Field
    String parent,

    @JsonProperty("category_hint")
    @Field
    String categoryHint,

    @JsonProperty("guidance_text_list")
    @Field
    List<Integer> guidanceTexts
) {
    public CategoryTemplate {
        guidanceTexts = Objects.requireNonNullElse(guidanceTexts, Collections.emptyList());
    }
}
