package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EnvelopeDao {

    private JdbcTemplate jdbc;

    @Autowired
    public EnvelopeDao(@Qualifier("fesJdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextEnvelopeId() {
        return jdbc.queryForObject("SELECT ENVELOPE_ID_SEQ.nextval FROM dual", Long.class);
    }

    public void insertEnvelope(long envelopeId, long batchId) {
        jdbc.update("insert into ENVELOPE(ENVELOPE_ID, ENVELOPE_BATCH_ID) " + "values(?,?)",
            envelopeId, batchId);
    }

}
