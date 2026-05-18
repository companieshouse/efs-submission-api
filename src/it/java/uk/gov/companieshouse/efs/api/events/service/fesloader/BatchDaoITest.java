package uk.gov.companieshouse.efs.api.events.service.fesloader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.efs.api.util.OracleTestContainer;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BatchDaoITest {

    static JdbcTemplate jdbcTemplate;
    static BatchDao batchDao;

    @BeforeAll
    static void setUpBatchDao() {
        jdbcTemplate = new JdbcTemplate(
            new DriverManagerDataSource(
                OracleTestContainer.INSTANCE.getJdbcUrl(),
                OracleTestContainer.INSTANCE.getUsername(),
                OracleTestContainer.INSTANCE.getPassword()
            )
        );

        jdbcTemplate.execute("""
            CREATE TABLE BATCH (
                BATCH_ID                NUMBER(10)          NOT NULL,
                BATCH_SCANNED           TIMESTAMP(6)        NOT NULL,
                BATCH_STATUS_ID         NUMBER(10)          NOT NULL,
                BATCH_SCANNER_NAME      VARCHAR2(50 Char)   NOT NULL,
                BATCH_SCAN_PERSON       VARCHAR2(50 Char)   NOT NULL,
                BATCH_NAME              VARCHAR2(40 Char)   NOT NULL,
                BATCH_OPTLOCK           NUMBER(10)          DEFAULT 0,
                BATCH_SCANNED_LOCATION  NUMBER(10),
                BATCH_PROCESSED_DATE    TIMESTAMP(6)
             )
        """);

        jdbcTemplate.execute("CREATE SEQUENCE BATCH_ID_SEQ START WITH 1 INCREMENT BY 1");

        jdbcTemplate.execute("""
            CREATE OR REPLACE PACKAGE fes_common_pkg AS
                FUNCTION F_GETNEXTREFID(batch_name_prefix IN VARCHAR2, requested_len IN NUMBER) RETURN NUMBER;
            END fes_common_pkg;
        """);

        jdbcTemplate.execute("""
            CREATE OR REPLACE PACKAGE BODY fes_common_pkg AS
                FUNCTION F_GETNEXTREFID(batch_name_prefix IN VARCHAR2, requested_len IN NUMBER) RETURN NUMBER IS
                BEGIN
                    RETURN 123456789;
                END F_GETNEXTREFID;
            END fes_common_pkg;
        """);

        batchDao = new BatchDao(jdbcTemplate);
    }

    @Test
    void getNextBatchId_returnsSequenceValue() {
        assertThat(batchDao.getNextBatchId(), is(1L));
    }

    @Test
    void getBatchNameId_returnsPackageFunctionValue() {
        assertThat(batchDao.getBatchNameId("EFS_20260518"), is(123456789L));
    }

    @Test
    void insertBatch_insertsRow() {
        final var timestamp = LocalDateTime.of(2026, 5, 18, 10, 30);

        batchDao.insertBatch(100L, "EFS_20260518_0001", timestamp);

        final var verifyQuery = """
            SELECT BATCH_ID,
                   BATCH_SCANNED,
                   BATCH_STATUS_ID,
                   BATCH_SCANNER_NAME,
                   BATCH_SCAN_PERSON,
                   BATCH_NAME,
                   BATCH_SCANNED_LOCATION
            FROM BATCH
            WHERE BATCH_ID = ?
        """;

        jdbcTemplate.query(verifyQuery, rs -> {
            assertThat(rs.getLong("BATCH_ID"), is(100L));
            assertThat(rs.getTimestamp("BATCH_SCANNED").toLocalDateTime(), is(timestamp));
            assertThat(rs.getLong("BATCH_STATUS_ID"), is(1L));
            assertThat(rs.getString("BATCH_SCANNER_NAME"), is("efs_batch"));
            assertThat(rs.getString("BATCH_SCAN_PERSON"), is("efs_filing"));
            assertThat(rs.getString("BATCH_NAME"), is("EFS_20260518_0001"));
            assertThat(rs.getLong("BATCH_SCANNED_LOCATION"), is(1L));
        }, 100L);
    }
}

