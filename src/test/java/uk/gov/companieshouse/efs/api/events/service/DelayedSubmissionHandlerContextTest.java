package uk.gov.companieshouse.efs.api.events.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelayedSubmissionHandlerContextTest {
    private DelayedSubmissionHandlerContext testContext;

    @Mock
    private StandardServiceDelayedHandler standard;
    @Mock
    private SameDayServiceDelayedHandler sameday;

    private Set<DelayedSubmissionHandlerStrategy> implementations;

    @BeforeEach
    void setUp() {
        when(standard.getServiceLevel()).thenReturn(
            DelayedSubmissionHandlerContext.ServiceLevel.STANDARD);
        when(sameday.getServiceLevel()).thenReturn(
            DelayedSubmissionHandlerContext.ServiceLevel.SAMEDAY);
        implementations = new HashSet<>(Arrays.asList(standard, sameday));
        testContext = new DelayedSubmissionHandlerContext(implementations);
    }

    @Test
    void getStandardStrategy() {
        assertThat(testContext.getStrategy(DelayedSubmissionHandlerContext.ServiceLevel.STANDARD),
            is(sameInstance(standard)));
    }

    @Test
    void getSameDayStrategy() {
        assertThat(testContext.getStrategy(DelayedSubmissionHandlerContext.ServiceLevel.SAMEDAY),
            is(sameInstance(sameday)));
    }
}