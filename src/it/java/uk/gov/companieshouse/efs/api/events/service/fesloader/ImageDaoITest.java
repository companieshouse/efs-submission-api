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
class ImageDaoITest {

    static JdbcTemplate jdbcTemplate;
    static ImageDao imageDao;

    @BeforeAll
    static void setUpImageDao() {
        jdbcTemplate = new JdbcTemplate(
            new DriverManagerDataSource(
                OracleTestContainer.INSTANCE.getJdbcUrl(),
                OracleTestContainer.INSTANCE.getUsername(),
                OracleTestContainer.INSTANCE.getPassword()
            )
        );

        jdbcTemplate.execute("""
            CREATE TABLE IMAGE (
                IMAGE_ID       NUMBER(10)   NOT NULL,
                IMAGE_IMAGE    BLOB,
                IMAGE_OPTLOCK  NUMBER(10)   DEFAULT 0
             )
        """);

        jdbcTemplate.execute("CREATE SEQUENCE IMAGE_ID_SEQ START WITH 1 INCREMENT BY 1");

        imageDao = new ImageDao(jdbcTemplate);
    }

    @Test
    void getNextImageId_returnsSequenceValue() {
        assertThat(imageDao.getNextImageId(), is(1L));
    }

    @Test
    void insertImage_insertsRow() {
        final var payload = "test-image".getBytes();

        imageDao.insertImage(400L, payload);

        final var verifyQuery = """
            SELECT IMAGE_ID,
                   IMAGE_IMAGE
            FROM IMAGE
            WHERE IMAGE_ID = ?
        """;

        jdbcTemplate.query(verifyQuery, rs -> {
            assertThat(rs.getLong("IMAGE_ID"), is(400L));
            assertThat(rs.getBytes("IMAGE_IMAGE"), is(payload));
        }, 400L);
    }
}

