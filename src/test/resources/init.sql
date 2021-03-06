CREATE TABLE BATCH(BATCH_ID NUMBER(10,0) NOT NULL,
        BATCH_SCANNED TIMESTAMP (6) NOT NULL,
        BATCH_STATUS_ID NUMBER(10,0) NOT NULL,
        BATCH_SCANNER_NAME VARCHAR2(50 CHAR) NOT NULL,
        BATCH_SCAN_PERSON VARCHAR2(50 CHAR) NOT NULL,
        BATCH_NAME VARCHAR2(40 CHAR) NOT NULL,
        BATCH_OPTLOCK NUMBER(10,0) DEFAULT 0,
        BATCH_SCANNED_LOCATION NUMBER(10,0),
        BATCH_PROCESSED_DATE TIMESTAMP (6),
        CONSTRAINT IDXPK_BATCH PRIMARY KEY (BATCH_ID));

CREATE TABLE ENVELOPE(ENVELOPE_ID NUMBER(10,0) NOT NULL,
        ENVELOPE_BATCH_ID NUMBER(10,0) NOT NULL,
        ENVELOPE_OPTLOCK NUMBER(10,0) DEFAULT 0,
        CONSTRAINT IDXPX_ENVELOPE PRIMARY KEY (ENVELOPE_ID));

CREATE TABLE IMAGE(IMAGE_ID NUMBER(10,0) NOT NULL,
        IMAGE_IMAGE BLOB,
        IMAGE_OPTLOCK NUMBER(10,0) DEFAULT 0,
        CONSTRAINT IDXPK_IMAGE PRIMARY KEY (IMAGE_ID));

CREATE TABLE FORM(FORM_ID NUMBER(10,0) NOT NULL, 
        FORM_BARCODE VARCHAR2(8 CHAR),
        FORM_INCORPORATION_NUMBER VARCHAR2(8 CHAR),
        FORM_CORPORATE_BODY_NAME VARCHAR2(160 CHAR),
        FORM_TYPE VARCHAR2(30 CHAR),
        FORM_COVERING_LETTER_ID NUMBER(10,0),
        FORM_IMAGE_ID NUMBER(10,0) NOT NULL,
        FORM_FEE_ENCLOSED VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL,
        FORM_ENVELOPE_ID NUMBER(10,0) NOT NULL,
        FORM_STATUS NUMBER(10,0) NOT NULL,
        FORM_PAGE_COUNT NUMBER(4,0) NOT NULL,
        FORM_OCR_FORM_TYPE VARCHAR2(20 CHAR),
        FORM_OCR_CORPORATE_BODY_NAME VARCHAR2(160 CHAR),
        FORM_OCR_INCORPORATION_NUMBER VARCHAR2(8 CHAR),
        FORM_OCR_BARCODE_1 VARCHAR2(20 CHAR),
        FORM_OCR_BARCODE_2 VARCHAR2(20 CHAR),
        FORM_OCR_BARCODE_3 VARCHAR2(20 CHAR),
        FORM_OCR_BARCODE_4 VARCHAR2(20 CHAR),
        FORM_USER_ID VARCHAR2(30 CHAR),
        FORM_ORG_UNIT_NAME VARCHAR2(30 CHAR),
        FORM_TRANSACTION_ID NUMBER(10,0),
        FORM_OPTLOCK NUMBER(10,0) DEFAULT 0,
        FORM_BARCODE_DATE DATE,
        FORM_SAME_DAY VARCHAR2(1 BYTE) DEFAULT 'N' NOT NULL,
        CONSTRAINT CHECKFEEENCLOSED CHECK (FORM_FEE_ENCLOSED='Y' OR FORM_FEE_ENCLOSED='N') ENABLE,
        CONSTRAINT IDXPK_FORM PRIMARY KEY (FORM_ID));

CREATE TABLE TRANSACTION(TRANSACTION_ID NUMBER(10,0) NOT NULL,
        TRANSACTION_TYPE_ID NUMBER(10, 0),
        TRANSACTION_STATUS_TYPE_ID NUMBER NOT NULL,
        FORM_BARCODE VARCHAR2(8 CHAR),
        CONSTRAINT IDXPK_TRAN PRIMARY KEY (TRANSACTION_ID));

CREATE TABLE TRANSACTION_TYPE(TRANSACTION_TYPE_ID NUMBER(10,0) NOT NULL,
        FORM_IND VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL,
        CONSTRAINT IDXPK_TRANTYP PRIMARY KEY (TRANSACTION_TYPE_ID));

CREATE TABLE TRANSACTION_QUERY_DECISION(TRANSACTION_ID NUMBER(10,0) NOT NULL,
        QUERY_ID NUMBER(10, 0) NOT NULL,
        DECISION_IND VARCHAR2(1 CHAR) NOT NULL,
        REASON_ENGLISH_TEXT VARCHAR2(4000 CHAR) NOT NULL,
        CONSTRAINT IDXPK_TRANQRYDSN PRIMARY KEY (TRANSACTION_ID, QUERY_ID));

INSERT INTO TRANSACTION(TRANSACTION_ID, TRANSACTION_TYPE_ID, TRANSACTION_STATUS_TYPE_ID, FORM_BARCODE) VALUES(1, 1, 10, 'Y9999999');
INSERT INTO TRANSACTION_TYPE(TRANSACTION_TYPE_ID, FORM_IND) VALUES(1, 'Y');
INSERT INTO TRANSACTION_QUERY_DECISION(TRANSACTION_ID, QUERY_ID, DECISION_IND, REASON_ENGLISH_TEXT) VALUES(1, 1, 'n', 'Invalid form');

CREATE SEQUENCE BATCH_ID_SEQ;
CREATE SEQUENCE ENVELOPE_ID_SEQ;
CREATE SEQUENCE FORM_ID_SEQ;
CREATE SEQUENCE IMAGE_ID_SEQ;
