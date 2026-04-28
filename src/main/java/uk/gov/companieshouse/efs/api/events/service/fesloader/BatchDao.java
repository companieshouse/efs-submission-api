package uk.gov.companieshouse.efs.api.events.service.fesloader;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BatchDao {

    private JdbcTemplate jdbc;

    public BatchDao(@Qualifier("fesJdbc") final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextBatchId() {
        Long result = jdbc.queryForObject("SELECT BATCH_ID_SEQ.nextval from dual", Long.class);
        if (result == null) {
            throw new IllegalStateException("No value returned for next batch id");
        }
        return result;
    }

    public long getBatchNameId(String batchNamePrefix) {
        Long result = jdbc.queryForObject("SELECT fes_common_pkg.F_GETNEXTREFID(?, ?) from DUAL", Long.class,
            batchNamePrefix, 16);
        if (result == null) {
            throw new IllegalStateException("No value returned for batch name id");
        }
        return result;
    }

    /**
     * Insert new batch into Batch table.
     *
     * @param batchId   next batch id
     * @param batchName EFS batch name
     * @param timestamp timestamp
     */
    public void insertBatch(final long batchId, final String batchName, final LocalDateTime timestamp) {
        final var INSERT_SQL = """
            insert into BATCH(BATCH_ID, BATCH_SCANNED, BATCH_STATUS_ID, BATCH_SCANNER_NAME, 
            BATCH_SCAN_PERSON, BATCH_NAME, BATCH_SCANNED_LOCATION) 
            values(?,?,?,?,?,?,?)""";

        jdbc.update(INSERT_SQL, batchId, Timestamp.valueOf(timestamp), 1, "efs_batch", "efs_filing", batchName, 1);
    }

}
