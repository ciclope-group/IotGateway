CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY ASC, name TEXT UNIQUE NOT NULL, email TEXT UNIQUE NOT NULL, password TEXT NOT NULL, validated BOOLEAN NOT NULL, token TEXT UNIQUE, token_expiration_datetime TEXT, dateCreated TEXT NOT NULL, dateModified TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS roles (id INTEGER PRIMARY KEY ASC, name TEXT UNIQUE NOT NULL, level INTEGER NOT NULL, dateCreated TEXT NOT NULL, dateModified TEXT NOT NULL);
CREATE TABLE IF NOT EXISTS users_in_role (id INTEGER PRIMARY KEY ASC, user INTEGER NOT NULL REFERENCES users(id), role INTEGER NOT NULL REFERENCES roles(id), UNIQUE (user, role));
CREATE TABLE IF NOT EXISTS reservations (id INTEGER PRIMARY KEY ASC, data TEXT);

INSERT OR IGNORE INTO roles (name,level,dateCreated,dateModified) VALUES ('Administrator',0,'2017-05-27T00:00:00.000Z','2017-05-27T00:00:00.000Z');
INSERT OR IGNORE INTO roles (name,level,dateCreated,dateModified) VALUES ('Privileged',1,'2017-05-27T00:00:00.000Z','2017-05-27T00:00:00.000Z');
INSERT OR IGNORE INTO roles (name,level,dateCreated,dateModified) VALUES ('Authenticated',2,'2017-05-27T00:00:00.000Z','2017-05-27T00:00:00.000Z');

INSERT OR IGNORE INTO users (name,email,password,validated,dateCreated,dateModified) VALUES ('administrator','a@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1,'2017-05-27T00:00:00.000Z','2017-05-27T00:00:00.000Z');
INSERT OR IGNORE INTO users (name,email,password,validated,dateCreated,dateModified) VALUES ('privileged','b@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1,'2017-05-27T00:00:00.000Z','2017-05-27T00:00:00.000Z');
INSERT OR IGNORE INTO users (name,email,password,validated,dateCreated,dateModified) VALUES ('authenticated','c@gogogogo.com','16$mVnPhgDp6OGiNi_bz0WjszkwQgVa6ZwAtASmb6hrdzQ',1,'2017-05-27T00:00:00.000Z','2017-05-27T00:00:00.000Z');

INSERT OR IGNORE INTO users_in_role (user, role) VALUES(1,1);
INSERT OR IGNORE INTO users_in_role (user, role) VALUES(2,2);
INSERT OR IGNORE INTO users_in_role (user, role) VALUES(3,3);

--INSERT OR IGNORE INTO gatekeeper_calendar (data) VALUES (json('{"startDate":"2017-06-09T12:00:00.000Z","endDate":"2017-06-09T12:15:00.000Z","freebusy":"BUSY","freebusytype":"RESERVATION","reservation":{"userName":"authenticated","experiment":"SOLAR"}}'));
--INSERT OR IGNORE INTO gatekeeper_calendar (data) VALUES (json('{"startDate":"2017-06-10T00:00:00.000Z","endDate":"2017-06-10T00:15:00.000Z","freebusy":"BUSY","freebusytype":"RESERVATION","reservation":{"userName":"authenticated","experiment":"LUNAR"}}'));
--INSERT OR IGNORE INTO gatekeeper_calendar (data) VALUES (json('{"startDate":"2017-06-10T00:30:00.000Z","endDate":"2017-06-10T00:45:00.000Z","freebusy":"BUSY","freebusytype":"RESERVATION","reservation":{"userName":"authenticated","experiment":"LUNAR"}}'));
--INSERT OR IGNORE INTO gatekeeper_calendar (data) VALUES (json('{"startDate":"2017-06-10T02:00:00.000Z","endDate":"2017-06-10T02:15:00.000Z","freebusy":"BUSY","freebusytype":"RESERVATION","reservation":{"userName":"privileged","experiment":"LUNAR"}}'));
--INSERT OR IGNORE INTO gatekeeper_calendar (data) VALUES (json('{"startDate":"2017-06-10T03:00:00.000Z","endDate":"2017-06-10T03:15:00.000Z","freebusy":"BUSY","freebusytype":"RESERVATION","reservation":{"userName":"authenticated","experiment":"LUNAR"}}'));
--INSERT OR IGNORE INTO gatekeeper_calendar (data) VALUES (json('{"startDate":"2017-06-12T22:00:00.000Z","endDate":"2017-06-12T22:15:00.000Z","freebusy":"BUSY","freebusytype":"RESERVATION","reservation":{"userName":"authenticated","experiment":"LUNAR"}}'));



-- Useful SQLs for the future
--SELECT users.data AS user, group_concat(roles.name) AS roleNames FROM gatekeeper_users AS users LEFT JOIN gatekeeper_users_in_role ON users.id = gatekeeper_users_in_role.user LEFT JOIN gatekeeper_roles AS roles ON roles.id = gatekeeper_users_in_role.role GROUP BY users.id, users.name;
--
--SELECT group_concat(role) FROM (SELECT json_insert(users.data,'$.userNames',json_group_array(roles.name)) AS role FROM gatekeeper_users AS users LEFT JOIN gatekeeper_users_in_role ON users.id = gatekeeper_users_in_role.user LEFT JOIN gatekeeper_roles AS roles ON roles.id = gatekeeper_users_in_role.role GROUP BY users.id, users.name LIMIT 10 OFFSET 0);
--
--SELECT group_concat(role) FROM (SELECT json_insert(roles.data,'$.userNames',CASE WHEN (json_group_array(users.name)='[null]') THEN json_array() ELSE json_group_array(users.name) END) AS role FROM gatekeeper_roles AS roles LEFT JOIN gatekeeper_users_in_role ON roles.id = gatekeeper_users_in_role.role LEFT JOIN gatekeeper_users AS users ON users.id = gatekeeper_users_in_role.user GROUP BY roles.id, roles.name);

--SELECT * FROM weatherstation_historicalstate WHERE DATE(json_extract(data, '$.timestamp'))>"2017-05-31";
--SELECT count(*) FROM gatekeeper_reservations WHERE DATE(json_extract(data, '$.startDate'))=="2017-06-10";

-- Get JsonArray with the roles the user is in
-- SELECT json_group_array(roles.name) FROM gatekeeper_users AS users LEFT JOIN gatekeeper_users_in_role ON users.id = gatekeeper_users_in_role.user LEFT JOIN gatekeeper_roles AS roles ON roles.id = gatekeeper_users_in_role.role WHERE users.name='authenticated' GROUP BY users.id, users.name;