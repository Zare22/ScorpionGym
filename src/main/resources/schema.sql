-- ScorpionGym database schema (SQLite)
-- =============================================================================
-- This file mirrors the live schema from gymdatabase.db. It is intended as
-- source-of-truth documentation: code review, onboarding, and the eventual
-- KMM/SQLDelight migration. It is NOT executed at runtime — DatabaseFactory
-- expects an existing database file.
--
-- Foreign keys are enforced (PRAGMA foreign_keys = ON in DatabaseFactory).
-- =============================================================================


-- ----------------------------------------------------------------------------
-- TABLES
-- ----------------------------------------------------------------------------

CREATE TABLE AppUser (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    isAdmin BOOLEAN NOT NULL DEFAULT FALSE
);

-- Single-row table holding the id of the currently-logged-in user.
-- Read by audit-log triggers via (SELECT currentAppUserId FROM CurrentSessionUser).
-- Updated on login (Main.kt) so triggers can stamp the right user on log rows.
CREATE TABLE CurrentSessionUser (
    currentAppUserId INTEGER
);

CREATE TABLE Member (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    phoneNumber TEXT,
    signedUpDate DATE NOT NULL,
    dateOfBirth DATE,
    membershipRecordId INTEGER,
    organizationId INTEGER,
    statusId INTEGER,
    remark TEXT,
    gender TEXT,
    FOREIGN KEY (membershipRecordId) REFERENCES MembershipRecord(id) ON DELETE SET NULL,
    FOREIGN KEY (organizationId)     REFERENCES Organization(id)     ON DELETE RESTRICT,
    FOREIGN KEY (statusId)           REFERENCES Status(id)           ON DELETE RESTRICT
);

CREATE TABLE MemberOtherService (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dateOfService DATETIME NOT NULL,
    memberId INTEGER NOT NULL,
    otherServiceId INTEGER NOT NULL,
    isPaid BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (memberId)       REFERENCES Member(id)       ON DELETE CASCADE,
    FOREIGN KEY (otherServiceId) REFERENCES OtherService(id) ON DELETE RESTRICT
);

CREATE TABLE Membership (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    duration LONG NOT NULL DEFAULT 1,
    numberOfTrainingsAvailable INTEGER NOT NULL,
    isNoLimit BOOLEAN NOT NULL DEFAULT 0
);

CREATE TABLE MembershipRecord (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    memberId INTEGER NOT NULL,
    membershipId INTEGER NOT NULL,
    dateStarted DATE NOT NULL,
    dateFinished DATE,
    isActive BOOLEAN NOT NULL DEFAULT TRUE,
    isPaid BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (memberId)     REFERENCES Member(id)     ON DELETE CASCADE,
    FOREIGN KEY (membershipId) REFERENCES Membership(id) ON DELETE RESTRICT
);

CREATE TABLE Organization (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    typeOfOrganizationId INTEGER,
    FOREIGN KEY (typeOfOrganizationId) REFERENCES TypeOfOrganization(id) ON DELETE RESTRICT
);

CREATE TABLE OtherService (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- Append-only audit log of payment state changes. Populated entirely by triggers
-- on MembershipRecord, MemberOtherService, and UnregisteredService — Kotlin code
-- never INSERTs here directly. Exactly one of (membershipRecordId, memberOtherServiceId,
-- unregisteredServiceId) is non-null per row.
CREATE TABLE PaymentAuditLog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    membershipRecordId INTEGER,
    memberOtherServiceId INTEGER,
    unregisteredServiceId INTEGER,
    isPaidOld BOOLEAN NOT NULL,
    isPaidNew BOOLEAN NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    changedAt DATETIME NOT NULL,
    loggedInUserId INTEGER NOT NULL,
    FOREIGN KEY (membershipRecordId)    REFERENCES MembershipRecord(id)    ON DELETE CASCADE,
    FOREIGN KEY (memberOtherServiceId)  REFERENCES MemberOtherService(id)  ON DELETE CASCADE,
    FOREIGN KEY (unregisteredServiceId) REFERENCES UnregisteredService(id) ON DELETE CASCADE,
    FOREIGN KEY (loggedInUserId)        REFERENCES AppUser(id)             ON DELETE CASCADE
);

CREATE TABLE Status (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL
);

CREATE TABLE TrainingSession (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    membershipRecordId INTEGER NOT NULL,
    sessionDateTime DATETIME NOT NULL,
    FOREIGN KEY (membershipRecordId) REFERENCES MembershipRecord(id) ON DELETE CASCADE
);

CREATE TABLE TypeOfOrganization (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    discountRate DECIMAL(3, 2) DEFAULT 0.00
);

