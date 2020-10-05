package uk.gov.companieshouse.efs.api.email.mapper;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalEmailMapperFactoryTest {
    private ExternalEmailMapperFactory testFactory;

    @Mock
    private ExternalAcceptEmailMapper acceptMapper;
    @Mock
    private ExternalConfirmationEmailMapper confirmationMapper;
    @Mock
    private ExternalRejectEmailMapper rejectMapper;

    @BeforeEach
    void setUp() {
        testFactory = new ExternalEmailMapperFactory(acceptMapper, confirmationMapper, rejectMapper);
    }

    @Test
    void getAcceptEmailMapper() {
        assertThat(testFactory.getAcceptEmailMapper(), is(sameInstance(acceptMapper)));
    }

    @Test
    void getConfirmationMapper() {
        assertThat(testFactory.getConfirmationMapper(), is(sameInstance(confirmationMapper)));
    }

    @Test
    void getRejectMapper() {
        assertThat(testFactory.getRejectMapper(), is(sameInstance(rejectMapper)));
    }
}