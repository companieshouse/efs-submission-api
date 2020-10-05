package uk.gov.companieshouse.efs.api.email.model;

import java.util.Objects;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class EmailDocument<T> {
    private String appId;
    private String messageId;
    private String messageType;
    private T data;
    private String emailAddress;
    private String createdAt;
    private String topic;

    public EmailDocument(String appId, String messageId,
                         String messageType, T data,
                         String emailAddress, String createdAt,
                         String topic) {
        this.appId = appId;
        this.messageId = messageId;
        this.messageType = messageType;
        this.data = data;
        this.emailAddress = emailAddress;
        this.createdAt = createdAt;
        this.topic = topic;
    }

    public String getAppId() {
        return appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public T getData() {
        return data;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getTopic() {
        return topic;
    }

    public static <T> EmailDocumentBuilder<T> builder() {
        return new EmailDocumentBuilder<>();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EmailDocument<?> that = (EmailDocument<?>) o;
        return Objects.equals(getAppId(), that.getAppId()) && Objects
            .equals(getMessageId(), that.getMessageId()) && Objects
                   .equals(getMessageType(), that.getMessageType()) && Objects
                   .equals(getData(), that.getData()) && Objects
                   .equals(getEmailAddress(), that.getEmailAddress()) && Objects
                   .equals(getCreatedAt(), that.getCreatedAt()) && Objects
                   .equals(getTopic(), that.getTopic());
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(getAppId(), getMessageId(), getMessageType(), getData(), getEmailAddress(),
                getCreatedAt(), getTopic());
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

    public static class EmailDocumentBuilder<T> {

        private String emailTemplateAppId;
        private String messageId;
        private String emailTemplateMessageType;
        private T data;
        private String recipientEmailAddress;
        private String createdAt;
        private String topic;

        public EmailDocumentBuilder<T> withEmailTemplateAppId(String emailTemplateAppId) {
            this.emailTemplateAppId = emailTemplateAppId;
            return this;
        }

        public EmailDocumentBuilder<T> withMessageId(String randomUuid) {
            this.messageId = randomUuid;
            return this;
        }

        public EmailDocumentBuilder<T> withEmailTemplateMessageType(String emailTemplateMessageType) {
            this.emailTemplateMessageType = emailTemplateMessageType;
            return this;
        }

        public EmailDocumentBuilder<T> withData(T emailData) {
            this.data = emailData;
            return this;
        }

        public EmailDocumentBuilder<T> withRecipientEmailAddress(String recipientEmailAddress) {
            this.recipientEmailAddress = recipientEmailAddress;
            return this;
        }

        public EmailDocumentBuilder<T> withCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EmailDocumentBuilder<T> withTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public EmailDocument<T> build() {
            return new EmailDocument<>(emailTemplateAppId, messageId, emailTemplateMessageType, data, recipientEmailAddress, createdAt, topic);
        }
    }
}
