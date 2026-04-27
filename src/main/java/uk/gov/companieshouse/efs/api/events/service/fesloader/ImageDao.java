package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao {

    private JdbcTemplate jdbc;

    public ImageDao(@Qualifier("fesJdbc") final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextImageId() {
        Long result = jdbc.queryForObject("SELECT IMAGE_ID_SEQ.nextval FROM dual", Long.class);
        if (result == null) {
            throw new IllegalStateException("No value returned for next image id");
        }
        return result;
    }

    public void insertImage(final long imageId, final byte[] image) {
        jdbc.update("""
                INSERT INTO image(
                    IMAGE_ID,
                    IMAGE_IMAGE
                )
                VALUES(?, ?)
                """, imageId, image);
    }
}
