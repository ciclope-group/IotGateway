/*
 *  Copyright (c) 2017, Javier Martínez Villacampa
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package info.ciclope.wotgate.thing.driver.gatekeeper.database;

public class DatabaseSql {
    // Time SQL
    public static final String DATETIME_NOW = "strftime('%Y-%m-%dT%H:%M:%fZ', 'now')";
    public static final String DATETIME_PARAMETER = "(strftime('%Y-%m-%dT%H:%M:%fZ', DATETIME(?/1000, 'unixepoch')))";

    // Database initialization
    public static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY ASC, " +
            "name TEXT UNIQUE NOT NULL, " +
            "email TEXT UNIQUE NOT NULL, " +
            "password TEXT NOT NULL, " +
            "validated BOOLEAN NOT NULL DEFAULT 0, " +
            "token TEXT UNIQUE, " +
            "tokenExpirationDatetime TEXT, " +
            "dateCreated TEXT NOT NULL DEFAULT (" + DATETIME_NOW + "), " +
            "dateModified TEXT NOT NULL DEFAULT (" + DATETIME_NOW + "));";
    public static final String CREATE_ROLES_TABLE = "CREATE TABLE IF NOT EXISTS roles (" +
            "id INTEGER PRIMARY KEY ASC, " +
            "name TEXT UNIQUE NOT NULL, " +
            "level INTEGER NOT NULL, " +
            "dateCreated TEXT NOT NULL DEFAULT (" + DATETIME_NOW + "));";
    public static final String CREATE_USER_ROLE_TABLE = "CREATE TABLE IF NOT EXISTS user_in_role (" +
            "user INTEGER NOT NULL, " +
            "role INTEGER NOT NULL, " +
            "FOREIGN KEY(user) REFERENCES users(id) ON DELETE CASCADE, " +
            "FOREIGN KEY(role) REFERENCES roles(id) ON DELETE CASCADE, " +
            "PRIMARY KEY(user, role));";
    public static final String CREATE_USER_REGISTRATION_TABLE = "CREATE TABLE IF NOT EXISTS user_registration (" +
            "id INTEGER PRIMARY KEY ASC, " +
            "token TEXT UNIQUE, " +
            "user INTEGER NOT NULL, " +
            "dateCreated TEXT NOT NULL DEFAULT (" + DATETIME_NOW + "), " +
            "expirationDateTime TEXT NOT NULL, " +
            "FOREIGN KEY(user) REFERENCES users(id) ON DELETE CASCADE);";
    public static final String CREATE_PASSWORD_RECOVERY_TABLE = "CREATE TABLE IF NOT EXISTS password_recovery (" +
            "id INTEGER PRIMARY KEY ASC, " +
            "token TEXT UNIQUE, " +
            "user INTEGER NOT NULL, " +
            "password TEXT NOT NULL, " +
            "dateCreated TEXT NOT NULL DEFAULT (" + DATETIME_NOW + "), " +
            "expirationDateTime TEXT NOT NULL, " +
            "FOREIGN KEY(user) REFERENCES users(id) ON DELETE CASCADE);";
    public static final String CREATE_RESERVATIONS_TABLE = "CREATE TABLE IF NOT EXISTS reservations (" +
            "id INTEGER PRIMARY KEY ASC, " +
            "startDate TEXT UNIQUE NOT NULL, " +
            "endDate TEXT UNIQUE NOT NULL, " +
            "user INTEGER NOT NULL, " +
            "dateCreated TEXT NOT NULL DEFAULT (" + DATETIME_NOW + "), " +
            "FOREIGN KEY(user) REFERENCES users(id) ON DELETE CASCADE);";

    public static final String INSERT_ROLE_ADMINISTRATOR = "INSERT OR IGNORE INTO roles (name,level) VALUES ('Administrator',0);";
    public static final String INSERT_ROLE_PRIVILEGED = "INSERT OR IGNORE INTO roles (name,level) VALUES ('Privileged',1);";
    public static final String INSERT_ROLE_AUTHENTICATED = "INSERT OR IGNORE INTO roles (name,level) VALUES ('Authenticated',2);";

    public static final String INSERT_USER_ADMINISTRATOR = "INSERT OR IGNORE INTO users (name,email,password,validated) VALUES ('administrator','a@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1);";
    public static final String INSERT_USER_PRIVILEGED = "INSERT OR IGNORE INTO users (name,email,password,validated) VALUES ('privileged','b@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1);";
    public static final String INSERT_USER_AUTHENTICATED = "INSERT OR IGNORE INTO users (name,email,password,validated) VALUES ('authenticated','c@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1);";

    public static final String INSERT_USER_ROLE_ADMINISTRATOR = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES(1,1);";
    public static final String INSERT_USER_ROLE_PRIVILEGED = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES(2,2);";
    public static final String INSERT_USER_ROLE_AUTHENTICATED = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES(3,3);";

    // Role operations
    public static final String INSERT_ROLE = "INSERT OR IGNORE INTO roles (name, level) VALUES (?,?);";
    public static final String GET_ALL_ROLES = "SELECT json_group_array(role) FROM (SELECT json_object('name', roles.name, 'level', roles.level, 'userNames', CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END, 'dateCreated', roles.dateCreated) AS role FROM roles LEFT JOIN user_in_role ON roles.id = user_in_role.role LEFT JOIN users ON users.id = user_in_role.user GROUP BY roles.id, roles.name);";
    public static final String GET_ROLE_BY_NAME = "SELECT json_object('name', roles.name, 'level', roles.level, 'userNames', CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END, 'dateCreated', roles.dateCreated) AS role FROM roles LEFT JOIN user_in_role ON roles.id = user_in_role.role LEFT JOIN users ON users.id = user_in_role.user WHERE roles.name = ? GROUP BY roles.id, roles.name;";
    public static final String GET_ROLES_BY_LEVEL = "SELECT json_group_array(role) FROM (SELECT json_object('name', roles.name, 'level', roles.level, 'userNames', CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END, 'dateCreated', roles.dateCreated) AS role FROM roles LEFT JOIN user_in_role ON roles.id = user_in_role.role LEFT JOIN users ON users.id = user_in_role.user WHERE roles.level = ? GROUP BY roles.id, roles.name);";
    public static final String DELETE_ROLE_BY_NAME = "DELETE FROM roles WHERE roles.name = ?;";

    // User-Role operations
    public static final String ADD_USER_ROLE = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES ((SELECT id FROM users WHERE users.name = ?), (SELECT id FROM roles WHERE roles.name = ?));";
    public static final String DELETE_USER_ROLE = "DELETE FROM user_in_role WHERE EXISTS (SELECT 1 FROM users LEFT JOIN roles ON users.name = ? AND roles.name = ? WHERE user_in_role.user = users.id AND user_in_role.role = roles.id);";

    // User operations
    public static final String GET_ALL_USERS = "SELECT json_group_array(user) FROM (SELECT json_object('name', users.name, 'email', users.email, 'validated', users.validated, 'online', users.token NOT NULL AND DATETIME(users.tokenExpirationDatetime) >= DATETIME('now'), 'roleNames', CASE WHEN (json_group_array(roles.name)='[null]') THEN json_array() ELSE json_group_array(roles.name) END, 'dateCreated', users.dateCreated, 'dateModified', users.dateModified) AS user FROM users LEFT JOIN user_in_role ON users.id = user_in_role.user LEFT JOIN roles ON roles.id = user_in_role.role GROUP BY users.id, users.name);";
    public static final String GET_USER = "SELECT json_object('name', users.name, 'email', users.email, 'roleNames', CASE WHEN (json_group_array(roles.name)='[null]') THEN json_array() ELSE json_group_array(roles.name) END, 'dateCreated', users.dateCreated, 'dateModified', users.dateModified) AS user FROM users LEFT JOIN user_in_role ON users.id = user_in_role.user LEFT JOIN roles ON roles.id = user_in_role.role WHERE users.name = ? GROUP BY users.id, users.name;";
    public static final String GET_USER_BY_NAME = "SELECT json_object('name', users.name, 'email', users.email, 'validated', users.validated, 'online', users.token NOT NULL AND DATETIME(users.tokenExpirationDatetime) >= DATETIME('now'), 'roleNames', CASE WHEN (json_group_array(roles.name)='[null]') THEN json_array() ELSE json_group_array(roles.name) END, 'dateCreated', users.dateCreated, 'dateModified', users.dateModified) AS user FROM users LEFT JOIN user_in_role ON users.id = user_in_role.user LEFT JOIN roles ON roles.id = user_in_role.role WHERE users.name = ? GROUP BY users.id, users.name;";
    public static final String GET_USER_BY_EMAIL = "SELECT json_object('name', users.name, 'email', users.email, 'validated', users.validated, 'online', users.token NOT NULL AND DATETIME(users.tokenExpirationDatetime) >= DATETIME('now'), 'roleNames', CASE WHEN (json_group_array(roles.name)='[null]') THEN json_array() ELSE json_group_array(roles.name) END, 'dateCreated', users.dateCreated, 'dateModified', users.dateModified) AS user FROM users LEFT JOIN user_in_role ON users.id = user_in_role.user LEFT JOIN roles ON roles.id = user_in_role.role WHERE users.email = ? GROUP BY users.id, users.name;";
    public static final String REGISTER_USER = "INSERT INTO users (name,email,password,validated) VALUES (?,?,?,0);";
    public static final String CREATE_USER_REGISTRATION = "INSERT INTO user_registration (token,user,expirationDateTime) VALUES (?,(SELECT id FROM users WHERE users.name = ?)," + DATETIME_PARAMETER + ");";
    public static final String VALIDATE_USER = "UPDATE users SET validated = 1, dateModified = (" + DATETIME_NOW + ") WHERE EXISTS (SELECT 1 FROM users LEFT JOIN user_registration ON user_registration.token = ? WHERE users.name = ? AND users.email = ? AND user_registration.user = users.id AND DATETIME(user_registration.expirationDateTime) >= DATETIME('now'));";
    public static final String DELETE_USER_REGISTRATION = "DELETE FROM user_registration WHERE token = ?;";
    public static final String UPDATE_USER_HASH = "UPDATE users SET password = ?, dateModified = (" + DATETIME_NOW + ") WHERE name = ?;";
    public static final String REQUEST_NEW_USER_HASH = "INSERT INTO password_recovery (token,user,password,expirationDateTime) VALUES (?,(SELECT id FROM users WHERE users.name = ? AND users.email = ?),?," + DATETIME_PARAMETER + ");";
    public static final String RECOVER_HASH = "UPDATE users SET password = (SELECT password_recovery.password FROM password_recovery LEFT JOIN users ON password_recovery.user = users.id WHERE users.name = ? AND users.email = ? AND password_recovery.token = ?), dateModified = (" + DATETIME_NOW + ") WHERE EXISTS (SELECT 1 FROM users LEFT JOIN password_recovery ON password_recovery.user = users.id WHERE password_recovery.token = ? AND users.name = ? AND users.email = ? AND DATETIME(password_recovery.expirationDateTime) >= DATETIME('now'));";
    public static final String DELETE_HASH_RECOVERY = "DELETE FROM password_recovery WHERE token = ?;";
    public static final String DELETE_USER_BY_NAME = "DELETE FROM users WHERE users.name = ?;";

    // Reservation operations
    public static final String GET_ALL_RESERVATIONS = "SELECT json_group_array(reservation) FROM (SELECT json_object('startDate', reservations.startDate, 'endDate', reservations.endDate, 'userName', users.name, 'dateCreated', reservations.dateCreated) AS reservation FROM reservations LEFT JOIN users ON users.id = reservations.user WHERE DATE(reservations.startDate) >= DATE('now') AND DATE(reservations.startDate) <= DATE('now','+14 day'));";
    public static final String GET_ALL_RESERVATIONS_BY_DATE = "SELECT json_group_array(reservation) FROM (SELECT json_object('startDate', reservations.startDate, 'endDate', reservations.endDate, 'userName', users.name, 'dateCreated', reservations.dateCreated) AS reservation FROM reservations LEFT JOIN users ON users.id = reservations.user WHERE DATE(reservations.startDate) = DATE(?/1000, 'unixepoch'));";
    public static final String GET_ALL_RESERVATIONS_BY_DATE_AND_USER = "SELECT json_group_array(reservation) FROM (SELECT json_object('startDate', reservations.startDate, 'endDate', reservations.endDate, 'dateCreated', reservations.dateCreated) AS reservation FROM reservations LEFT JOIN users ON users.id = reservations.user WHERE DATE(reservations.startDate) = DATE(?/1000, 'unixepoch') AND users.name = ?);";
    public static final String GET_ALL_RESERVATIONS_BY_USER = "SELECT json_group_array(reservation) FROM (SELECT json_object('startDate', reservations.startDate, 'endDate', reservations.endDate, 'dateCreated', reservations.dateCreated) AS reservation FROM reservations LEFT JOIN users ON users.id = reservations.user WHERE users.name = ? AND DATE(reservations.startDate) >= DATE('now') AND DATE(reservations.startDate) <= DATE('now','+14 day'));";
    public static final String ADD_RESERVATION = "INSERT INTO reservations (startDate, endDate, user) VALUES (" + DATETIME_PARAMETER + ", " + DATETIME_PARAMETER + ", (SELECT id FROM users WHERE users.name = ?));";
    public static final String DELETE_RESERVATION_BY_DATE = "DELETE FROM reservations WHERE DATETIME(reservations.startDate) = DATETIME(?/1000, 'unixepoch') AND reservations.user = (SELECT id FROM users WHERE users.name= ?);";
    public static final String GET_ACTIVE_RESERVATION = "SELECT json_object('startDate', reservations.startDate, 'endDate', reservations.endDate, 'userName', users.name, 'dateCreated', reservations.dateCreated) FROM reservations LEFT JOIN users ON users.id = reservations.user WHERE DATETIME(reservations.startDate) <= DATETIME('now') AND DATETIME(reservations.endDate) >= DATETIME('now')";

    // Authorization operations
    public static final String GET_USER_ROLES = "SELECT json_group_array(userroles.name) FROM (SELECT roles.name FROM users, roles, user_in_role WHERE users.id = user_in_role.user AND user_in_role.role = roles.id AND users.name = ? GROUP BY roles.name UNION SELECT roles.name FROM (SELECT roles.name, roles.level FROM users, roles, user_in_role WHERE users.id = user_in_role.user AND user_in_role.role = roles.id AND users.name = ? GROUP BY roles.name) AS userroles, roles WHERE roles.level > userroles.level GROUP BY roles.name) AS userroles";
    public static final String GET_USER_HASH = "SELECT password FROM users WHERE name = ? AND validated;";
    public static final String ADD_USER_TOKEN = "UPDATE users SET token = ?, tokenExpirationDatetime = " + DATETIME_PARAMETER + " WHERE name = ? AND validated;";
    public static final String REVOKE_USER_TOKEN = "UPDATE users SET token = null, tokenExpirationDatetime = null WHERE name = ? AND validated;";
    public static final String GET_USER_PERMISSIONS = "SELECT json_object('name', users.name, 'roleNames', (" + GET_USER_ROLES + "), 'reservationOngoing', (" + GET_ACTIVE_RESERVATION + ") NOT NULL) FROM users WHERE users.name = ? AND validated;";

    private DatabaseSql() {
    }
}
