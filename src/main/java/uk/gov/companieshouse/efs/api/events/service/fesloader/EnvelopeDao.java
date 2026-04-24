package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EnvelopeDao {

    private JdbcTemplate jdbc;

    public EnvelopeDao(@Qualifier("fesJdbc") final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextEnvelopeId() {
        Long result = jdbc.queryForObject("SELECT ENVELOPE_ID_SEQ.nextval FROM dual", Long.class);
        if (result == null) {
            throw new IllegalStateException("No value returned for next envelope id");
        }
        return result;
    }

    public void insertEnvelope(final long envelopeId, final long batchId) {
        jdbc.update("insert into ENVELOPE(ENVELOPE_ID, ENVELOPE_BATCH_ID) " + "values(?,?)",
            envelopeId, batchId);
    }

}
