package uk.gov.companieshouse.efs.api.events.service.fesloader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.efs.api.util.OracleTestContainer;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnvelopeDaoITest {

    static JdbcTemplate jdbcTemplate;
    static EnvelopeDao envelopeDao;

    @BeforeAll
    static void setUpEnvelopeDao() {
        jdbcTemplate = new JdbcTemplate(
            new DriverManagerDataSource(
                OracleTestContainer.INSTANCE.getJdbcUrl(),
                OracleTestContainer.INSTANCE.getUsername(),
                OracleTestContainer.INSTANCE.getPassword()
            )
        );

        jdbcTemplate.execute("""
            CREATE TABLE ENVELOPE (
                ENVELOPE_ID        NUMBER(10)   NOT NULL,
                ENVELOPE_BATCH_ID  NUMBER(10)   NOT NULL,
                ENVELOPE_OPTLOCK   NUMBER(10)   DEFAULT 0
             )
        """);

        jdbcTemplate.execute("CREATE SEQUENCE ENVELOPE_ID_SEQ START WITH 1 INCREMENT BY 1");

        envelopeDao = new EnvelopeDao(jdbcTemplate);
    }

    @Test
    void getNextEnvelopeId_returnsSequenceValue() {
        assertThat(envelopeDao.getNextEnvelopeId(), is(1L));
    }

    @Test
    void insertEnvelope_insertsRow() {
        envelopeDao.insertEnvelope(200L, 300L);

        final var verifyQuery = """
            SELECT ENVELOPE_ID,
                   ENVELOPE_BATCH_ID
            FROM ENVELOPE
            WHERE ENVELOPE_ID = ?
        """;

        jdbcTemplate.query(verifyQuery, rs -> {
            assertThat(rs.getLong("ENVELOPE_ID"), is(200L));
            assertThat(rs.getLong("ENVELOPE_BATCH_ID"), is(300L));
        }, 200L);
    }
}

