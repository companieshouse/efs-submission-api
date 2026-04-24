package uk.gov.companieshouse.efs.api.email;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.fes.service.GenericDatumWriterFactory;
import uk.gov.companieshouse.efs.api.fes.service.GenericRecordFactory;

@Component
public class EmailSerialiser {

    private GenericDatumWriterFactory datumWriterFactory;

    private EncoderFactory encoderFactory;

    private ObjectMapper mapper;

    private GenericRecordFactory genericRecordFactory;

    /**
     * Constructor.
     *
     * @param mapper                dependency
     * @param encoderFactory        dependency
     * @param datumWriterFactory    dependency
     * @param genericRecordFactory  dependency
     */
    public EmailSerialiser(final ObjectMapper mapper,
                           final EncoderFactory encoderFactory,
                           final GenericDatumWriterFactory datumWriterFactory,
                           final GenericRecordFactory genericRecordFactory) {
        this.mapper = mapper;
        this.encoderFactory = encoderFactory;
        this.datumWriterFactory = datumWriterFactory;
        this.genericRecordFactory = genericRecordFactory;
    }

    public byte[] serialize(EmailDocument<?> document, Schema schema) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.binaryEncoder(stream, null);
            GenericDatumWriter<GenericRecord> datumWriter = datumWriterFactory.getGenericDatumWriter(schema);
            datumWriter.write(buildAvroGenericRecord(document, schema), encoder);
            encoder.flush();
            return stream.toByteArray();
        } catch (final IOException | JacksonException ex) {
            throw new EmailServiceException("Error serializing email", ex);
        }
    }

    private GenericRecord buildAvroGenericRecord(final EmailDocument<?> document, final Schema schema)
            throws JacksonException {
        final var documentData = genericRecordFactory.getGenericRecord(schema);
        documentData.put("app_id", document.getAppId());
        documentData.put("message_id", document.getMessageId());
        documentData.put("message_type", document.getMessageType());
        documentData.put("data", mapper.writeValueAsString(document.getData()));
        documentData.put("email_address", document.getEmailAddress());
        documentData.put("created_at", document.getCreatedAt());
        return documentData;
    }
}