-- A "walk-in" / non-member service. Either a one-off OtherService purchase or
-- a one-off Membership purchase, but not both — one of the two FKs is null.
CREATE TABLE UnregisteredService (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dateOfService DATETIME NOT NULL,
    otherServiceId INTEGER,
    membershipId INTEGER,
    isPaid BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (membershipId)   REFERENCES Membership(id)   ON DELETE RESTRICT,
    FOREIGN KEY (otherServiceId) REFERENCES OtherService(id) ON DELETE RESTRICT
);

-- Free-form audit log of admin actions on entities (created/updated/deleted X).
-- Written by util/AuditLog.kt. No ON DELETE rule on appUserId, so deleting an
-- AppUser requires manually clearing UserActivityLog first (see AppUserDao.deleteAppUser).
CREATE TABLE UserActivityLog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    appUserId INTEGER NOT NULL,
    action TEXT NOT NULL,
    dateOfAction TEXT NOT NULL,
    FOREIGN KEY (appUserId) REFERENCES AppUser(id)
);


-- ----------------------------------------------------------------------------
-- TRIGGERS
-- ----------------------------------------------------------------------------
-- Six "Log*Payment*" triggers auto-populate PaymentAuditLog whenever isPaid
-- flips on the relevant table. They share the same shape: on INSERT-when-paid
-- or on UPDATE OF isPaid, write a row to PaymentAuditLog with the price looked
-- up from the source table.
-- ----------------------------------------------------------------------------

CREATE TRIGGER LogMembershipRecordPaymentInsert
    AFTER INSERT ON MembershipRecord
    FOR EACH ROW
    WHEN NEW.isPaid = 1
BEGIN
    INSERT INTO PaymentAuditLog (
        membershipRecordId, memberOtherServiceId, unregisteredServiceId,
        isPaidOld, isPaidNew, price, changedAt, loggedInUserId
    )
    VALUES (
        NEW.id, NULL, NULL,
        0, NEW.isPaid,
        (SELECT price FROM Membership WHERE id = NEW.membershipId),
        strftime('%Y-%m-%d', 'now', 'localtime'),
        (SELECT currentAppUserId FROM CurrentSessionUser)
    );
END;

CREATE TRIGGER LogMembershipRecordPaymentUpdate
    AFTER UPDATE OF isPaid ON MembershipRecord
    FOR EACH ROW
    WHEN OLD.isPaid != NEW.isPaid
BEGIN
    INSERT INTO PaymentAuditLog (
        membershipRecordId, memberOtherServiceId, unregisteredServiceId,
        isPaidOld, isPaidNew, price, changedAt, loggedInUserId
    )
    VALUES (
        OLD.id, NULL, NULL,
        OLD.isPaid, NEW.isPaid,
        (SELECT price FROM Membership WHERE id = OLD.membershipId),
        strftime('%Y-%m-%d', 'now', 'localtime'),
        (SELECT currentAppUserId FROM CurrentSessionUser)
    );
END;

CREATE TRIGGER LogOtherServicePaymentInsert
    AFTER INSERT ON MemberOtherService
    FOR EACH ROW
    WHEN NEW.isPaid = 1
BEGIN
    INSERT INTO PaymentAuditLog (
        membershipRecordId, memberOtherServiceId, unregisteredServiceId,
        isPaidOld, isPaidNew, price, changedAt, loggedInUserId
    )
    VALUES (
        NULL, NEW.id, NULL,
        0, NEW.isPaid,
        (SELECT price FROM OtherService WHERE id = NEW.otherServiceId),
        strftime('%Y-%m-%d', 'now', 'localtime'),
        (SELECT currentAppUserId FROM CurrentSessionUser)
    );
END;

CREATE TRIGGER LogOtherServicePaymentUpdate
    AFTER UPDATE OF isPaid ON MemberOtherService
    FOR EACH ROW
    WHEN OLD.isPaid != NEW.isPaid
BEGIN
    INSERT INTO PaymentAuditLog (
        membershipRecordId, memberOtherServiceId, unregisteredServiceId,
        isPaidOld, isPaidNew, price, changedAt, loggedInUserId
    )
    VALUES (
        NULL, OLD.id, NULL,
        OLD.isPaid, NEW.isPaid,
        (SELECT price FROM OtherService WHERE id = OLD.otherServiceId),
        strftime('%Y-%m-%d', 'now', 'localtime'),
        (SELECT currentAppUserId FROM CurrentSessionUser)
    );
END;

CREATE TRIGGER LogUnregisteredServicePaymentInsert
    AFTER INSERT ON UnregisteredService
    FOR EACH ROW
    WHEN NEW.isPaid = 1
BEGIN
    INSERT INTO PaymentAuditLog (
        membershipRecordId, memberOtherServiceId, unregisteredServiceId,
        isPaidOld, isPaidNew, price, changedAt, loggedInUserId
    )
    VALUES (
        NULL, NULL, NEW.id,
        0, NEW.isPaid,
        COALESCE(
            (SELECT price FROM OtherService WHERE id = NEW.otherServiceId),
            (SELECT price FROM Membership   WHERE id = NEW.membershipId)
        ),
        strftime('%Y-%m-%d', 'now', 'localtime'),
        (SELECT currentAppUserId FROM CurrentSessionUser)
    );
