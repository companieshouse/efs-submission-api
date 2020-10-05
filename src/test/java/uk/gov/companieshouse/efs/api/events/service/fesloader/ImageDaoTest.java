package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageDaoTest {

    private static final long IMAGE_ID = 1L;
    private ImageDao imageDao;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        this.imageDao = new ImageDao(jdbcTemplate);
    }

    @Test
    void testImageDaoReturnsIdFromSequence() {
        //given
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(IMAGE_ID);

        //when
        long actual = imageDao.getNextImageId();

        //then
        assertEquals(IMAGE_ID, actual);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class));
    }

    @Test
    void testImageDaoInsertsNewImage() {
        //when
        imageDao.insertImage(IMAGE_ID, "Hello".getBytes());

        //then
        verify(jdbcTemplate).update(anyString(), eq(IMAGE_ID), eq("Hello".getBytes()));
    }
}
