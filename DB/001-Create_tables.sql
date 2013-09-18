-- CREATE TABLESPACE itemMapper_data DATAFILE 'itemMapperBeta1.dat' SIZE 1G REUSE AUTOEXTEND ON NEXT 1G MAXSIZE 19G;
-- CREATE USER itemMapper IDENTIFIED BY itemMapperpw DEFAULT TABLESPACE itemMapper_data QUOTA UNLIMITED ON itemMapper_data;
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

CREATE LOGIN salesmanBuddyServer WITH PASSWORD=N'salesmanBuddyServerpw';
CREATE USER salesmanBuddyServer FOR LOGIN salesmanBuddyServer;
GRANT CREATE SESSION TO salesmanBuddyServer;
GRANT CREATE TABLE TO salesmanBuddyServer;
GRANT CREATE TRIGGER TO salesmanBuddyServer;

USE salesmanBuddy;

CREATE TABLE users (
    id                       int                         IDENTITY(1,1) NOT NULL PRIMARY KEY,
    firstName                NVARCHAR(50)                NOT NULL,
    lastName                 NVARCHAR(50)                NOT NULL,
    email                    NVARCHAR(256)               NOT NULL,
    password                 NVARCHAR(25)                NOT NULL,
    created                  DATETIME2    default SYSUTCDATETIME() NOT NULL,
    status                   NUMERIC(2)     default 1        NOT NULL,
    type                     NUMERIC(3)     default 1           NOT NULL
);

CREATE TABLE securityQuestions (
    id                         int                      IDENTITY(1,1) NOT NULL PRIMARY KEY,
    question                NVARCHAR(50)                NOT NULL,
    answer                    NVARCHAR(50)                NOT NULL,
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL,
    userId                    int                    NOT NULL FOREIGN KEY REFERENCES users(id),
    timesCorrect            NUMERIC(4)    default 0                NOT NULL,
    timesIncorrect            NUMERIC(4)    default 0                NOT NULL,
    status                    NUMERIC(2)    default 1         NOT NULL
);

CREATE TABLE items (
    id                         int                   IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name                    NVARCHAR(30)                NOT NULL,
    userComment             NVARCHAR(200)                NOT NULL,
    status                    NUMERIC(2) default 1         NOT NULL
);

CREATE TABLE itemHistory (
    id                         int                   IDENTITY(1,1) NOT NULL PRIMARY KEY,
    type                    NUMERIC(3)                    NOT NULL,
    created                    DATETIME2   default SYSUTCDATETIME() NOT NULL,
    details                    NVARCHAR(20)                NOT NULL,
    itemId                    int                    NOT NULL FOREIGN KEY REFERENCES items(id),
    userId                  int                     NOT NULL FOREIGN KEY REFERENCES users(id)
);

CREATE TABLE itemOwned (
    id                         int                   IDENTITY(1,1) NOT NULL PRIMARY KEY,
    userId                    int                    NOT NULL FOREIGN KEY REFERENCES users(id),
    itemId                    int                    NOT NULL FOREIGN KEY REFERENCES items(id),
    created                DATETIME2  default SYSUTCDATETIME() NOT NULL,
    status                    NUMERIC(2)    default 1        NOT NULL
);

CREATE TABLE codes (
    id                         int                   IDENTITY(1,1) NOT NULL PRIMARY KEY,
    code                    NVARCHAR(4000)                NOT NULL,
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL,
    itemId                    int                    NOT NULL FOREIGN KEY REFERENCES items(id)
);

CREATE TABLE locations (
    id                         int                   IDENTITY(1,1) NOT NULL PRIMARY KEY,
    longitude                NVARCHAR(20)                NOT NULL,
    latitude                NVARCHAR(20)                NOT NULL,
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL,
    userId                    int                    NULL FOREIGN KEY REFERENCES users(id),
    itemId                    int                    NOT NULL FOREIGN KEY REFERENCES items(id),
    type                    NUMERIC(3)       default 1       NOT NULL
);

CREATE TABLE tokens (
    id                         int                   IDENTITY(1,1) NOT NULL PRIMARY KEY,
    token                    NVARCHAR(24)                NOT NULL,
    userId                    int                    NOT NULL FOREIGN KEY REFERENCES users(id),
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL,
    type                    NUMERIC(3)                    NOT NULL,
    status                     NUMERIC(2)    default 1         NOT NULL
);
