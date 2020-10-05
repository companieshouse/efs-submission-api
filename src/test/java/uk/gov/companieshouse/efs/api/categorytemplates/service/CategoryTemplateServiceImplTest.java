package uk.gov.companieshouse.efs.api.categorytemplates.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants.OTHER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.api.categorytemplates.mapper.CategoryTemplateMapper;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTemplate;
import uk.gov.companieshouse.efs.api.categorytemplates.model.CategoryTypeConstants;
import uk.gov.companieshouse.efs.api.categorytemplates.repository.CategoryTemplateRepository;

@ExtendWith(MockitoExtension.class)
class CategoryTemplateServiceImplTest {

    @Mock
    private CategoryTemplateRepository categoryRepository;

    @Mock
    private CategoryTemplateMapper mapper;

    @Mock
    private CategoryTemplateListApi categoryList;

    @Mock
    private CategoryTemplate categoryTemplate;

    private CategoryTemplateServiceImpl service;

    @BeforeEach
    void setUp() {
        this.service = new CategoryTemplateServiceImpl(categoryRepository, mapper);
    }

    @Test
    void testFetchEntityAndMapToCategoryListApiObject() {
        //given
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(categoryTemplate));
        when(mapper.map(anyList())).thenReturn(categoryList);

        //when
        CategoryTemplateListApi actual = service.getCategoryTemplates();

        //then
        assertThat(actual, is(equalTo(categoryList)));
    }

    @Test
    void testFetchEntityWhenCategoryListApiObjectNotFound() {

        //when
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        CategoryTemplateListApi actual = service.getCategoryTemplates();

        //then
        assertThat(actual, is(nullValue()));
    }

    @Test
    void testFetchEntityAndMapToCategoryTemplateApiObject() {

        //given
        String categoryId = "CC01";
        CategoryTemplate category = new CategoryTemplate(categoryId, "CatCat", null, null);
        CategoryTemplateApi mappedCategory = new CategoryTemplateApi(categoryId, "CatCat", null, null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(mapper.map(category)).thenReturn(mappedCategory);

        //when
        CategoryTemplateApi actual = service.getCategoryTemplate(categoryId);

        //then
        assertThat(actual, is(equalTo(mappedCategory)));
    }

    @Test
    void testFetchEntityWhenCategoryTemplateApiObjectNotFound() {

        //given
        String categoryId = "CC01";

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        //when
        CategoryTemplateApi actual = service.getCategoryTemplate(categoryId);

        //then
        assertThat(actual, is(nullValue()));
        verifyNoInteractions(mapper);
    }

    @Test
    void testFetchEntityAndMapToCategoryTemplateListApiObjectByCategory() {

        //given
        String categoryId = "CC01";
        CategoryTemplate mappedCategory = new CategoryTemplate(categoryId, "CatCat", null, null);

        List<CategoryTemplate> listCategory = new ArrayList<>();
        listCategory.add(mappedCategory);

        when(categoryRepository.findByParent(categoryId)).thenReturn(listCategory);
        when(mapper.map(anyList())).thenReturn(categoryList);

        //when
        CategoryTemplateListApi actual = service.getCategoryTemplatesByCategory(categoryId);

        //then
        assertThat(actual, is(equalTo(categoryList)));
    }

    @ParameterizedTest
    @CsvSource({"RP,'','',OTHER", "CC,'','',CC", "IA1986,INS,'',INS"})
    void getTopLevelCategory(final String category, final String parent, final String grandParent,
        final String result) {
        //given
        final CategoryTypeConstants expected = CategoryTypeConstants.nameOf(result).orElse(OTHER);

        when(categoryRepository.findById(anyString())).thenReturn(Optional.of(categoryTemplate));
        when(categoryTemplate.getParent()).thenReturn(parent).thenReturn(grandParent);

        //when
        final CategoryTypeConstants topLevelCategory = service.getTopLevelCategory(category);

        //then
        assertThat(topLevelCategory, is(expected));
    }

    @Test
    void getTopLevelCategoryWhenNull() {
        //when
        final CategoryTypeConstants topLevelCategory = service.getTopLevelCategory(null);

        //then
        assertThat(topLevelCategory, is(nullValue()));
    }
}