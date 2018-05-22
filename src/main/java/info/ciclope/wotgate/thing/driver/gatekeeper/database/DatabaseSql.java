package info.ciclope.wotgate.thing.driver.gatekeeper.database;

class DatabaseSql {
    static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS user (" +
            "id INTEGER PRIMARY KEY, " +
            "username TEXT UNIQUE NOT NULL, " +
            "email TEXT UNIQUE NOT NULL, " +
            "password TEXT NOT NULL, " +
            "enabled BOOLEAN NOT NULL DEFAULT 0);";

    static final String CREATE_ROLES_TABLE = "CREATE TABLE IF NOT EXISTS authority (" +
            "id INTEGER PRIMARY KEY, " +
            "name TEXT UNIQUE NOT NULL);";

    static final String CREATE_USER_ROLE_TABLE = "CREATE TABLE IF NOT EXISTS user_authority (" +
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
}
