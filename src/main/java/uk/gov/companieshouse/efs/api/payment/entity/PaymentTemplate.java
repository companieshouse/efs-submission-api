package uk.gov.companieshouse.efs.api.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Builder class for the {@code PaymentTemplate}
 */
@Document(collection = "payment_templates")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = PaymentTemplate.Builder.class)
public final class PaymentTemplate {

    private PaymentTemplate() {
    // required by Spring Data
    }

    private PaymentTemplate(final Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.etag = builder.etag;
        this.items = builder.items;
        this.kind = builder.kind;
        this.links = builder.links;
        this.paymentReference = builder.paymentReference;
        this.status = builder.status;
        this.companyNumber = builder.companyNumber;
    }

    /**
     * {@link Builder}  static constructor.
     *
     * @return {@link Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * {@link PaymentTemplate} builder static constructor.
     *
     * @param original the {@link PaymentTemplate}
     * @return a new builder for {@link PaymentTemplate}
     */
    public static Builder newBuilder(final PaymentTemplate original) {
        Builder builder = new Builder();
        builder.id = original.getId();
        builder.description = original.getDescription();
        builder.etag = original.getEtag();
        builder.items = original.getItems();
        builder.kind = original.getKind();
        builder.links = original.getLinks();
        builder.paymentReference = original.getPaymentReference();
        builder.status = original.getStatus();
        builder.companyNumber = original.getCompanyNumber();
        return builder;
    }

    /**
     * {@code Item} inner class
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(builder = Item.Builder.class)
    public static class Item {
        private String amount;
        @Field("available_payment_methods")
        @JsonProperty("available_payment_methods")
        private List<String> availablePaymentMethods;
        @Field("class_of_payment")
        @JsonProperty("class_of_payment")
        private List<String> classOfPayment;
        private String description;
        @Field("description_identifier")
        @JsonProperty("description_identifier")
        private String descriptionId;
        private String kind;
        @Field("product_type")
        @JsonProperty("product_type")
        private String productType;

        private Item() {
            // required by Spring Data
        }

        /**
         * {@code Item} constructor
         *
         * @param other {@code Item}
         */
        public Item(final Item other) {
            this.amount = other.amount;
            this.availablePaymentMethods = cloneStringList(other.availablePaymentMethods);
            this.classOfPayment = cloneStringList(other.classOfPayment);
            this.description = other.description;
            this.descriptionId = other.descriptionId;
            this.kind = other.kind;
            this.productType = other.productType;
        }

        private Item(final Builder builder) {
            this.amount = builder.amount;
            this.availablePaymentMethods = builder.availablePaymentMethods;
            this.classOfPayment = builder.classOfPayment;
            this.description = builder.description;
            this.descriptionId = builder.descriptionId;
            this.kind = builder.kind;
            this.productType = builder.productType;
        }

