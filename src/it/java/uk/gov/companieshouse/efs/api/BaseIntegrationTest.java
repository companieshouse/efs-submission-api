package uk.gov.companieshouse.efs.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import javax.sql.DataSource;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = EfsApiApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class BaseIntegrationTest {

    private static final String DOCKER_COMPOSE_FILE = "src/test/resources/docker-compose.yml";

    private static final String ORACLE_SERVICE_NAME = "efs-oracle_1";
    private static final int ORACLE_SERVICE_PORT = 1521;
    private static final String ORACLE_JDBC_URL_PATTERN = "jdbc:oracle:thin:@%s:%d:xe";
    private static final String ORACLE_USERNAME = "system";
    private static final String ORACLE_PASSWORD = "oracle";
    private static final String SQL_INIT_SCRIPT = "init.sql";

    private static final String MONGODB_SERVICE_NAME = "efs-mongodb_1";
    private static final int MONGODB_SERVICE_PORT = 27017;
    private static final String MONGODB_URL_PATTERN = "mongodb://%s:%d/test";
    private static final String SUBMISSION_COLLECTION_NAME = "submissions";
    private static final String FORM_TEMPLATES_COLLECTION_NAME = "form_templates";
    private static final String CATEGORY_TEMPLATES_COLLECTION_NAME = "category_templates";
    private static final String CATEGORY_TEMPLATES_FILE = "category_templates.json";
    private static final String FORM_TEMPLATES_FILE = "form_templates.json";

    private static final String ZOOKEEPER_SERVICE_NAME = "efs-zookeeper_1";
    private static final String KAFKA_SERVICE_NAME = "efs-kafka_1";
    private static final String SCHEMA_REGISTRY_SERVICE_NAME = "efs-confluent_1";
    private static final int ZOOKEEPER_SERVICE_PORT = 2181;
    private static final int KAFKA_SERVICE_PORT = 39092;
    private static final int SCHEMA_REGISTRY_SERVICE_PORT = 8081;
    private static final String KAFKA_URL_PATTERN = "%s:%d";
    private static final String KAFKA_EMAIL_TOPIC_NAME = "email-send";
    private static final String HTTP_HOSTNAME_AND_PORT = "http://%s:%d";
    private static final String EMAIL_SEND_FETCH_SCHEMA_PATH = "/subjects/email-send/versions/latest";
    private static final String SEND_EMAIL_SCHEMA_FILE = "/test-sendEmailSchema.json";
    private static final String SCHEMA_REGISTRY_EMAIL_SEND_POST_URL_PATTERN = "http://%s:%d/subjects/email-send/versions";

    private static final String MOCKSERVER_SERVICE_NAME = "efs-mock-server_1";
    private static final int MOCKSERVER_SERVICE_PORT = 1080;
    private static final String BARCODE_GENERATOR_URL_PATTERN = "http://%s:%d/barcode";
    private static final String FILE_TRANSFER_API_URL_PATTERN = "http://%s:%s/test/files";

    private static final String LOCALSTACK_SERVICE_NAME = "efs-localstack_1";
    private static final int LOCALSTACK_S3_SERVICE_PORT = 4572;
    private static final int LOCALSTACK_SQS_SERVICE_PORT = 4576;
    private static final String QUEUE_NAME = "queue1.fifo";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String LOCALSTACK_ACCESS_KEY = "accesskey";
    private static final String LOCALSTACK_SECRET_KEY = "secretkey";

    private static DockerComposeContainer<?> container = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_FILE))
            .withExposedService(ZOOKEEPER_SERVICE_NAME, ZOOKEEPER_SERVICE_PORT)
            .withExposedService(KAFKA_SERVICE_NAME, KAFKA_SERVICE_PORT)
            .withExposedService(SCHEMA_REGISTRY_SERVICE_NAME, SCHEMA_REGISTRY_SERVICE_PORT)
            .withExposedService(MONGODB_SERVICE_NAME, MONGODB_SERVICE_PORT)
            .withExposedService(MOCKSERVER_SERVICE_NAME, MOCKSERVER_SERVICE_PORT)
