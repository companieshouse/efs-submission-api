package uk.gov.companieshouse.efs.api.fes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import uk.gov.companieshouse.efs.api.fes.repository.ChipsDao;
import uk.gov.companieshouse.efs.api.fes.service.exception.ChipsServiceException;

@ExtendWith(MockitoExtension.class)
class ChipsServiceImplTest {

    private ChipsServiceImpl chipsService;

    @Mock
    private ChipsDao chipsDao;

    @BeforeEach
    void setUp() {
        this.chipsService = new ChipsServiceImpl(chipsDao);
    }

    @Test
    void testReturnListOfRejectReasons() {
        //given
        when(chipsDao.readRejectReasonsForBarcode(anyString())).thenReturn(Collections.singletonList("Presenter's name is camelCase"));

        //when
        List<String> actual = chipsService.getRejectReasons("Y12345ABC");

        //then
        assertEquals(Collections.singletonList("Presenter's name is camelCase"), actual);
    }
    
    @Test
    void testChipsServiceExceptionIsCaught() {
        //given
        when(chipsDao.readRejectReasonsForBarcode(anyString())).thenThrow(new DuplicateKeyException("oops"));
        
        //when
        Executable actual = () -> chipsService.getRejectReasons("Y12345ABC");
        
        //then
        ChipsServiceException exception = assertThrows(ChipsServiceException.class, actual);
        assertEquals("Unable to read reject reasons from CHIPS", exception.getMessage());
    }
}
