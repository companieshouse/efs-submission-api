package uk.gov.companieshouse.efs.api.events.service.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.api.formtemplates.model.FormTemplate;

@ExtendWith(MockitoExtension.class)
class FesLoaderModelTest {
    private FesLoaderModel testModel;
    private List<FesFileModel> fileModelList;
    private LocalDateTime now;
    private FesFileModel tiffFile;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        tiffFile = new FesFileModel(new byte[] {0x70}, 1);
        fileModelList = Collections.singletonList(tiffFile);
        testModel = new FesLoaderModel("barcode", "company-name", "company-number", "form", false,
            fileModelList, now);
    }

    @Test
    void constructor() {
        assertThat(testModel.getFormType(), is("form"));
        assertThat(testModel.getCompanyName(), is("company-name"));
        assertThat(testModel.getCompanyNumber(), is("company-number"));
        assertThat(testModel.getBarcode(), is("barcode"));
        assertThat(testModel.getBarcodeDate(), is(now));
        assertThat(testModel.isSameDay(), is(false));
        assertThat(testModel.getTiffFiles(), contains(tiffFile));
    }

    @Test
    void setTiffFiles() {
        testModel.setTiffFiles(null);
        assertThat(testModel.getTiffFiles(), is(nullValue()));
    }

    @Test
    void setBarcodeDate() {
        testModel.setBarcodeDate(null);
        assertThat(testModel.getBarcodeDate(), is(nullValue()));
    }

    @Test
    void setBarcode() {
        testModel.setBarcode(null);
        assertThat(testModel.getBarcode(), is(nullValue()));
    }

    @Test
    void setCompanyName() {
        testModel.setCompanyName(null);
        assertThat(testModel.getCompanyName(), is(nullValue()));
    }

    @Test
    void setCompanyNumber() {
        testModel.setCompanyNumber(null);
        assertThat(testModel.getCompanyNumber(), is(nullValue()));
    }

    @Test
    void setFormType() {
        testModel.setFormType(null);
        assertThat(testModel.getFormType(), is(nullValue()));
    }

    @Test
    void setSameDay() {
        testModel.setSameDay(true);
        assertThat(testModel.isSameDay(), is(true));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(FesLoaderModel.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }
}