END;

CREATE TRIGGER LogUnregisteredServicePaymentUpdate
    AFTER UPDATE OF isPaid ON UnregisteredService
    FOR EACH ROW
    WHEN OLD.isPaid != NEW.isPaid
BEGIN
    INSERT INTO PaymentAuditLog (
        membershipRecordId, memberOtherServiceId, unregisteredServiceId,
        isPaidOld, isPaidNew, price, changedAt, loggedInUserId
    )
    VALUES (
        NULL, NULL, OLD.id,
        OLD.isPaid, NEW.isPaid,
        COALESCE(
            (SELECT price FROM OtherService WHERE id = OLD.otherServiceId),
            (SELECT price FROM Membership   WHERE id = OLD.membershipId)
        ),
        strftime('%Y-%m-%d', 'now', 'localtime'),
        (SELECT currentAppUserId FROM CurrentSessionUser)
    );
END;

-- When a record is deactivated, clear it as the "current" record on the member.
CREATE TRIGGER SetMembershipRecordIdToNull
    AFTER UPDATE ON MembershipRecord
    FOR EACH ROW
    WHEN NEW.isActive = FALSE
BEGIN
    UPDATE Member
       SET membershipRecordId = NULL
     WHERE membershipRecordId = NEW.id;
END;

-- When the Nth training is logged on a capped membership, deactivate the record.
-- Note: also re-applied on every app start by DatabaseFactory.initDB() to keep
-- the latest version live.
CREATE TRIGGER UpdateMembershipStatus
    AFTER INSERT ON TrainingSession
    FOR EACH ROW
BEGIN
    UPDATE MembershipRecord
       SET isActive = FALSE,
           dateFinished = strftime('%Y-%m-%d', datetime('now', 'localtime'))
     WHERE id = NEW.membershipRecordId
       AND isActive = TRUE
       AND (SELECT COUNT(*) FROM TrainingSession WHERE membershipRecordId = NEW.membershipRecordId) >=
           COALESCE(
               (SELECT m.numberOfTrainingsAvailable
                  FROM Membership AS m
                  JOIN MembershipRecord AS mr ON m.id = mr.membershipId
                 WHERE mr.id = NEW.membershipRecordId),
               0
           );
END;


-- ----------------------------------------------------------------------------
-- VIEWS
-- ----------------------------------------------------------------------------

-- Denormalized read-only view over PaymentAuditLog joined with all three
-- payment sources (MembershipRecord, MemberOtherService, UnregisteredService)
-- and AppUser. Each row exposes only the fields for whichever source produced
-- it; the others are NULL.
CREATE VIEW PaymentAuditLogView AS
SELECT
    p.id AS paymentAuditLogId,

    -- MembershipRecord-related fields
    mr.id           AS membershipRecordId,
    mr.memberId     AS membershipMemberId,
    mr.membershipId AS membershipId,
    mr.dateStarted  AS membershipDateStarted,
    mr.dateFinished AS membershipDateFinished,
    mr.isActive     AS membershipIsActive,
    mr.isPaid       AS membershipIsPaid,

    -- MemberOtherService-related fields
    mo.id             AS memberOtherServiceId,
    mo.dateOfService  AS memberOtherServiceDateOfService,
    mo.isPaid         AS memberOtherServiceIsPaid,
    mo.memberId       AS memberOtherServiceMemberId,
    mo.otherServiceId AS memberOtherServiceOtherServiceId,

    -- UnregisteredService-related fields
    u.id             AS unregisteredServiceId,
    u.dateOfService  AS unregisteredServiceDateOfService,
    u.isPaid         AS unregisteredServiceIsPaid,
    u.membershipId   AS unregisteredServiceMembershipId,
    u.otherServiceId AS unregisteredServiceOtherServiceId,

    -- PaymentAuditLog fields
    p.isPaidOld,
    p.isPaidNew,
    p.price,
    p.changedAt,

    -- AppUser fields
    a.id       AS appUserId,
    a.username AS appUsername,

    -- 1 if this row is from an UnregisteredService that bought a Membership
    CASE WHEN u.membershipId IS NOT NULL THEN 1 ELSE 0 END AS isUnregisteredServiceMembership
FROM PaymentAuditLog p
LEFT JOIN MembershipRecord    mr ON p.membershipRecordId    = mr.id
LEFT JOIN MemberOtherService  mo ON p.memberOtherServiceId  = mo.id
LEFT JOIN UnregisteredService u  ON p.unregisteredServiceId = u.id
LEFT JOIN AppUser             a  ON p.loggedInUserId        = a.id;
