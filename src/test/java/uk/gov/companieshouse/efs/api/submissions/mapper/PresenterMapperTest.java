package uk.gov.companieshouse.efs.api.submissions.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.efs.api.submissions.model.Presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenterMapperTest {

    private static final String PRESENTER_EMAIL = "demo@ch.gov.uk";

    private PresenterMapper mapper;

    @Mock
    private PresenterApi presenter;

    @BeforeEach
    void setUp() {
        this.mapper = new PresenterMapper();
    }

    @Test
    void testMapperMapsPresenterRequestEntityToDataObject() {
        //given
        when(presenter.getEmail()).thenReturn(PRESENTER_EMAIL);

        //when
        Presenter actual = mapper.map(presenter);

        //then
        assertEquals(expectedPresenter(), actual);
    }

    private Presenter expectedPresenter() {
        return new Presenter(PRESENTER_EMAIL);
    }
}
