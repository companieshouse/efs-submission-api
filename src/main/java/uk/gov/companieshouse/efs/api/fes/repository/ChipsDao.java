package uk.gov.companieshouse.efs.api.fes.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChipsDao {

    private JdbcTemplate template;

    @Autowired
    public ChipsDao(@Qualifier("chipsJdbc") JdbcTemplate template) {
        this.template = template;
    }

    /**
     * Retrieves english reject reasons.
     *
     * @param barcode   the barcode
     * @return          List of reject reasons
     */
    public List<String> readRejectReasonsForBarcode(String barcode) {
        return template.queryForList("SELECT tqt.reason_english_text FROM transaction t "
                                     + "INNER JOIN transaction_type tt ON tt.transaction_type_Id = t.transaction_type_Id AND form_ind='Y' "
                                     + "INNER JOIN transaction_query_decision tqt ON tqt.transaction_id = t.transaction_id "
                                     + "WHERE transaction_status_type_id = 10 "
                                     + "AND tqt.decision_ind IN ('n','f') "
                                     + "AND form_barcode = ?", String.class, barcode);
    }
}
