package uk.gov.companieshouse.efs.api.filetransfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;

@Component
public class ApiClientUtil {

    private final InternalFileTransferClient internalFileTransferClient;

    @Autowired
    public ApiClientUtil(InternalFileTransferClient internalFileTransferClient) {
        this.internalFileTransferClient = internalFileTransferClient;
    }

    public InternalFileTransferClient getInternalFileTransferClient(final String fileTransferApiUrl ) {
        internalFileTransferClient.setBasePath( fileTransferApiUrl );
        return internalFileTransferClient;
    }

}