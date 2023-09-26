package uk.gov.companieshouse.efs.api.payment.entity;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaymentTemplateTest {
    private static final String COMPANY_NUMBER = "00000000";
    public static final PaymentTemplateId TEMPLATE_ID =
            new PaymentTemplateId("SLPCS01 Test", Instant.parse("2019-01-08T00:00:00.000Z"));

    private PaymentTemplate testDetails;
    private PaymentTemplate.Item item;
    private PaymentTemplate.Links links;


    @BeforeEach
    void setUp() throws MalformedURLException {
        item = PaymentTemplate.Item.newBuilder().withAmount("100")
            .withAvailablePaymentMethods(Collections.singletonList("credit-card"))
            .withClassOfPayment(Collections.singletonList("data-maintenance"))
            .withDescription("Upload a form to Companies House").withDescriptionId("AMOUNT_TO_PAY")
            .withKind("cost#cost")
            .withProductType("efs-test").build();
        links = new PaymentTemplate.Links("http://resource.url", new URL("http://self.url"));
        testDetails = PaymentTemplate.newBuilder().withId(TEMPLATE_ID)
            .withDescription("Upload a form to Companies house")
            .withEtag("d8a936fc59fd43ba6c66363c25684be1964ea03d").withItem(item).withKind("cost"
                        + "#cost")
            .withLinks(links)
            .withPaymentReference("Test Charge")
            .withStatus(PaymentTemplate.Status.PENDING)
            .withCompanyNumber(COMPANY_NUMBER)
            .build();
    }

    @Test
    void buildCopy() {
        PaymentTemplate copy = PaymentTemplate.newBuilder(testDetails).build();

        assertThat(copy, is(not(sameInstance(testDetails))));
        assertThat(copy, is(equalTo(testDetails)));
        assertThat(copy.getItems(), is(not(sameInstance(testDetails.getItems()))));
    }

    @Test
    void setId() {
        testDetails.setId(TEMPLATE_ID);

        assertThat(testDetails.getId(), is(TEMPLATE_ID));
    }

    @Test
    void setDescription() {
        testDetails.setDescription("description");

        assertThat(testDetails.getDescription(), is("description"));
    }

    @Test
    void setEtag() {
        testDetails.setEtag("f88dd058fe004909615a64f01be66a7");

        assertThat(testDetails.getEtag(), is("f88dd058fe004909615a64f01be66a7"));
    }

    @Test
    void setItems() {
        final List<PaymentTemplate.Item> items = Collections.singletonList(item);

        testDetails.setItems(items);

        assertThat(testDetails.getItems(), is(equalTo(items)));
        assertThat(testDetails.getItems().get(0), is(not(sameInstance(item))));
    }

    @Test
    void buildWithItems() {
        testDetails =
            PaymentTemplate.newBuilder().withItems(Collections.singletonList(item)).build();

        assertThat(testDetails.getItems().get(0), is(item));
    }

    @Test
    void buildWithItemWhenItemsNotNull() {
        testDetails =
            PaymentTemplate.newBuilder().withItems(Collections.singletonList(item)).withItem(item)
                .build();

        assertThat(testDetails.getItems(), contains(item, item));
    }

    @Test
    void buildWithItemsWhenNull() {
        testDetails = PaymentTemplate.newBuilder().withItems(null).build();

        assertThat(testDetails.getItems(), is(Matchers.nullValue()));
    }

    @Test
    void setKind() {
        testDetails.setKind("kind");

        assertThat(testDetails.getKind(), is("kind"));
    }

    @Test
    void setLinks() throws MalformedURLException {
        final PaymentTemplate.Links expected = new PaymentTemplate.Links("resource", new URL(
                "http://self"));

        testDetails.setLinks(expected);

        assertThat(testDetails.getLinks(), is(expected));
    }

    @Test
    void setPaymentReference() {
        testDetails.setPaymentReference("reference");

        assertThat(testDetails.getPaymentReference(), is("reference"));
    }

    @Test
    void setStatus() {
        testDetails.setStatus(PaymentTemplate.Status.PENDING);

        assertThat(testDetails.getStatus(), is(PaymentTemplate.Status.PENDING));
    }

    @Test
    void equalsAndHashcode() {
        EqualsVerifier.forClass(PaymentTemplate.class)
                .usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    @Test
    void toStringTest() {
        assertThat(testDetails.toString(),
                //@formatter:off
            is("PaymentTemplate["
                + "id=PaymentTemplateId[fee=SLPCS01 Test,startTimestamp=2019-01-08T00:00:00Z],"
                + "description=Upload a form to Companies house,"
                + "etag=d8a936fc59fd43ba6c66363c25684be1964ea03d,"
                + "items=["
                +   "PaymentTemplate.Item["
                +     "amount=100,"
                +     "availablePaymentMethods=[credit-card],"
                +     "classOfPayment=[data-maintenance],"
                +     "description=Upload a form to Companies House,"
                +     "descriptionId=AMOUNT_TO_PAY,"
                +     "kind=cost#cost,"
                +     "productType=efs-test"
                +   "]"
                + "],"
                + "kind=cost#cost,"
                + "links=PaymentTemplate.Links[resource=http://resource.url,"
                + "self=http://self.url],"
                + "paymentReference=Test Charge,"
                + "status=pending,"
                + "companyNumber=00000000"
                + "]"));
            //@formatter:off
    }

    @Test
    void jsonRepresentation() throws JsonProcessingException {
        final String json = new ObjectMapper().writeValueAsString(testDetails);
        assertThat(json, allOf(
            //formatter:off
            containsString("id"), containsString("description"),
            containsString("etag"), containsString("items"),
            containsString("kind"), containsString("links"),
            containsString("payment_reference"),
            containsString("status"), containsString("company_number")));
        //formatter:on
    }

    @Test
    void itemBuildCopy() {
        PaymentTemplate.Item copy = PaymentTemplate.Item.newBuilder(item).build();

        assertThat(copy, is(equalTo(item)));
        assertThat(copy, is(not(sameInstance(item))));
    }

    @Test
    void itemSetAmount() {
        item.setAmount("1000");

        assertThat(item.getAmount(), is("1000"));
    }

    @Test
    void itemSetAvalilablePaymentMethods() {
        List<String> expected = Arrays.asList("cash", "cheque");
        item.setAvailablePaymentMethods(expected);

        assertThat(item.getAvailablePaymentMethods(), is(expected));
    }

    @Test
    void itemSetAvaliablePaymentMethodsWhenNull() {
        item.setAvailablePaymentMethods(null);

        assertThat(item.getAvailablePaymentMethods(), is(nullValue()));
    }

    @Test
    void itemSetClassOfPayment() {
        List<String> expected = Arrays.asList("class 1", "class 2");
        item.setClassOfPayment(expected);

        assertThat(item.getClassOfPayment(), is(expected));
    }

    @Test
    void itemSetClassOfPaymentWhenNull() {
        item.setClassOfPayment(null);

        assertThat(item.getClassOfPayment(), is(nullValue()));
    }

    @Test
    void itemSetDescription() {
        item.setDescription("description");

        assertThat(item.getDescription(), is("description"));
    }

    @Test
    void itemSetDescriptionId() {
        item.setDescriptionId("id");

        assertThat(item.getDescriptionId(), is("id"));
    }

    @Test
    void itemSetKind() {
        item.setKind("kind");

        assertThat(item.getKind(), is("kind"));
    }

    @Test
    void itemSetProductType() {
        item.setProductType("product");

        assertThat(item.getProductType(), is("product"));
    }

    @Test
    void itemEqualsAndHashcode() {
        EqualsVerifier.forClass(PaymentTemplate.Item.class).usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void itemToString() {
        assertThat(
            //@formatter:off
            item.toString(), is(
            "PaymentTemplate.Item["
                + "amount=100,"
                + "availablePaymentMethods=[credit-card],"
                + "classOfPayment=[data-maintenance],"
                + "description=Upload a form to Companies House,"
                + "descriptionId=AMOUNT_TO_PAY,"
                + "kind=cost#cost,"
                + "productType=efs-test"
                + "]"));
        //@formatter:on
    }

    @Test
    void itemJsonRepresentation() throws JsonProcessingException {
        final String json = new ObjectMapper().writeValueAsString(item);
        assertThat(json, allOf(
            //formatter:off
            containsString("amount"), containsString("available_payment_methods"),
            containsString("class_of_payment"), containsString("description"),
            containsString("description_identifier"), containsString("kind")));
        //formatter:on
    }

    @Test
    void linksSetResource() {
        links.setResource("resource");

        assertThat(links.getResource(), is("resource"));
    }

    @Test
    void linksSetSelf() throws MalformedURLException {
        final URL expected = new URL("http://self");

        links.setSelf(expected);

        assertThat(links.getSelf(), is(expected));
    }

    @Test
    void linksEqualsAndHashcode() {
        EqualsVerifier.forClass(PaymentTemplate.Links.class).usingGetClass()
            .suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void linksToString() {
        assertThat(links.toString(),
            //@formatter:off
            is("PaymentTemplate.Links["
               + "resource=http://resource.url,self=http://self.url]"));
        //@formatter:on
    }

    @Test
    void linksJsonRepresentation() throws JsonProcessingException {
        assertThat(new ObjectMapper().writeValueAsString(links), allOf(
            //@formatter:off
                containsString("resource"),
                containsString("self")));
        //@formatter:on
    }

    @Test
    void statusFromValue() {
        assertThat(PaymentTemplate.Status.fromValue("paid"), is(PaymentTemplate.Status.PAID));
    }

    @Test
    void statusFromValueWhenNoMatch() {
        assertThat(PaymentTemplate.Status.fromValue("PAID"), is(nullValue()));
    }

}