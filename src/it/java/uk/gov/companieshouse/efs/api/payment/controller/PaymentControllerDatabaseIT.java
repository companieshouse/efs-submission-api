package uk.gov.companieshouse.efs.api.payment.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplate;
import uk.gov.companieshouse.efs.api.payment.entity.PaymentTemplateId;
import uk.gov.companieshouse.efs.api.payment.repository.PaymentTemplateRepository;

@DataMongoTest
@EnableMongoRepositories("uk.gov.companieshouse.efs.api.payment.repository")
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = MongoDbTestContainerConfig.class)
class PaymentControllerDatabaseIT {
    private static final String FEE_ID = "FEE";
    private static final LocalDateTime NOW_LDT = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    private static final Logger log = LoggerFactory.getLogger(PaymentControllerDatabaseIT.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private PaymentTemplateRepository paymentTemplateRepository;
    private PaymentTemplate past;
    private PaymentTemplate present;
    private PaymentTemplate future;

    @BeforeEach
    void setUp() {
        past = createPaymentTemplate(FEE_ID, NOW_LDT.minusSeconds(1), "past");
        present = createPaymentTemplate(FEE_ID, NOW_LDT, "present");
        future = createPaymentTemplate(FEE_ID, NOW_LDT.plusHours(1),
            "future");
        mongoTemplate.save(past);
        mongoTemplate.save(present);
        mongoTemplate.save(future);

    }

    @Test
    void findByFeeWhenNoneExistThenEmptyList() {
        final List<PaymentTemplate> result =
            paymentTemplateRepository.findById_FeeOrderById_ActiveFromDesc(
                "no-such-fee");

        assertThat(result, is(empty()));
    }

    @Test
    void findByFeeWhenManyExistThenListAllNewestFirst() {
        final List<PaymentTemplate> result =
            paymentTemplateRepository.findById_FeeOrderById_ActiveFromDesc(
                FEE_ID);
        Assertions.assertEquals(3, result.size());
        assertThat(result, contains(future, present, past));
    }

    @Test
    void findFirstByFeeWhenNoneExistThenEmptyOptional() {
        final Optional<PaymentTemplate> result =
            paymentTemplateRepository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                "no-such-fee", NOW_LDT);

        assertThat(result, is(Optional.empty()));

    }

    @Test
    void findByFirstFeeWhenManyExistThenMostRecentNotInFuture() {
        final Optional<PaymentTemplate> result =
            paymentTemplateRepository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                FEE_ID, NOW_LDT);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getItems().get(0).getAmount(), is("present"));
    }

    @Test
    void findByFirstFeeWhenOnlyFutureExistThenEmptyOptional() {
        mongoTemplate.save(createPaymentTemplate("FUTURE_FEE", NOW_LDT.plusSeconds(1), "future"));

        final Optional<PaymentTemplate> result =
            paymentTemplateRepository.findFirstById_FeeAndId_ActiveFromLessThanEqualOrderById_ActiveFromDesc(
                "FUTURE_FEE", NOW_LDT);

        assertThat(result, is(Optional.empty()));
    }

    private PaymentTemplate createPaymentTemplate(final String feeId,
        final LocalDateTime activeFrom, final String amount) {
        return PaymentTemplate.newBuilder()
            .withId(new PaymentTemplateId(feeId, activeFrom))
            .withItem(PaymentTemplate.Item.newBuilder().withAmount(amount).build())
            .build();
    }
}
