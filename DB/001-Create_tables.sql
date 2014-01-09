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
    created                    DATETIME2   default SYSUTCDATETIME()  NOT NULL,
    longitude                  decimal(10, 6)                        NOT NULL, -- sub meter accuracy
    latitude                   decimal(10, 6)                        NOT NULL,
    userId                     int                                   NOT NULL FOREIGN KEY REFERENCES users(id),
    stateId                     int                                   NOT NULL FOREIGN KEY REFERENCES states(id)
);

CREATE TABLE questions (
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    version                    int          default 0                NOT NULL,
    questionOrder              int          default 0                NOT NULL,
    questionTextEnglish        NVARCHAR(1000)                        NOT NULL,
    questionTextSpanish        NVARCHAR(1000)                        NOT NULL,
    questionIsBool             BIT          default 0                NOT NULL,
    questionIsText             BIT          default 0                NOT NULL,
    questionIsDropdown         BIT          default 0                NOT NULL,
    questionIsImage            BIT          default 0                NOT NULL,
    required                   BIT          default 0                NOT NULL,
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL
);

CREATE TABLE answers (
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    answerText                 NVARCHAR(500)                         NOT NULL,
    answerBool                 BIT          default 0                NOT NULL,
    answerIsBool               BIT          default 0                NOT NULL,
    answerIsText               BIT          default 0                NOT NULL,
    answerIsDropdown           BIT          default 0                NOT NULL,
    licenseId                  int                                   NOT NULL FOREIGN KEY REFERENCES licenses(id),
    questionId                 int                                   NOT NULL FOREIGN KEY REFERENCES questions(id),
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL
);

CREATE TABLE imageDetails (
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    photoName                  NVARCHAR(30)                          NOT NULL,
    bucketId                   int                                   NOT NULL FOREIGN KEY REFERENCES buckets(id),
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL,
    answerId                   int                                   NOT NULL FOREIGN KEY REFERENCES answers(id)
);

CREATE TABLE dropdownOptions (
    id                          int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    optionText                  NVARCHAR(40)                          NOT NULL,
    questionId                  int                                   NOT NULL FOREIGN KEY REFERENCES questions(id),
    optionOrder                 int         default 0                 NOT NULL,
    optionValue                 NVARCHAR(100)                         NOT NULL
);
