package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.submissions.FileApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileDetailsMapperTest {

    private static final String FILE_ID = "1234";
    private static final String FILE_NAME = "HELLO.pdf";
    private static final long FILE_SIZE = 100L;

    private FileDetailsMapper mapper;

    @Mock
    private CurrentTimestampGenerator timestampGenerator;

    @Mock
    private FileListApi fileListApi;

    @Mock
    private FileApi file;

    @BeforeEach
    void setUp() {
        this.mapper = new FileDetailsMapper(timestampGenerator);
    }

    @Test
    void testFileDetailsMapperMapsFileDetailsRequestEntityToDataEntity() {
        //given
        LocalDateTime now = LocalDateTime.now();
        when(timestampGenerator.generateTimestamp()).thenReturn(now);
        when(fileListApi.getFiles()).thenReturn(Collections.singletonList(file));
        when(file.getFileId()).thenReturn(FILE_ID);
        when(file.getFileName()).thenReturn(FILE_NAME);
        when(file.getFileSize()).thenReturn(FILE_SIZE);

        //when
        List<FileDetails> actual = mapper.map(fileListApi);

        //then
        assertEquals(Collections.singletonList(expectedFileDetails(now)), actual);
    }

    private FileDetails expectedFileDetails(LocalDateTime now) {
        return new FileDetails(FILE_ID, FILE_NAME, FILE_SIZE, null, FileConversionStatus.WAITING, null, now);
    }
}
