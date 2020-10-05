package uk.gov.companieshouse.efs.api.events.service.fesloader;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BatchDao {

    private JdbcTemplate jdbc;

    @Autowired
    public BatchDao(@Qualifier("fesJdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextBatchId() {
        return jdbc.queryForObject("SELECT BATCH_ID_SEQ.nextval from dual", Long.class);
    }

    public long getBatchNameId(String batchNamePrefix) {
        return jdbc.queryForObject("SELECT fes_common_pkg.F_GETNEXTREFID(?, ?) from DUAL",
                new Object[] { batchNamePrefix, 16 }, Long.class);
    }

    /**
     * Insert new batch into Batch table.
     *
     * @param batchId   next batch id
     * @param batchName EFS batch name
     * @param timestamp timestamp
     */
    public void insertBatch(long batchId, String batchName, LocalDateTime timestamp) {
        jdbc.update(
            "insert into BATCH(BATCH_ID, BATCH_SCANNED, BATCH_STATUS_ID, BATCH_SCANNER_NAME, "
            + "BATCH_SCAN_PERSON, BATCH_NAME, BATCH_SCANNED_LOCATION) "
            + "values(?,?,?,?,?,?,?)", batchId, Timestamp.valueOf(timestamp), 1, "efs_batch",
            "efs_filing", batchName, 1);
    }

}
