package uk.gov.companieshouse.efs.api.fes.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenericDatumWriterFactoryTest {
    private GenericDatumWriterFactory factory;

    @Mock
    private Schema schema;

    @BeforeEach
    void setUp() {
        this.factory = new GenericDatumWriterFactory();
    }

    @Test
    void testGenericDatumWriterFactoryReturnsNewInstanceWithSchema() {
        //when
        GenericDatumWriter<GenericRecord> actual = factory.getGenericDatumWriter(schema);

        //then
        assertNotNull(actual);
    }
}