//            .withExposedService(ORACLE_SERVICE_NAME, ORACLE_SERVICE_PORT) // disabled until container image available
            .withExposedService(LOCALSTACK_SERVICE_NAME, LOCALSTACK_S3_SERVICE_PORT)
            .withExposedService(LOCALSTACK_SERVICE_NAME, LOCALSTACK_SQS_SERVICE_PORT);

    private static SqsClient sqsClient;
    private static S3Client s3Client;
    private static String queueUrl;
    private static RestTemplate restTemplate = new RestTemplate();
    private static MongoTemplate mongoTemplate;
    private static JdbcTemplate jdbcTemplate;

    static {
        container.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> container.stop()));
        initEnvironment();
    }

    @AfterEach
    protected void after() throws ExecutionException, InterruptedException {
        deleteKafkaOffsets();
    }

    public static SqsClient getSqsClient() {
        return sqsClient;
    }

    public static S3Client getS3Client() {
        return s3Client;
    }

    public static String getQueueUrl() {
        return queueUrl;
    }

    public static String getBucketName() {
        return BUCKET_NAME;
    }

    public static String getSubmissionCollectionName() {
        return SUBMISSION_COLLECTION_NAME;
    }

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public static MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public static void deleteKafkaOffsets() {
        try (AdminClient adminClient = KafkaAdminClient.create(Collections.singletonMap(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.format(KAFKA_URL_PATTERN, container.getServiceHost(KAFKA_SERVICE_NAME, KAFKA_SERVICE_PORT), KAFKA_SERVICE_PORT)))) {
            adminClient.deleteRecords(Collections.singletonMap(new TopicPartition(KAFKA_EMAIL_TOPIC_NAME, 0), RecordsToDelete.beforeOffset(1))).all().get();
        } catch (ExecutionException | InterruptedException e) {
            // do nothing; offsets may not have been published
        }
    }

    public static boolean newKafkaOffsetsHaveBeenPublished() {
        return TestKafkaConsumer.hasEntries(String.format(KAFKA_URL_PATTERN, container.getServiceHost(KAFKA_SERVICE_NAME, KAFKA_SERVICE_PORT), KAFKA_SERVICE_PORT), 10);
    }

    public static MockServerClient getMockServerClient() {
        return new MockServerClient(container.getServiceHost(MOCKSERVER_SERVICE_NAME, MOCKSERVER_SERVICE_PORT), container.getServicePort(MOCKSERVER_SERVICE_NAME, MOCKSERVER_SERVICE_PORT));
    }

    private static void initEnvironment() {
        initKafka();
        initREST();
        initMongo();
        initAWS();
//        initOracle();
    }

    private static void initREST() {
        System.setProperty("file.transfer.api.url", String.format(FILE_TRANSFER_API_URL_PATTERN, container.getServiceHost(MOCKSERVER_SERVICE_NAME, MOCKSERVER_SERVICE_PORT), container.getServicePort(MOCKSERVER_SERVICE_NAME, MOCKSERVER_SERVICE_PORT)));
        System.setProperty("barcode.generator.service.url", String.format(BARCODE_GENERATOR_URL_PATTERN, container.getServiceHost(MOCKSERVER_SERVICE_NAME, MOCKSERVER_SERVICE_PORT), container.getServicePort(MOCKSERVER_SERVICE_NAME, MOCKSERVER_SERVICE_PORT)));
    }

    private static void initOracle() {
        System.setProperty("fes.datasource.url", String.format(ORACLE_JDBC_URL_PATTERN, container.getServiceHost(ORACLE_SERVICE_NAME, ORACLE_SERVICE_PORT), container.getServicePort(ORACLE_SERVICE_NAME, ORACLE_SERVICE_PORT)));
        System.setProperty("fes.datasource.username", ORACLE_USERNAME);
        System.setProperty("fes.datasource.password", ORACLE_PASSWORD);
        System.setProperty("chips.datasource.url", String.format(ORACLE_JDBC_URL_PATTERN, container.getServiceHost(ORACLE_SERVICE_NAME, ORACLE_SERVICE_PORT), container.getServicePort(ORACLE_SERVICE_NAME, ORACLE_SERVICE_PORT)));
        System.setProperty("chips.datasource.username", ORACLE_USERNAME);
        System.setProperty("chips.datasource.password", ORACLE_PASSWORD);
        DataSource ds = new SingleConnectionDataSource(String.format(ORACLE_JDBC_URL_PATTERN, container.getServiceHost(ORACLE_SERVICE_NAME, ORACLE_SERVICE_PORT), container.getServicePort(ORACLE_SERVICE_NAME, ORACLE_SERVICE_PORT)), ORACLE_USERNAME, ORACLE_PASSWORD, true);
        new ResourceDatabasePopulator(new ClassPathResource(SQL_INIT_SCRIPT)).execute(ds);
        jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.execute("CREATE PACKAGE fes_common_pkg AS " +
                "FUNCTION f_getnextrefid(p_reference_key VARCHAR2, p_reference_group_type_id NUMBER) " +
                "RETURN NUMBER; " +
                "END fes_common_pkg;");
        jdbcTemplate.execute("CREATE PACKAGE BODY fes_common_pkg AS " +
                "FUNCTION f_getnextrefid(p_reference_key VARCHAR2, p_reference_group_type_id NUMBER) " +
                "RETURN NUMBER IS " +
                "result NUMBER(10); " +
                "BEGIN " +
                "SELECT 1 INTO result FROM DUAL; " +
                "RETURN(result); " +
                "END; " +
                "END fes_common_pkg;");
    }

    private static void initAWS() {
        System.setProperty("aws.access.key", LOCALSTACK_ACCESS_KEY);
        System.setProperty("aws.secret.key", LOCALSTACK_SECRET_KEY);
        System.setProperty("aws.region", "eu-west-2");
        initSQS();
        initS3();
    }

    private static void initS3() {
        String s3EndpointOverride = String.format(HTTP_HOSTNAME_AND_PORT, container.getServiceHost(LOCALSTACK_SERVICE_NAME, LOCALSTACK_S3_SERVICE_PORT), container.getServicePort(LOCALSTACK_SERVICE_NAME, LOCALSTACK_S3_SERVICE_PORT));
        System.setProperty("s3.endpoint.override", s3EndpointOverride);
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3EndpointOverride))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(LOCALSTACK_ACCESS_KEY, LOCALSTACK_SECRET_KEY)))
                .region(Region.EU_WEST_2)
                .build();
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(BUCKET_NAME)
                .build());
    }

    private static void initSQS() {
        String sqsEndpointOverride = String.format(HTTP_HOSTNAME_AND_PORT, container.getServiceHost(LOCALSTACK_SERVICE_NAME, LOCALSTACK_SQS_SERVICE_PORT), container.getServicePort(LOCALSTACK_SERVICE_NAME, LOCALSTACK_SQS_SERVICE_PORT));
        System.setProperty("sqs.endpoint.override", sqsEndpointOverride);
        sqsClient = SqsClient.builder()
                .endpointOverride(URI.create(sqsEndpointOverride))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(LOCALSTACK_ACCESS_KEY, LOCALSTACK_SECRET_KEY)))
                .region(Region.EU_WEST_2)
                .build();
        CreateQueueResponse createQueueResponse = sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName(QUEUE_NAME)
                .attributes(Collections.singletonMap(QueueAttributeName.FIFO_QUEUE, "true"))
                .build());
        queueUrl = createQueueResponse.queueUrl();
        System.setProperty("aws.sqs.queue.url", queueUrl);
    }

    private static void initMongo() {
        String mongoURI = String.format(MONGODB_URL_PATTERN, container.getServiceHost(MONGODB_SERVICE_NAME, MONGODB_SERVICE_PORT), container.getServicePort(MONGODB_SERVICE_NAME, MONGODB_SERVICE_PORT));
        System.setProperty("spring.data.mongodb.uri", mongoURI);
        mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoURI));
        mongoTemplate.createCollection(FORM_TEMPLATES_COLLECTION_NAME);
        mongoTemplate.createCollection(CATEGORY_TEMPLATES_COLLECTION_NAME);
        mongoTemplate.createCollection(SUBMISSION_COLLECTION_NAME);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(ClassLoader.getSystemClassLoader().getResourceAsStream(FORM_TEMPLATES_FILE)).forEach(node -> mongoTemplate.insert(Document.parse(node.toString()), FORM_TEMPLATES_COLLECTION_NAME));
            mapper.readTree(ClassLoader.getSystemClassLoader().getResourceAsStream(CATEGORY_TEMPLATES_FILE)).forEach(node -> mongoTemplate.insert(Document.parse(node.toString()), CATEGORY_TEMPLATES_COLLECTION_NAME));
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initKafka() {
        String kafkaURI = String.format(KAFKA_URL_PATTERN, container.getServiceHost(KAFKA_SERVICE_NAME, KAFKA_SERVICE_PORT), KAFKA_SERVICE_PORT);
        String kafkaSchemaRegistryBaseURL = String.format(HTTP_HOSTNAME_AND_PORT, container.getServiceHost(SCHEMA_REGISTRY_SERVICE_NAME, SCHEMA_REGISTRY_SERVICE_PORT), container.getServicePort(SCHEMA_REGISTRY_SERVICE_NAME, SCHEMA_REGISTRY_SERVICE_PORT));
        TestEnvironmentSetupHelper.setEnvironmentVariable("KAFKA_BROKER_ADDR", kafkaURI);
        System.setProperty("kafka.zookeeper.addr", kafkaURI);
        System.setProperty("kafka.schema.registry.url", kafkaSchemaRegistryBaseURL);
        System.setProperty("kafka.schema.uri.email-send", EMAIL_SEND_FETCH_SCHEMA_PATH);
        try (AdminClient adminClient = KafkaAdminClient.create(Collections.singletonMap(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaURI))) {
            adminClient.createTopics(Collections.singletonList(new NewTopic(KAFKA_EMAIL_TOPIC_NAME, 1, (short) 1))).all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        try {
            restTemplate.postForEntity(
                    String.format(SCHEMA_REGISTRY_EMAIL_SEND_POST_URL_PATTERN, container.getServiceHost(SCHEMA_REGISTRY_SERVICE_NAME, SCHEMA_REGISTRY_SERVICE_PORT), container.getServicePort(SCHEMA_REGISTRY_SERVICE_NAME, SCHEMA_REGISTRY_SERVICE_PORT)),
                    new HttpEntity<>(IOUtils.resourceToString(SEND_EMAIL_SCHEMA_FILE, StandardCharsets.UTF_8), CollectionUtils.toMultiValueMap(Collections.singletonMap(HttpHeaders.CONTENT_TYPE, Collections.singletonList("application/vnd.schemaregistry.v1+json")))), Void.class
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
