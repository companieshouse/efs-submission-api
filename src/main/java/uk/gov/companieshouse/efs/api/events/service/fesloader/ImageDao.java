package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao {

    private JdbcTemplate jdbc;

    @Autowired
    public ImageDao(@Qualifier("fesJdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long getNextImageId() {
        return jdbc.queryForObject("SELECT IMAGE_ID_SEQ.nextval FROM dual", Long.class);
    }

    public void insertImage(long imageId, byte[] image) {
        jdbc.update("INSERT INTO image(IMAGE_ID, IMAGE_IMAGE) VALUES(?, ?)", imageId, image);
    }
}
