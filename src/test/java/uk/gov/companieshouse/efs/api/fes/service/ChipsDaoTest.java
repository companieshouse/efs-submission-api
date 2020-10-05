package uk.gov.companieshouse.efs.api.fes.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.companieshouse.efs.api.fes.repository.ChipsDao;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChipsDaoTest {
    private ChipsDao chipsDao;

    @Mock
    private JdbcTemplate template;

    @BeforeEach
    void setUp() {
        this.chipsDao = new ChipsDao(template);
    }

    @Test
    void testChipsDaoReturnsListOfString() {
        //given
        when(template.queryForList(anyString(), eq(String.class), anyString())).thenReturn(Collections.singletonList("Presenter is a teapot"));

        //when
        List<String> actual = chipsDao.readRejectReasonsForBarcode("Y123XYZ");

        //then
        assertEquals(Collections.singletonList("Presenter is a teapot"), actual);
        verify(template).queryForList(anyString(), eq(String.class), eq("Y123XYZ"));
    }
}
