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

package info.ciclope.wotgate.thingmanager;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;

public class InteractionAuthorization {
    private static final String USERNAME_KEY = "user";
    private static final String ROLES_KEY = "roles";

    private final JsonObject accessInformation;


    public InteractionAuthorization(String username, JsonArray roles) {
        accessInformation = new JsonObject();
        accessInformation.put(USERNAME_KEY, username);
        accessInformation.put(ROLES_KEY, roles);
    }

    public InteractionAuthorization(JsonObject accessInformation) {
        if (accessInformation.containsKey(USERNAME_KEY) &&
                accessInformation.containsKey(ROLES_KEY) &&
                accessInformation.getJsonArray(ROLES_KEY) != null) {
            this.accessInformation = accessInformation.copy();
        } else {
            this.accessInformation = new JsonObject().put(USERNAME_KEY, "").put(ROLES_KEY, new JsonArray());
        }
    }

    public JsonObject getAccessInformation() {
        return accessInformation;
    }

    public String getUsername() {
        return accessInformation.getString(USERNAME_KEY);
    }

    public boolean isAuthenticatedUser() {
        return accessInformation.getString(USERNAME_KEY) != null &&
                !accessInformation.getString(USERNAME_KEY).isEmpty();
    }

    public boolean isInteractionAllowed(String roleBasedAccess, String roleBasedWritingAccess) {
        if (roleBasedWritingAccess != null || !roleBasedWritingAccess.isEmpty()) {
            return this.containsRole(roleBasedWritingAccess);
        } else if(roleBasedAccess != null || !roleBasedAccess.isEmpty()) {
            return this.containsRole(roleBasedAccess);
        } else {
            return true;
        }
    }

    public boolean containsRole(String rolename) {
        boolean isRoleFound = false;
        Iterator<Object> iterator = accessInformation.getJsonArray(ROLES_KEY).iterator();
        while (!isRoleFound && iterator.hasNext()) {
            String name = (String) iterator.next();
            if (name.equals(rolename)) {
                isRoleFound = true;
            }
        }

        return isRoleFound;
    }

}
