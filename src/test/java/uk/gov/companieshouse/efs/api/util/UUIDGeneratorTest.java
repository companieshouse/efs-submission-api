package uk.gov.companieshouse.efs.api.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class UUIDGeneratorTest {

    private UuidGenerator uuidGenerator = new UuidGenerator();

    @Test
    public void testGenerateId(){
        String testId = uuidGenerator.generateId();
        assertNotNull(testId);
    }
}
