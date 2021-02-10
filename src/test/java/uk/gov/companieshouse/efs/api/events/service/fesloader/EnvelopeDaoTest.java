package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvelopeDaoTest {

    private static final long ENVELOPE_ID = 1L;
    private static final long BATCH_ID = 2L;
    private EnvelopeDao envelopeDao;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        this.envelopeDao = new EnvelopeDao(jdbcTemplate);
    }

    @Test
    void testEnvelopeDaoReturnsNextIdFromSequence() {
        //given
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(ENVELOPE_ID);

        //when
        long actual = this.envelopeDao.getNextEnvelopeId();

        //then
        assertEquals(ENVELOPE_ID, actual);
        verify(this.jdbcTemplate).queryForObject(anyString(), eq(Long.class));
    }

    @Test
    void testEnvelopeDaoInsertsNewEnvelopeRecord() {
        //when
        this.envelopeDao.insertEnvelope(ENVELOPE_ID, BATCH_ID);

        //then
        verify(this.jdbcTemplate).update(anyString(), eq(ENVELOPE_ID), eq(BATCH_ID));
    }
}
