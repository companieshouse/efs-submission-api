package uk.gov.companieshouse.efs.api.categorytemplates.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryTemplateServiceTest {

    private class TestEFSServiceImpl implements CategoryTemplateService {

    }

    private TestEFSServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TestEFSServiceImpl();
    }

    @Test
    void getCategoryTemplates() {
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
                () -> testService.getCategoryTemplates());

        assertThat(thrown.getMessage(), is("not implemented"));
    }

    @Test
    void getCategoryTemplate() {
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
                () -> testService.getCategoryTemplate("CC01"));

        assertThat(thrown.getMessage(), is("not implemented"));
    }

    @Test
    void getCategoryTemplatesByCategory() {
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
                () -> testService.getCategoryTemplatesByCategory("CC01"));

        assertThat(thrown.getMessage(), is("not implemented"));
    }
    @Test
    void getTopLevelCategory() {
        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
                () -> testService.getTopLevelCategory("CC"));

        assertThat(thrown.getMessage(), is("not implemented"));
    }
}