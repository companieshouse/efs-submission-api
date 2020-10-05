package uk.gov.companieshouse.efs.api.paymentreports.service;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutputStreamWriterFactoryTest {
    private OutputStreamWriterFactory testFactory;

    @Mock
    private BufferedOutputStream bufferedOutputStream;

    @BeforeEach
    void setUp() {
        testFactory = new OutputStreamWriterFactory();
    }

    @Test
    void createForCreatesNewInstances() {
        final OutputStreamWriter writer1 = testFactory.createFor(bufferedOutputStream);
        final OutputStreamWriter writer2 = testFactory.createFor(bufferedOutputStream);

        assertThat(writer1, is(instanceOf(OutputStreamWriter.class)));
        assertThat(writer2, is(not(sameInstance(writer1))));
    }

}