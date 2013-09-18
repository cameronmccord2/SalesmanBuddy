CREATE TABLESPACE salesmanBuddy_data DATAFILE 'salesmanBuddyBeta1.dat' SIZE 1G REUSE AUTOEXTEND ON NEXT 1G MAXSIZE 19G;
CREATE USER salesmanBuddy IDENTIFIED BY salesmanbuddypw DEFAULT TABLESPACE salesmanBuddy_data QUOTA UNLIMITED ON salesmanBuddy_data;
CREATE DATABASE salesmanBuddy;
GO
ALTER DATABASE salesmanBuddy MODIFY FILE(
    NAME=N'salesmanBuddy',
    SIZE=10MB,
    MAXSIZE=5GB,
    FILEGROWTH=10MB
);
GO
ALTER DATABASE salesmanBuddy MODIFY FILE(
    NAME=N'salesmanBuddy_log',
    SIZE=1MB,
    MAXSIZE=5GB,
    FILEGROWTH=10%
);
GO

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
    created                  DATETIME2      default SYSUTCDATETIME() NOT NULL
    -- google fields
);

CREATE TABLE licenses (
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    showInUserList             NUMERIC(2)  default 1                 NOT NULL,
    photo                      NVARCHAR(20)                          NOT NULL,
    bucket                     NVARCHAR(20)                          NOT NULL,
    created                    DATETIME2   default SYSUTCDATETIME()  NOT NULL,
    longitude                  decimal(10, 6)                        NOT NULL, -- sub meter accuracy
    latitude                   decimal(10, 6)                        NOT NULL,
    userId                     int                                   NOT NULL FOREIGN KEY REFERENCES users(id)
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
    order                       NUMERIC(3)  default 0           NOT NULL
);

CREATE TABLE stateQuestionsResponse (
    id                         int             IDENTITY(1,1) NOT NULL PRIMARY KEY,
    licenseId                  int                           NOT NULL FOREIGN KEY REFERENCES licenses(id),
    stateQuestionsSpecificsId  int                           NOT NULL FOREIGN KEY REFERENCES stateQuestionsSpecifics(id),
    responseText               NVARCHAR(50)                  NULL,
    responseBool               NUMERIC(2)   default 0,       NOT NULL
);
