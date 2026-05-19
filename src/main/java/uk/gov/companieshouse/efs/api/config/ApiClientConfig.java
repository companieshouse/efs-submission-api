package uk.gov.companieshouse.efs.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.handler.filetransfer.FileTransferHttpClient;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;

@Configuration
public class ApiClientConfig {

    @Value( "${chs.internal.api.key}" )
    private String internalApiKey;

    @Bean
    public InternalFileTransferClient getInternalFileClient(){
        return new InternalFileTransferClient( new FileTransferHttpClient( internalApiKey ) );
    }

}
