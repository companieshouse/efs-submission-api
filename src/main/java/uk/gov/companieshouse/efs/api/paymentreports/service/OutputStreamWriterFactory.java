package uk.gov.companieshouse.efs.api.paymentreports.service;

import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class OutputStreamWriterFactory {
    public OutputStreamWriter createFor(final BufferedOutputStream bufferedOutputStream) {
        return new OutputStreamWriter(bufferedOutputStream, StandardCharsets.UTF_8);
    }
}
