-- There is no need to create a table space, it is stored in the default by default
CREATE USER salesmanBuddyUser;
CREATE DATABASE salesmanBuddy;
GO
--ALTER DATABASE salesmanBuddy MODIFY FILE(
--    NAME=N'salesmanBuddy',
--    SIZE=10MB,
--    MAXSIZE=5GB,
--    FILEGROWTH=10MB
--);
--GO
--ALTER DATABASE salesmanBuddy MODIFY FILE(
--    NAME=N'salesmanBuddy_log',
--    SIZE=1MB,
--    MAXSIZE=5GB,
--    FILEGROWTH=10%
--);
--GO-- there are errors here
CREATE LOGIN salesmanBuddyServer WITH PASSWORD=N'salesmanbuddyserverpw';
CREATE USER salesmanBuddyServer FOR LOGIN salesmanBuddyServer;
GRANT CREATE SESSION TO salesmanBuddyServer;
GRANT CREATE TABLE TO salesmanBuddyServer;
GRANT CREATE TRIGGER TO salesmanBuddyServer;

USE salesmanBuddy;



CREATE TABLE states (
    id                       int                      IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name                     NVARCHAR(30)                           NOT NULL,
    status                   NUMERIC(2) default 0                   NOT NULL
);

CREATE TABLE dealerships (
    id                       int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name                     NVARCHAR(100)                         NOT NULL,
    city                     NVARCHAR(100)                         NOT NULL,
    stateId                  int                                   NOT NULL FOREIGN KEY REFERENCES states(id),
    created                  DATETIME2    default SYSUTCDATETIME() NOT NULL,
);

CREATE TABLE users (
    id                       int                       IDENTITY(1,1) NOT NULL PRIMARY KEY,
    dealershipId             int                                     NOT NULL FOREIGN KEY REFERENCES dealerships(id),
    deviceType               int                                     NOT NULL,
    type                     NUMERIC(3)     default 1                NOT NULL,
    created                  DATETIME2      default SYSUTCDATETIME() NOT NULL,
    googleUserId             NVARCHAR(25)                            NOT NULL
);

CREATE TABLE buckets (
    id                          int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    stateId                     int                                   NOT NULL FOREIGN KEY REFERENCES states(id),
    name                        NVARCHAR(100)                         NOT NULL,
    created                     DATETIME2    default SYSUTCDATETIME() NOT NULL
);

CREATE TABLE licenses (
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    showInUserList             NUMERIC(2)  default 1                 NOT NULL,
    photo                      NVARCHAR(30)                          NOT NULL,
    bucketId                   int                                   NOT NULL FOREIGN KEY REFERENCES buckets(id),
    created                    DATETIME2   default SYSUTCDATETIME()  NOT NULL,
    longitude                  decimal(10, 6)                        NOT NULL, -- sub meter accuracy
    latitude                   decimal(10, 6)                        NOT NULL,
    userId                     int                                   NOT NULL FOREIGN KEY REFERENCES users(id)
);

CREATE TABLE contactInfo(--http://stackoverflow.com/questions/20958/list-of-standard-lengths-for-database-fields
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    userId                     int                                   NOT NULL FOREIGN KEY REFERENCES users(id),
    licenseId                  int                                   NOT NULL FOREIGN KEY REFERENCES licenses(id),
    created                  DATETIME2      default SYSUTCDATETIME() NOT NULL,
    firstName                  NVARCHAR(50)                          NULL,
    lastName                   NVARCHAR(50)                          NULL,
    email                      NVARCHAR(255)                         NULL,
    phoneNumber                NVARCHAR(15)                          NULL,
    streetAddress              NVARCHAR(100)                         NULL,
    city                       NVARCHAR(40)                          NULL,
    stateId                    int                                   NULL FOREIGN KEY REFERENCES states(id),
    notes                      NVARCHAR(500)                         NULL
);

CREATE TABLE stateQuestions (
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    stateId                    int                                   NOT NULL FOREIGN KEY REFERENCES states(id),
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL
);

CREATE TABLE stateQuestionsSpecifics (
    id                          int               IDENTITY(1,1) NOT NULL PRIMARY KEY,
    stateQuestionId             int                             NOT NULL FOREIGN KEY REFERENCES stateQuestions(id),
    questionText                NVARCHAR(200)                   NOT NULL,
    responseType                NUMERIC(3)  default 1           NOT NULL,
    questionOrder                       NUMERIC(3)  default 0           NOT NULL
);

CREATE TABLE stateQuestionsResponse (
    id                         int             IDENTITY(1,1) NOT NULL PRIMARY KEY,
    licenseId                  int                           NOT NULL FOREIGN KEY REFERENCES licenses(id),
    stateQuestionsSpecificsId  int                           NOT NULL FOREIGN KEY REFERENCES stateQuestionsSpecifics(id),
    responseText               NVARCHAR(50)                  NULL,
    responseBool               NUMERIC(2)   default 0       NOT NULL
);
