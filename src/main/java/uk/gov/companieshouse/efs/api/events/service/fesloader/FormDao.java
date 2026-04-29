package uk.gov.companieshouse.efs.api.events.service.fesloader;

import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.efs.api.events.service.model.FormModel;

@Repository
public class FormDao {
    private static final String FORM_INSERT_SQL = """
        INSERT INTO form(
            FORM_ID,
            FORM_BARCODE,
            FORM_INCORPORATION_NUMBER,
            FORM_CORPORATE_BODY_NAME,
            FORM_TYPE,
            FORM_IMAGE_ID,
            FORM_ENVELOPE_ID,
            FORM_STATUS,
            FORM_PAGE_COUNT,
            FORM_OCR_FORM_TYPE,
            FORM_OCR_CORPORATE_BODY_NAME,
            FORM_OCR_INCORPORATION_NUMBER,
            FORM_OCR_BARCODE_1,
            FORM_BARCODE_DATE,
            FORM_SAME_DAY
        )
        VALUES(FORM_ID_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
        """;

    private JdbcTemplate jdbc;

    public FormDao(@Qualifier("fesJdbc") final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertForm(final FormModel model) {
        this.jdbc.update(FORM_INSERT_SQL,
            model.getBarcode(),
            model.getCompanyNumber(),
            model.getCompanyName(),
            model.getFormType(),
            model.getImageId(),
            model.getEnvelopeId(),
            model.getFormStatus(),
            model.getNumberOfPages(),
            model.getFormType(),
            model.getCompanyName(),
            model.getCompanyNumber(),
            model.getBarcode(),
            Timestamp.valueOf(model.getBarcodeDate()),
            model.getSameDayIndicator());
    }
}
