package uk.gov.companieshouse.efs.api.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RandomStringPatternTest {

    private RandomStringPattern testPattern;

    @Mock
    Random notRandom;

    @Test
    void randomStringPatternWithMeta() {
        testPattern = new RandomStringPattern(notRandom, "##-XXX-##", "ABC");

        assertThat(testPattern.nextString(), is("AA-XXX-AA"));
    }

    @Test
    void randomStringPatternWithoutMeta() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new RandomStringPattern(notRandom, "XXX", "ABC"));

        assertThat(thrown.getMessage(), is("'#' missing from 'pattern' parameter."));
    }

    @Test
    void randomStringPatternWhenSymbolsTooShort() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new RandomStringPattern(notRandom, "#", "R"));

        assertThat(thrown.getMessage(), is("'symbols' parameter is too short."));
    }

}