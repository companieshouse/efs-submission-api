package uk.gov.companieshouse.efs.api.submissions.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RandomPatternGeneratorImplTest {
    private RandomPatternGeneratorImpl testGenerator;

    @Mock
    private SecureRandom secureRandom;

    @BeforeEach
    void setUp() throws NoSuchProviderException, NoSuchAlgorithmException {
        testGenerator = new RandomPatternGeneratorImpl(secureRandom, "###-###", "AAA");
    }

    @Test
    void getSecureRandom()
    {
        assertThat(testGenerator.getSecureRandom(), is(sameInstance(secureRandom)));
    }

    @Test
    void setSecureRandomWhenNull() {
        testGenerator.setSecureRandom(null);

        assertThat(testGenerator.getSecureRandom(), is(nullValue()));
    }

    @Test
    void setSecureRandomWhenNonNull() {
        testGenerator.setSecureRandom(secureRandom);

        verify(secureRandom, times(2)).nextBytes(any(byte[].class));
    }

    @Test
    void getPattern() {
        assertThat(testGenerator.getPattern(), is("###-###"));
    }

    @Test
    void getSymbolSet() {
        assertThat(testGenerator.getSymbolSet(), is("AAA"));
    }

    @Test
    void generateId() {
        final String id = testGenerator.generateId();

        assertThat(id, is("AAA-AAA"));
    }
}