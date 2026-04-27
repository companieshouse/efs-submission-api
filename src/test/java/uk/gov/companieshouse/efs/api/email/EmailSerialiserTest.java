package uk.gov.companieshouse.efs.api.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.efs.api.email.exception.EmailServiceException;
import uk.gov.companieshouse.efs.api.email.model.EmailDocument;
import uk.gov.companieshouse.efs.api.fes.service.GenericDatumWriterFactory;
import uk.gov.companieshouse.efs.api.fes.service.GenericRecordFactory;

@ExtendWith(MockitoExtension.class)
class EmailSerialiserTest {

    private EmailSerialiser serialiser;

    @Mock
    private EmailDocument<?> document;

    @Mock
    private GenericDatumWriterFactory datumWriterFactory;

    @Mock
    private GenericDatumWriter<GenericRecord> datumWriter;

    @Mock
    private EncoderFactory encoderFactory;

    @Mock
    private BinaryEncoder binaryEncoder;

    @Mock
    private GenericRecordFactory genericRecordFactory;

    @Mock
    private GenericRecord genericRecord;

    @Mock
    private Schema schema;

    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        // Use a real JsonMapper by default
        this.mapper = JsonMapper.builder().build();
        this.serialiser = new EmailSerialiser(mapper, encoderFactory, datumWriterFactory, genericRecordFactory);
    }

    @Test
    void serializeSuccess() {
        //given
        when(encoderFactory.binaryEncoder(any(), any())).thenReturn(binaryEncoder);
        when(genericRecordFactory.getGenericRecord(schema)).thenReturn(genericRecord);
        when(datumWriterFactory.getGenericDatumWriter(any())).thenReturn(datumWriter);
        when(document.getCreatedAt()).thenReturn("02 June 2020");

        //when
        byte[] actual = serialiser.serialize(document, schema);

        //then
        assertNotNull(actual);
        verify(genericRecordFactory).getGenericRecord(schema);
        verify(encoderFactory).binaryEncoder(any(), any());
        verify(genericRecordFactory).getGenericRecord(schema);
        verify(genericRecord).put("email_address", document.getEmailAddress());
        verify(genericRecord).put(eq("created_at"), anyString());
    }

    @Test
    void serializeWhenJsonParsingErrorThrowsEmailServiceException() throws JacksonException{
        //given
        when(encoderFactory.binaryEncoder(any(), any())).thenReturn(binaryEncoder);
        when(genericRecordFactory.getGenericRecord(schema)).thenReturn(genericRecord);
        // Use a custom ObjectMapper that always throws JacksonException
        final var throwingMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(final Object value) throws JacksonException {
                throw new JacksonException("Stubbed for test") {};
            }
        };
        this.serialiser = new EmailSerialiser(throwingMapper, encoderFactory, datumWriterFactory, genericRecordFactory);
        when(datumWriterFactory.getGenericDatumWriter(any())).thenReturn(datumWriter);

        //when
        Executable actual = () -> serialiser.serialize(document, schema);

        //then
        EmailServiceException actualException = assertThrows(EmailServiceException.class, actual);
        assertEquals("Error serializing email", actualException.getMessage());
    }

}
