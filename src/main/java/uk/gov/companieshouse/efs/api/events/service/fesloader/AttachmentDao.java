package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AttachmentDao {

    private JdbcTemplate jdbc;

    @Autowired
    public AttachmentDao(@Qualifier("fesJdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextAttachmentId() {
        return jdbc.queryForObject("SELECT ATTACHMENT_ID_SEQ.nextval FROM dual", Long.class);
    }

    public void insertAttachment(long attachmentId, long formId, long attachmentTypeId, long imageId) {
        jdbc.update("INSERT INTO attachment(ATTACHMENT_ID, ATTACHMENT_FORM_ID, ATTACHMENT_TYPE_ID, ATTACHMENT_IMAGE_ID) " +
                        "VALUES(?,?,?,?)", attachmentId,formId, attachmentTypeId, imageId);
    }
}
