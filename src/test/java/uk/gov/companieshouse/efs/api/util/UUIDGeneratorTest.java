package uk.gov.companieshouse.efs.api.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;


class UUIDGeneratorTest {

    private UuidGenerator uuidGenerator = new UuidGenerator();

    @Test
    void testGenerateId(){
        String testId = uuidGenerator.generateId();
        assertNotNull(testId);
    }
}
