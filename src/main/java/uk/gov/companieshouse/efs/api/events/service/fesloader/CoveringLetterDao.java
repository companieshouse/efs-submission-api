package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CoveringLetterDao {

    private JdbcTemplate jdbc;

    @Autowired
    public CoveringLetterDao(@Qualifier("fesJdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextCoveringLetterId() {
        return jdbc.queryForObject("SELECT COVERING_LETTER_ID_SEQ.nextval FROM dual", Long.class);
    }

    public void insertCoveringLetter(long coveringLetterId, long envelopeId, long imageId, int pageCount) {
        jdbc.update("insert into COVERING_LETTER(COVERING_LETTER_ID, COVERING_LETTER_ENVELOPE_ID, COVERING_LETTER_IMAGE_ID, " +
                        "COVERING_LETTER_PAGE_COUNT) " + "values(?,?,?,?)",
            coveringLetterId, envelopeId, imageId, pageCount);
    }
}
