package uk.gov.companieshouse.efs.api.events.service.fesloader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchDaoTest {
    private static final long BATCH_ID = 1L;
    private static final long BATCH_NAME_ID = 2L;
    private static final String BATCH_NAME = "EFS_200512_0001";

    @Mock
    private JdbcTemplate template;

    private BatchDao batchDao;

    @BeforeEach
    void setUp() {
        this.batchDao = new BatchDao(template);
    }

    @Test
    void testBatchDaoObtainsNextBatchIdFromSequence() {
        //given
        when(this.template.queryForObject(anyString(), eq(Long.class))).thenReturn(BATCH_ID);

        //when
        long actual = this.batchDao.getNextBatchId();

        //then
        assertEquals(BATCH_ID, actual);
        verify(this.template).queryForObject("SELECT BATCH_ID_SEQ.nextval from dual", Long.class);
    }

    @Test
    void testBatchDaoObtainsNextBatchNameIdFromSequence() {
        //given
        when(this.template.queryForObject(anyString(), eq(Long.class), any())).thenReturn(BATCH_NAME_ID);

        //when
        long actual = this.batchDao.getBatchNameId("EFS_200511");

        //then
        assertEquals(BATCH_NAME_ID, actual);
    }

    @Test
    void testBatchDaoInsertsNewBatchRecord() {
        //given
        LocalDateTime now = LocalDateTime.of(2020, 5, 12, 12, 0);

        //when
        this.batchDao.insertBatch(BATCH_ID, BATCH_NAME, now);

        //then
        verify(this.template).update(anyString(), eq(BATCH_ID), eq(Timestamp.valueOf(now)), eq(1), eq("efs_batch"), eq("efs_filing"), eq(BATCH_NAME), eq(1));
    }
}
