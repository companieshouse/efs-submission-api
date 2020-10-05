package uk.gov.companieshouse.efs.api.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
    @Autowired
    public EmailSerialiser(ObjectMapper mapper,
                           EncoderFactory encoderFactory,
                           GenericDatumWriterFactory datumWriterFactory,
                           GenericRecordFactory genericRecordFactory) {
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
        } catch (IOException ex) {
            throw new EmailServiceException("Error serializing email", ex);
        }
    }

    private GenericRecord buildAvroGenericRecord(EmailDocument<?> document, Schema schema)
            throws JsonProcessingException {
        GenericRecord documentData = genericRecordFactory.getGenericRecord(schema);
        documentData.put("app_id", document.getAppId());
        documentData.put("message_id", document.getMessageId());
        documentData.put("message_type", document.getMessageType());
        documentData.put("data", mapper.writeValueAsString(document.getData()));
        documentData.put("email_address", document.getEmailAddress());
        documentData.put("created_at", document.getCreatedAt());
        return documentData;
    }
}
