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
    public static final String TIME_NOW = "strftime('%Y-%m-%dT%H:%M:%fZ', 'now')";

    // Database initialization
    public static final String CREATE_ROLES_TABLE ="CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY ASC, " +
            "name TEXT UNIQUE NOT NULL, " +
            "email TEXT UNIQUE NOT NULL, " +
            "password TEXT NOT NULL, " +
            "validated BOOLEAN NOT NULL, " +
            "token TEXT UNIQUE, " +
            "token_expiration_datetime TEXT, " +
            "dateCreated TEXT NOT NULL, " +
            "dateModified TEXT NOT NULL);";
    public static final String CREATE_USERS_TABLE ="CREATE TABLE IF NOT EXISTS roles (" +
            "id INTEGER PRIMARY KEY ASC, " +
            "name TEXT UNIQUE NOT NULL, " +
            "level INTEGER NOT NULL, " +
            "dateCreated TEXT NOT NULL, " +
            "dateModified TEXT NOT NULL);";
    public static final String CREATE_USER_ROLE_TABLE ="CREATE TABLE IF NOT EXISTS user_in_role (" +
            "user INTEGER NOT NULL, " +
            "role INTEGER NOT NULL, " +
            "FOREIGN KEY(user) REFERENCES users(id) ON DELETE CASCADE, " +
            "FOREIGN KEY(role) REFERENCES roles(id) ON DELETE CASCADE, " +
            "PRIMARY KEY(user, role));";
    public static final String CREATE_RESERVATIONS_TABLE ="CREATE TABLE IF NOT EXISTS reservation (id INTEGER PRIMARY KEY ASC, data TEXT);";

    public static final String INSERT_ROLE_ADMINISTRATOR = "INSERT OR IGNORE INTO roles (name,level,dateCreated,dateModified) VALUES ('Administrator',0," + TIME_NOW + "," + TIME_NOW + ");";
    public static final String INSERT_ROLE_PRIVILEGED = "INSERT OR IGNORE INTO roles (name,level,dateCreated,dateModified) VALUES ('Privileged',1," + TIME_NOW + "," + TIME_NOW + ");";
    public static final String INSERT_ROLE_AUTHENTICATED = "INSERT OR IGNORE INTO roles (name,level,dateCreated,dateModified) VALUES ('Authenticated',2," + TIME_NOW + "," + TIME_NOW + ");";

    public static final String INSERT_USER_ADMINISTRATOR = "INSERT OR IGNORE INTO users (name,email,password,validated,dateCreated,dateModified) VALUES ('administrator','a@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1," + TIME_NOW + "," + TIME_NOW + ");";
    public static final String INSERT_USER_PRIVILEGED = "INSERT OR IGNORE INTO users (name,email,password,validated,dateCreated,dateModified) VALUES ('privileged','b@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1," + TIME_NOW + "," + TIME_NOW + ");";
    public static final String INSERT_USER_AUTHENTICATED = "INSERT OR IGNORE INTO users (name,email,password,validated,dateCreated,dateModified) VALUES ('authenticated','c@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1," + TIME_NOW + "," + TIME_NOW + ");";

    public static final String INSERT_USER_ROLE_ADMINISTRATOR = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES(1,1);";
    public static final String INSERT_USER_ROLE_PRIVILEGED = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES(2,2);";
    public static final String INSERT_USER_ROLE_AUTHENTICATED = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES(3,3);";

    // Role operations
    public static final String INSERT_ROLE = "INSERT OR IGNORE INTO roles (name, level, dateCreated, dateModified) VALUES (?,?," + TIME_NOW + "," + TIME_NOW + ");";
    public static final String GET_ALL_ROLES = "SELECT json_group_array(role) FROM (SELECT json_object('name', roles.name, 'level', roles.level, 'userNames', CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END, 'dateCreated', roles.dateCreated, 'dateModified', roles.dateModified) AS role FROM roles LEFT JOIN user_in_role ON roles.id = user_in_role.role LEFT JOIN users ON users.id = user_in_role.user GROUP BY roles.id, roles.name);";
    public static final String GET_ROLE_BY_NAME = "SELECT json_object('name', roles.name, 'level', roles.level, 'userNames', CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END, 'dateCreated', roles.dateCreated, 'dateModified', roles.dateModified) AS role FROM roles LEFT JOIN user_in_role ON roles.id = user_in_role.role LEFT JOIN users ON users.id = user_in_role.user WHERE roles.name = ? GROUP BY roles.id, roles.name;";
    public static final String GET_ROLES_BY_LEVEL = "SELECT json_group_array(role) FROM (SELECT json_object('name', roles.name, 'level', roles.level, 'userNames', CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END, 'dateCreated', roles.dateCreated, 'dateModified', roles.dateModified) AS role FROM roles LEFT JOIN user_in_role ON roles.id = user_in_role.role LEFT JOIN users ON users.id = user_in_role.user WHERE roles.level = ? GROUP BY roles.id, roles.name);";
    public static final String DELETE_ROLE_BY_NAME = "DELETE FROM roles WHERE roles.name = ?;";

    // User-Role operations
    public static final String ADD_USER_ROLE = "INSERT OR IGNORE INTO user_in_role (user, role) VALUES ((SELECT id FROM users WHERE users.name = ?), (SELECT id FROM roles WHERE roles.name = ?));";
    public static final String DELETE_USER_ROLE = "DELETE FROM user_in_role WHERE EXISTS (SELECT 1 FROM users LEFT JOIN roles ON users.name = ? AND roles.name = ? WHERE user_in_role.user = users.id AND user_in_role.role = roles.id);";

    // User operations
    public static final String ADD_USER = "";
    public static final String DELETE_USER = "DELETE FROM users WHERE users.name = ?;";
    public static final String UPDATE_USER_VALIDATION = "";
    public static final String UPDATE_USER_HASH = "";

    // Reservation operations

    // Authorization operations


    private DatabaseSql(){}
}
