package uk.gov.companieshouse.efs.api.fes.service;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GenericDatumWriterFactoryTest {
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
