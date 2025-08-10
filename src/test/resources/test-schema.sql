-- SQL schéma pre H2 databázu, ktorá musí presne sedieť k JPA entitám
-- Každý stĺpec musí mať presne taký názov, aký očakáva JPA mapovanie

-- Tabuľka pre Animal hierarchy (SINGLE_TABLE inheritance)
CREATE TABLE ANIMALS (
    -- BaseEntity fields
    ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    VERSION INTEGER,
    CUSTOM_CREATED_AT TIMESTAMP,
    SIMPLE_FIELD VARCHAR(255),
    XMLPARSER VARCHAR(255),
    HTMLELEMENT VARCHAR(255),
    USER_ID VARCHAR(255),
    FIRST_NAME2 VARCHAR(255),
    VERSION2_BETA VARCHAR(255),
    
    -- Discriminator column
    ANIMAL_TYPE VARCHAR(31) NOT NULL,
    
    -- Animal fields  
    NAME VARCHAR(255),
    SCIENTIFIC_NAME VARCHAR(255),
    AGE INTEGER,
    WEIGHT DOUBLE,
    
    -- Embedded Address habitat with AttributeOverrides
    HABITAT_STREET VARCHAR(255),
    HABITAT_CITY VARCHAR(255),
    ZIP_CODE VARCHAR(255),
    COUNTRY_CODE VARCHAR(255),
    
    -- Dog specific fields
    BREED VARCHAR(255),
    IS_VACCINATED BOOLEAN,
    OWNER_ID VARCHAR(255),
    VETERINARIAN_ID BIGINT,
    
    -- Dog embedded homeAddress (prefix: home_address_)
    HOME_ADDRESS_STREET VARCHAR(255),
    HOME_ADDRESS_CITY_NAME VARCHAR(255),  -- Note: uses Address @Column mapping
    HOME_ADDRESS_ZIP_CODE VARCHAR(255),
    HOME_ADDRESS_COUNTRY_CODE VARCHAR(255),
    
    -- Cat specific fields
    IS_INDOOR BOOLEAN,
    FAVORITE_FOOD VARCHAR(255),
    LIVES_REMAINING INTEGER,
    FAVORITE_TOY_ID BIGINT
);

-- Tabuľka pre Veterinarian
CREATE TABLE VETERINARIANS (
    ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    FIRST_NAME VARCHAR(255),
    LAST_NAME VARCHAR(255),
    LICENSE_NUMBER VARCHAR(255),
    PHONE_NUM VARCHAR(255),
    GRADUATION_DATE DATE,
    
    -- Embedded clinicAddress (prefix: clinic_address_)
    CLINIC_ADDRESS_STREET VARCHAR(255),
    CLINIC_ADDRESS_CITY_NAME VARCHAR(255),  -- Note: uses Address @Column mapping
    CLINIC_ADDRESS_ZIP_CODE VARCHAR(255),
    CLINIC_ADDRESS_COUNTRY_CODE VARCHAR(255)
);

-- Tabuľka pre CatToy
CREATE TABLE CAT_TOYS (
    ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    NAME VARCHAR(255),
    MATERIAL VARCHAR(255),
    PRICE DOUBLE,
    IS_SQUEAKY BOOLEAN
);

-- Tabuľka pre ComplexEntity - testuje všetky Spring Boot konvencie
CREATE TABLE COMPLEX_ENTITIES (
    ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Spring Boot naming convention tests
    CAMEL_CASE VARCHAR(255),
    XMLHTTP_REQUEST VARCHAR(255),
    HTML_PARSER VARCHAR(255),
    URL_PATH VARCHAR(255),
    USER_IDNUMBER VARCHAR(255),
    VERSION2_BETA3 VARCHAR(255),
    GET_NAME VARCHAR(255),
    SET_URL VARCHAR(255),
    
    OPTIMISTIC_LOCK_VERSION INTEGER,
    CUSTOM_NAME VARCHAR(100) NOT NULL,
    
    -- Embedded primaryAddress (prefix: primary_address_)
    PRIMARY_ADDRESS_STREET VARCHAR(255),
    PRIMARY_ADDRESS_CITY_NAME VARCHAR(255),  -- Note: uses Address @Column mapping
    PRIMARY_ADDRESS_ZIP_CODE VARCHAR(255),
    PRIMARY_ADDRESS_COUNTRY_CODE VARCHAR(255),
    
    -- Embedded billingAddress with AttributeOverrides
    BILLING_STREET_NAME VARCHAR(255),
    BILLING_CITY_NAME VARCHAR(255),
    BILLING_ZIP VARCHAR(255),
    BILLING_COUNTRY VARCHAR(255),
    
    -- Relations
    ASSIGNED_VET_ID BIGINT,
    
    -- Timestamps
    CREATED_TIMESTAMP TIMESTAMP,
    MODIFIED_TIMESTAMP TIMESTAMP
);

-- Tabuľka pre ComplexEntityDetails
CREATE TABLE COMPLEX_ENTITY_DETAILS (
    ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    DETAILED_DESCRIPTION VARCHAR(255),
    TECHNICAL_SPECS VARCHAR(255),
    IS_ACTIVE BOOLEAN,
    COMPLEX_ENTITY_ID BIGINT
);

-- Note: Foreign key constraints removed for H2 compatibility in testing
-- They are not needed for schema comparison tests