        /**
         * The {@link PaymentTemplate.Builder}
         *
         * @return a new {@link PaymentTemplate.Builder}
         */
        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * {@code Item} builder static constructor.
         *
         * @param copy {@code Item}
         * @return new builder
         */
        public static Builder newBuilder(final Item copy) {
            Builder builder = new Builder();
            builder.amount = copy.getAmount();
            builder.availablePaymentMethods = copy.getAvailablePaymentMethods();
            builder.classOfPayment = copy.getClassOfPayment();
            builder.description = copy.getDescription();
            builder.descriptionId = copy.getDescriptionId();
            builder.kind = copy.getKind();
            builder.productType = copy.getProductType();
            return builder;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(final String amount) {
            this.amount = amount;
        }

        public List<String> getAvailablePaymentMethods() {
            return cloneStringList(availablePaymentMethods);
        }

        public void setAvailablePaymentMethods(final List<String> availablePaymentMethods) {
            this.availablePaymentMethods = cloneStringList(availablePaymentMethods);
        }

        public List<String> getClassOfPayment() {
            return cloneStringList(classOfPayment);
        }

        public void setClassOfPayment(final List<String> classOfPayment) {
            this.classOfPayment = cloneStringList(classOfPayment);
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public String getDescriptionId() {
            return descriptionId;
        }

        public void setDescriptionId(final String descriptionId) {
            this.descriptionId = descriptionId;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(final String kind) {
            this.kind = kind;
        }

        public String getProductType() {
            return productType;
        }

        public void setProductType(final String productType) {
            this.productType = productType;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Item item = (Item) o;
            return Objects.equals(getAmount(), item.getAmount()) && Objects
                .equals(getAvailablePaymentMethods(), item.getAvailablePaymentMethods()) && Objects
                       .equals(getClassOfPayment(), item.getClassOfPayment()) && Objects
                       .equals(getDescription(), item.getDescription()) && Objects
                       .equals(getDescriptionId(), item.getDescriptionId()) && Objects
                       .equals(getKind(), item.getKind()) && Objects
                       .equals(getProductType(), item.getProductType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAmount(), getAvailablePaymentMethods(), getClassOfPayment(),
                getDescription(), getDescriptionId(), getKind(), getProductType());
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("amount", amount).append("availablePaymentMethods", availablePaymentMethods)
                .append("classOfPayment", classOfPayment).append("description", description)
                .append("descriptionId", descriptionId).append("kind", kind)
                .append("productType", productType).toString();
        }

        private static List<String> cloneStringList(final List<String> list) {
            return list == null
                ? null
                : list.stream().map(String::new).collect(Collectors.toList());
        }

        /**
         * {@code PaymentTemplate} builder static inner class.
         */
        public static final class Builder {
            private String amount;
            @JsonProperty("available_payment_methods")
            private List<String> availablePaymentMethods;
            @JsonProperty("class_of_payment")
            private List<String> classOfPayment;
            private String description;
            @JsonProperty("description_identifier")
            private String descriptionId;
            private String kind;
            @JsonProperty("product_type")
            private String productType;

            private Builder() {
            }

            /**
             * set the amount on the {@code PaymentTemplate}
             *
             * @param val the amount
             * @return builder with the amount set to specified value
             */
            public Builder withAmount(final String val) {
                amount = val;
                return this;
            }

            /**
             * set the available payment methods on the {@code PaymentTemplate}
             *
             * @param val the available payment methods
             * @return builder with the available payment methods set to specified value
             */
            public Builder withAvailablePaymentMethods(final List<String> val) {
                availablePaymentMethods = cloneStringList(val);
                return this;
            }

            /**
             * set the class of payment on the {@code PaymentTemplate}
             *
             * @param val the class of payment
             * @return builder with the class of payment set to specified value
             */
            public Builder withClassOfPayment(final List<String> val) {
                classOfPayment = cloneStringList(val);
                return this;
            }

            /**
             * set the description on the {@code PaymentTemplate}
             *
             * @param val the description
             * @return builder with the description set to specified value
             */
            public Builder withDescription(final String val) {
                description = val;
                return this;
            }

            /**
             * set the description id on the {@code PaymentTemplate}
             *
             * @param val the description id
             * @return builder with the description id set to specified value
             */
            public Builder withDescriptionId(final String val) {
                descriptionId = val;
                return this;
            }

            /**
             * set the kind of payment on the {@code PaymentTemplate}
             *
             * @param val the kind of payment
             * @return builder with the kind of payment set to specified value
             */
            public Builder withKind(final String val) {
                kind = val;
                return this;
            }

            /**
             * set the product type on the {@code PaymentTemplate}
             *
             * @param val the product type
             * @return builder with the product type set to specified value
             */
            public Builder withProductType(final String val) {
                productType = val;
                return this;
            }

            /**
             * builder for a new {@link Item}
             *
             * @return new {@link Item}
             */
            public Item build() {
                return new Item(this);
            }
        }
    }

    /**
     * Contains links required by the payment service
     */
    public static class Links {
        private String resource;
        private URL self;

        /**
         * constructor that sets the resource and self URL
         * @param resource the resource link
         * @param self URL required by payment service to enable return back to calling service
         */
        public Links(final String resource, final URL self) {
            this.resource = resource;
            this.self = self;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(final String resource) {
            this.resource = resource;
        }

        public URL getSelf() {
            return self;
        }

        public void setSelf(final URL self) {
            this.self = self;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Links links = (Links) o;
            return Objects.equals(getResource(), links.getResource()) && Objects
                .equals(getSelf(), links.getSelf());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getResource(), getSelf());
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("resource", resource).append("self", self).toString();
        }
    }

    /**
     * Payment status enum
     */
    public enum Status {
        PAID("paid"), PENDING("pending"), FAILED("failed");

        private String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Returns the status or null if no match found
         *
         * @param text the status value
         * @return the status or null if no match found
         */
        @JsonCreator
        public static Status fromValue(String text) {
            for (Status b : Status.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @Id
    private String id;

    @Field
    private String description;
    @Field
    private String etag;
    @Field
    private List<Item> items;
    @Field
    private String kind;
    @Field
    private Links links;
    @JsonProperty("payment_reference")
    @Field
    private String paymentReference;
    @Field
    private Status status;
    @JsonProperty("company_number")
    @Transient // not an entity field; for use by PaymentController.getPaymentDetails() response
    private String companyNumber;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(final String etag) {
        this.etag = etag;
    }

    private static List<Item> cloneItems(final List<Item> list) {
        return list == null ? null : list.stream().map(Item::new).collect(Collectors.toList());
    }


    public List<Item> getItems() {
        return cloneItems(items);
    }

    public void setItems(final List<Item> items) {
        this.items = cloneItems(items);
    }

    public String getKind() {
        return kind;
    }

    public void setKind(final String kind) {
        this.kind = kind;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(final Links links) {
        this.links = links;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(final String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PaymentTemplate that = (PaymentTemplate) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDescription(), that.getDescription())
            && Objects.equals(getEtag(), that.getEtag()) && Objects.equals(getItems(), that.getItems()) && Objects
            .equals(getKind(), that.getKind()) && Objects.equals(getLinks(), that.getLinks()) && Objects
            .equals(getPaymentReference(), that.getPaymentReference()) && getStatus() == that.getStatus() && Objects
            .equals(getCompanyNumber(), that.getCompanyNumber());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getId(), getDescription(), getEtag(), getItems(), getKind(), getLinks(), getPaymentReference(),
                getStatus(), getCompanyNumber());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id)
            .append("description", description).append("etag", etag).append("items", items).append("kind", kind)
            .append("links", links).append("paymentReference", paymentReference).append("status", status)
            .append("companyNumber", companyNumber).toString();
    }

    /**
     * {@code PaymentTemplate} static builder class
     */
    public static final class Builder {
        private String id;
        private String description;
        private String etag;
        @JsonProperty("items")
        private List<Item> items;
        private String kind;
        private Links links;
        @JsonProperty("payment_reference")
        private String paymentReference;
        private Status status;
        @JsonProperty("company_number")
        private String companyNumber;

        private Builder() {
        }

        /**
         * set the id on the {@code PaymentTemplate}
         *
         * @param val the id
         * @return builder with the id set to specified value
         */
        public Builder withId(final String val) {
            id = val;
            return this;
        }

        /**
         * set the description on the {@code PaymentTemplate}
         *
         * @param val the description
         * @return builder with the description set to specified value
         */
        public Builder withDescription(final String val) {
            description = val;
            return this;
        }

        /**
         * set the etag on the {@code PaymentTemplate}
         *
         * @param val the etag
         * @return builder with the etag set to specified value
         */
        public Builder withEtag(final String val) {
            etag = val;
            return this;
        }

        /**
         * set the items on the {@code PaymentTemplate}
         *
         * @param val the items
         * @return builder with the items set to specified value
         */
        public Builder withItems(final List<Item> val) {
            items = cloneItems(val);
            return this;
        }

        /**
         * add {@code Item} to the Item list
         *
         * @param val the item
         * @return builder with specified item added to item list
         */
        public Builder withItem(final Item val) {
            if (items == null) {
                items = new ArrayList<>();
            }
            items.add(val);
            return this;
        }

        /**
         * set the kind of payment on the {@code PaymentTemplate}
         *
         * @param val the kind of payment
         * @return builder with the kind of payment set to specified value
         */
        public Builder withKind(final String val) {
            kind = val;
            return this;
        }

        /**
         * set the link to the payment on the {@code PaymentTemplate}
         *
         * @param val the payment link
         * @return builder with the payment link set to specified value
         */
        public Builder withLinks(final Links val) {
            links = val;
            return this;
        }

        /**
         * set the payment reference on the {@code PaymentTemplate}
         *
         * @param val the payment reference
         * @return builder with the payment reference set to specified value
         */
        public Builder withPaymentReference(final String val) {
            paymentReference = val;
            return this;
        }

        /**
         * set the payment status on the {@code PaymentTemplate}
         *
         * @param val the payment status
         * @return builder with the payment status set to specified value
         */
        public Builder withStatus(final Status val) {
            status = val;
            return this;
        }


        /**
         * set the company number on the {@code PaymentTemplate}
         *
         * @param val the company number
         * @return builder with the company number set to the specified value
         */
        public Builder withCompanyNumber(final String val) {
            companyNumber = val;
            return this;
        }

        /**
         * Builds a {@code PaymentTemplate}
         *
         * @return {@code PaymentTemplate}
         */
        public PaymentTemplate build() {
            return new PaymentTemplate(this);
        }
    }
}
