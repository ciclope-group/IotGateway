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

package info.ciclope.wotgate;

public class ErrorCode {
    public static final String ERROR_NO_SQL_CONNECTION = "ERROR: No SQL connection available.";
    public static final String ERROR_OPENING_STORAGE_CONNECTION = "ERROR: Failed opening the storage connection.";
    public static final String ERROR_CLOSING_STORAGE_CONNECTION = "ERROR: Failed closing the storage connection.";
    public static final String ERROR_OPENING_TRANSACTION_CONNECTION = "ERROR: Failed opening transaction connection.";
    public static final String ERROR_CLOSING_TRANSACTION_CONNECTION = "ERROR: Failed closing transaction connection.";
    public static final String ERROR_COMMIT_TRANSACTION_CONNECTION = "ERROR: Failed transaction commit.";
    public static final String ERROR_ROLLBACK_TRANSACTION_CONNECTION = "ERROR: Failed transaction rollback.";
    public static final String ERROR_QUERY = "ERROR: Failed query to the storage.";
    public static final String ERROR_UPDATE = "ERROR: Failed update to the storage.";
    public static final String ERROR_BATCH = "ERROR: Failed query batch to the storage.";
    public static final String ERROR_INSERT_THING = "ERROR: Failed insert abstractthing.";
    public static final String ERROR_LOAD_THING_EXTRA_CONFIGURATION = "ERROR: Failed load of abstractthing extra configuration.";
    public static final String ERROR_THING_CONFIGURATION = "ERROR: AbstractThing configuration needs a name.";
    public static final String ERROR_THING_INTERACTION_NOT_IMPLEMENTED = "Web Thing doesn't implement this interaction.";

    private ErrorCode() {
    }
}
