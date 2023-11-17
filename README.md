# efs-submission-api

The Emergency Filing Service API is responsible for handling and processing EFS applications submitted by users.

API users (including [efs-submission-web](https://github.com/companieshouse/efs-submission-web)) interact with efs-submission-api by sending HTTP requests containing JSON to service endpoints. Service endpoints available in efs-submission-api as well as their expected request and response models are outlined in the [Swagger specification file](spec/swagger.json). 

The service integrates with a number of internal and external systems. This includes [FES](https://github.com/companieshouse/fes-control), [chs-notification-api](https://github.com/companieshouse/chs-notification-api) (via Kafka) and [efs-document-processor](https://github.com/companieshouse/efs-document-processor) (via AWS SQS) to notify applicants and internal users if/when an application has been submitted, converted, accepted or rejected.

Requirements
------------

To build efs-submission-api, you will need:
* [Git](https://git-scm.com/downloads)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](https://maven.apache.org/download.cgi)
* [MongoDB](https://www.mongodb.com/)
* [Apache Kafka](https://kafka.apache.org/)
* Internal Companies House core services

You will also need a REST client (e.g. Postman or cURL) if you want to interact with any efs-submission-api service endpoints.

Certain endpoints (e.g. POST /efs-submission-api/events/submit-files-to-fes) will not work correctly unless the relevant environment variables are configured. 

## Building and Running Locally

1. From the command line, in the same folder as the Makefile run `make clean build`
1. Configure project environment variables where necessary (see below).
1. Ensure dependent Companies House services are running within the Companies House developer environment
1. Start the service in the CHS developer environment
1. Send a GET request using your REST client to /efs-submission-api/healthcheck. The response should be 200 OK with status=UP.
1. A database named `efs_submissions` and the following collections are required:

|Collection name         |Description|Data|
-------------------------|---------------|------|
 submission              |id, dates, presenter, form & file details|this and the database will be created by the service upon starting a submission, then populated as the user enters their data|
 category_templates      |the 'groupings' for the forms|reference data manually populated from:<br>`src/main/resources/category_templates.json`|
 form_templates          |the form ids, names etc.|reference data manually populated from:<br>`src/main/resources/form_templates.json`|
 company_auth_allow_list |emails of IP's who are allowed to see and submit Insolvency forms|manually populated e.g.<br> `{"emailAddress": "demo@ch.gov.uk"}`|
 payment_charges         |payment templates required by payment service|reference data manually populated from:<br>`src/main/resources/payments_templates.json`|

Configuration
-------------
System properties for efs-submission-api are defined in `application.properties`. These are normally configured per environment.

Certain form types will be sent by efs-submission-api to FES while other form types will be emailed to the relevant org unit. Properties relevant to either scenario are labelled accordingly.

| Variable                                     | Description                                                                                                                                 | Example                                             |Mandatory (always, email, FES)|
----------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|--------|
 BARCODE_SERVICE_URL                          | The barcode service URL (used to generate barcodes for applications sent to FES)                                                            | http://example.com                                  |always
 FILE_TRANSFER_API_URL                        | The file transfer API URL (used to check the anti-virus status of uploaded PDF files)                                                       | http://example.com                                  |FES
 FILE_TRANSFER_API_KEY                        | The file transfer API key                                                                                                                   | MYTRANSFERAPIKEY                                    |FES
 MONGO_EFS_API_DB_NAME                        | The name of the collection responsible for storing EFS documents                                                                            | collection_name                                     |always
 MONGODB_URL                                  | The URL of the MongoDB instance where documents and application data should be stored                                                       | mongodb://mongohost:27017                           |always
 REF_PATTERN                                  | The pattern that randomly generated submission numbers will follow                                                                          | ############                                        |always
 REF_SYMBOL_SET                               | Set of characters permitted in randomly generated submission numbers                                                                        | abc123                                              |always
 MANAGEMENT_ENDPOINTS_ENABLED_BY_DEFAULT      |                                                                                                                                             | false                                               |always
 MANAGEMENT_ENDPOINT_HEALTH_ENABLED           |                                                                                                                                             | true                                                |always
 MANAGEMENT_ENDPOINTS_WEB_PATH_MAPPING_HEALTH |                                                                                                                                             | healthcheck                                         |always
 MANAGEMENT_ENDPOINTS_WEB_BASE_PATH           |                                                                                                                                             | /efs-submission-api                                 |always
 LOGGING_LEVEL                                | Log message granularity                                                                                                                     | INFO                                                |always
 REQUEST_LOGGING_LEVEL                        | Request log message granularity                                                                                                             | WARN                                                |always
 EFS_MAX_QUEUE_MESSAGES                       | Maximum number of submissions that will be retrieved by /efs-submission-api/queue-files                                                     | 50                                                  |FES
 AWS_SQS_QUEUE_URL                            | URL of a FIFO SQS queue that file conversion requests will be published to                                                                  | http://example.com                                  |FES
 KAFKA_CONFIG_RETRIES                         |                                                                                                                                             | 5                                                   |always
 KAFKA_CONFIG_IS_ROUND_ROBIN                  |                                                                                                                                             | true                                                |always
 KAFKA_CONFIG_ACKS                            |                                                                                                                                             | WAIT_FOR_ALL                                        |always
 EMAIL_SCHEMA_URI                             |                                                                                                                                             | http://example.com                                  |always
 INTERNAL_REG_FUNC_EMAIL                      | The email address that will be used for registry power forms                                                                                | test_user@testing.com                               |always
 INTERNAL_CHANGE_CONSTITUTION_EMAIL_ADDRESS   | The email address that will be used for change constitution forms                                                                           | test_user@testing.com                               |always
 AWS_REGION                                   | The AWS region that efs-submission-api will use when connecting to AWS services                                                             | aws-region                                          |always
 FILE_BUCKET_NAME                             | The S3 bucket that uploaded PDF files will be stored                                                                                        | s3-bucket-name                                      |email
 AWS_ACCESS_KEY_ID                            | The access key ID of the AWS account that efs-submission-api will use when connecting to AWS                                                | MYAWSACCESSKEYID                                    |always
 AWS_SECRET_ACCESS_KEY                        | The secret access key of the AWS account that efs-submission-api will use when connecting to AWS                                            | MYAWSSECRETACCESSKEY                                |always
 EFS_MESSAGE_PARTITION_SIZE                   | The maximum number of messages that efs-submission-api will send to AWS SQS (must be less than 10)                                          | 10                                                  |FES
 TIFF_BUCKET_NAME                             | The S3 bucket that converted TIFF files will be retrieved from                                                                              | s3-bucket-name                                      |FES
 FES_JDBC_URL                                 | A JDBC URL referring to a FES database                                                                                                      | jdbc:oracle:thin@chd-feshostname:1521:fesdbname     |FES
 FES_JDBC_DRIVER_CLASS                        | The fully qualified class name of the driver that will be used to connect to FES                                                            | oracle.jdbc.OracleDriver                            |FES
 FES_JDBC_USERNAME                            | The username that will be used to connect to FES                                                                                            | username                                            |FES
 FES_JDBC_PASSWORD                            | The password that will be used to connect to FES                                                                                            | password                                            |FES
 CHIPS_JDBC_URL                               | A JDBC URL referring to a CHIPS database                                                                                                    | jdbc:oracle:thin@chd-chipshostname:1521:chipsdbname |FES
 CHIPS_JDBC_DRIVER_CLASS                      | The fully qualified class name of the driver that will be used to connect to CHIPS                                                          | oracle.jdbc.OracleDriver                            |FES
 CHIPS_JDBC_USERNAME                          | The username that will be used to connect to CHIPS                                                                                          | username                                            |FES
 CHIPS_JDBC_PASSWORD                          | The password that will be used to connect to CHIPS                                                                                          | password                                            |FES
 FILE_LINK_EXPIRY_IN_DAYS                     | The number of days after which email links to uploaded PDF files will expire                                                                | 7                                                   |email
 INTERNAL_SCOTTISH_PARTNERSHIPS_EMAIL_ADDRESS | The email address that will be used for Scottish Partnership forms                                                                          | test_user@testing.com                               |always
 PLANNED_MAINTENANCE_START_TIME               | Datetime for start of out-of-service period (exclusive). Only applicable if the period end is also configured.<br>Allowed format: see Notes | 2 Dec 23 01:00 GMT<br>14 July 24 00:30 +01<br>      |optional; requires End time (see below)
 PLANNED_MAINTENANCE_END_TIME                 | Datetime for end of out-of-service period (exclusive). Only applicable if the period start is also configured.<br>Allowed format: see Notes | 2 Dec 23 02:30 GMT<br>14 July 24 03:30 +01<br>      |optional; requires Start time (see above)
 PLANNED_MAINTENANCE_MESSAGE                  | Message to return during the out-of-service period|Service is undergoing planned maintenance|optional; default value *UNAVAILABLE - PLANNED MAINTENANCE*

### Notes

Planned maintenance format: `d MMM yy HH:mm z|x` where
- `MMM` is the 3-letter month abbrev. (case sensitive: e.g. `Nov` not `NOV`)
- `z` is the zone short name e.g. `GMT`
- `x` is the 2-digit zone offset from UTC e.g. `+01`  (= British Summer Time)

> **CAUTION**: Use zone offset *+01* for Daylight Saving Time (British Summer Time). Zone short name *BST* denotes Bangladesh Standard Time (UTC+06) not British Summer Time (UTC+01). 


## Building the docker image 

    mvn -s settings.xml compile jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-1.amazonaws.com/local/efs-submission-api

## Running Locally using Docker

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.

1. Enable the `efs` module

1. Run `tilt up` and wait for all services to start

### To make local changes

Development mode is available for this service in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable efs-submission-api

This will clone the efs-submission-api into the repositories folder inside of docker-chs-dev folder. Any changes to the code, or resources will automatically trigger a rebuild and reluanch.
