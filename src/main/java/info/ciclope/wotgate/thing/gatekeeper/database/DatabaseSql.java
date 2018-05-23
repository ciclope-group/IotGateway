package info.ciclope.wotgate.thing.gatekeeper.database;

import info.ciclope.wotgate.thing.gatekeeper.model.AuthorityName;
import info.ciclope.wotgate.thing.gatekeeper.model.ReservationStatus;

class DatabaseSql {
    static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS user (" +
            "id INTEGER PRIMARY KEY, " +
            "username TEXT UNIQUE NOT NULL, " +
            "email TEXT UNIQUE NOT NULL, " +
            "password TEXT NOT NULL, " +
            "enabled BOOLEAN NOT NULL DEFAULT 0);";

    static final String CREATE_AUTHORITY_TABLE = "CREATE TABLE IF NOT EXISTS authority (" +
            "id INTEGER PRIMARY KEY, " +
            "name TEXT UNIQUE NOT NULL);";

    static final String CREATE_USER_AUTHORITY_TABLE = "CREATE TABLE IF NOT EXISTS user_authority (" +
            "user_id INTEGER NOT NULL, " +
            "authority_id INTEGER NOT NULL, " +
            "FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE, " +
            "FOREIGN KEY(authority_id) REFERENCES authority(id) ON DELETE CASCADE, " +
            "PRIMARY KEY(user_id, authority_id));";

    static final String CREATE_RESERVATIONS_TABLE = "CREATE TABLE IF NOT EXISTS reservation (" +
            "id INTEGER PRIMARY KEY, " +
            "startDate TEXT NOT NULL, " +
            "endDate TEXT NOT NULL, " +
            "user_id INTEGER NOT NULL, " +
            "dateCreated TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')), " +
            "status_id INTEGER NOT NULL, " +
            "FOREIGN KEY(user_id) REFERENCES user(id), " +
            "FOREIGN KEY(status_id) REFERENCES reservation_status(id));";

    static final String CREATE_RESERVATION_STATUS_TABLE = "CREATE TABLE IF NOT EXISTS reservation_status (" +
            "id INTEGER PRIMARY KEY, " +
            "description TEXT NOT NULL);";

    static final String INSERT_AUTHORITIES = String.format("INSERT OR IGNORE INTO authority(id, name) " +
            "VALUES (1, '%s'), (2, '%s')", AuthorityName.ROLE_USER, AuthorityName.ROLE_ADMIN);

    static final String INSERT_RESERVATION_STATUS = "INSERT OR IGNORE INTO reservation_status (id, description) VALUES " +
            String.format("(%d, '%s'),", ReservationStatus.PENDING, ReservationStatus.PENDING_STR) +
            String.format("(%d, '%s'),", ReservationStatus.COMPLETED, ReservationStatus.COMPLETED_STR) +
            String.format("(%d, '%s')", ReservationStatus.CANCELED, ReservationStatus.CANCELED_STR);


}
