package uk.gov.companieshouse.efs.api.events.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ClientServiceTest {

    private static final String BUCKET_NAME = "TEST_BUCKET";

    private static final String ENV_NAME = "TEST_ENV";

    protected static final String SCOT_CONTENT = """
        submissionId,customerRef,userEmail,submittedAt,amountPaid,paymentRef,formType,companyNumber
        SCOT_FEE,REF_SF,presenter@nomail.net,2020-08-31T10:10:10,10,PAY_SF,SQP1,00000000
        """;

    @Mock
    private S3Presigner presigner;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    @Mock
    private S3Client s3Client;

    @Mock
    private PutObjectRequest putObjectRequest;

    @Mock
    private RequestBody requestBody;

    @Captor
    private ArgumentCaptor<GetObjectPresignRequest> captor;

    private S3ClientService spyService;

    @BeforeEach
    void setUp() {
        this.spyService = spy(new S3ClientService(presigner, 7L, s3Client, ENV_NAME));
    }

    @Test
    void testS3ClientServiceGeneratesLink() throws MalformedURLException, URISyntaxException {
        //given
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGetObjectRequest);
        when(presignedGetObjectRequest.url()).thenReturn(new URI("http://chs-dev.internal:4001").toURL());

        //when
        String actual = spyService.generateFileLink("12345678", BUCKET_NAME);

        //then
        assertEquals("http://chs-dev.internal:4001", actual);
        verify(presigner).presignGetObject(captor.capture());
        assertEquals("12345678", captor.getValue().getObjectRequest().key());
        assertEquals(BUCKET_NAME, captor.getValue().getObjectRequest().bucket());
        assertEquals(Duration.ofDays(7L), captor.getValue().signatureDuration());
    }

    @Test
    void testS3ClientLogsErrorIfPresignedLinkCannotBeGenerated() {
        //given
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenThrow(SdkException.class);

        //when
        Executable actual = () -> spyService.generateFileLink("12345678", BUCKET_NAME);

        //then
        assertThrows(SdkException.class, actual);
    }

    @Test
    void testUploadToS3() {
        final String reportName = "EFS_ScottishPaymentTransactions_2020-08-31.csv";

        doReturn(putObjectRequest).when(spyService).buildS3Request(reportName, BUCKET_NAME, "text/csv", "UTF-8");
        doReturn(requestBody).when(spyService).setS3BodyContent(SCOT_CONTENT);

        spyService.uploadToS3(reportName, SCOT_CONTENT, BUCKET_NAME);

        verify(s3Client).putObject(putObjectRequest, requestBody);
    }
}
