package uk.gov.companieshouse.efs.api.submissions.mapper;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.submissions.FileConversionStatus;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.efs.api.submissions.model.FileDetails;
import uk.gov.companieshouse.efs.api.util.CurrentTimestampGenerator;

@Component
public class FileDetailsMapper {

    private CurrentTimestampGenerator currentTimestampGenerator;

    @Autowired
    public FileDetailsMapper(CurrentTimestampGenerator currentTimestampGenerator) {
        this.currentTimestampGenerator = currentTimestampGenerator;
    }

    public List<FileDetails> map(FileListApi fileListApi) {
        return fileListApi.getFiles().stream()
                .map(fileDetailsApi -> new FileDetails(
                        fileDetailsApi.getFileId(),
                        fileDetailsApi.getFileName(),
                        fileDetailsApi.getFileSize(),
                        null,
                        FileConversionStatus.WAITING,
                        null,
                        currentTimestampGenerator.generateTimestamp().atZone(ZoneId.of("UTC")).toLocalDateTime())).collect(Collectors.toList());
    }

}
