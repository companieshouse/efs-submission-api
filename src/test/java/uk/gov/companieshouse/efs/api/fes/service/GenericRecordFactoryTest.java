package uk.gov.companieshouse.efs.api.fes.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenericRecordFactoryTest {

    private GenericRecordFactory genericRecordFactory;

    @Mock
    private Schema schema;

    @BeforeEach
    void setUp() {
        this.genericRecordFactory = new GenericRecordFactory();
    }

    @Test
    void testGenericRecordFactoryReturnsGenericDataRecordWithSchema() {
        //when
        GenericRecord actual = genericRecordFactory.getGenericRecord(
            Schema.createRecord(null, null, null, false, Collections.emptyList()));

        //then
        assertNotNull(actual);
    }
}
