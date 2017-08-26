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

package info.ciclope.wotgate.thing.driver.gatekeeper.interaction;

import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.storage.DatabaseStorage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Calendar {
    private final String IS_RESERVATION = "(json_extract(data, '$.freebusytype') = 'RESERVATION')";
    private final String USERNAME_EQUALS = "(json_extract(data, '$.reservation.userName') = ? )";
    private final String STARTDATE_EQUALS = "(DATE(json_extract(data, '$.startDate')) = DATE(?))";
    private final String STARTDATE_EQUALS_OR_AFTER = "(DATE(json_extract(data, '$.startDate')) >= DATE(?))";
    private final String RESERVATION_ON_DATETIME = "(DATETIME(json_extract(data, '$.startDate')) <= DATETIME(?)) AND (DATETIME(json_extract(data, '$.endDate')) > DATETIME(?))";
    private final String INSERT_RESERVATION = "INSERT INTO calendar (data) VALUES (json(?));";
    private final String SEARCH_ALL_USER_RESERVATIONS = "SELECT json_group_array(json(data)) FROM calendar WHERE " + USERNAME_EQUALS + ";";
    private final String SEARCH_USER_RESERVATION_ON_DATETIME = "SELECT data FROM calendar WHERE " + RESERVATION_ON_DATETIME + " AND " + USERNAME_EQUALS + ";";
    private final String USER_RESERVATIONS_ON_DATE = "SELECT data FROM calendar WHERE " + STARTDATE_EQUALS + " AND " + USERNAME_EQUALS + ";";
    private final String USER_RESERVATIONS_FROM_DATE = "SELECT data FROM calendar WHERE " + STARTDATE_EQUALS_OR_AFTER + " AND " + USERNAME_EQUALS + ";";
    private final String RESERVATIONS_ON_START_DATE = "SELECT data FROM calendar WHERE " + STARTDATE_EQUALS + " AND " + IS_RESERVATION + ";";
    private final String DELETE_USER_RESERVATION = "DELETE FROM calendar WHERE " + STARTDATE_EQUALS + " AND " + USERNAME_EQUALS + ";";
    private final Integer SLOT_SIZE = 15;
    private final Integer MAXIMUM_USER_RESERVATIONS_PER_DAY = 4;


    private final DatabaseStorage databaseStorage;

    public Calendar(DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void getCalendar(final JsonObject inputData, final String userName, Handler<AsyncResult<JsonArray>> handler) {
        if (inputData.containsKey("startDate")) {
            try {
                ZonedDateTime.parse(inputData.getString("startDate"));
            } catch (DateTimeParseException exception) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
                return;
            }
            if (inputData.containsKey("freebusy")) {
                if (inputData.getString("freebusy").equals("BUSY")) {
                    getUserReservationsOnDate(inputData, userName, handler);
                    return;
                } else if (inputData.getString("freebusy").equals("FREE")) {
                    getFreeSlotsOnDate(inputData, handler);
                    return;
                }
            }
            if (inputData.containsKey("freebusytype") && inputData.getString("freebusytype").equals("RESERVATION")) {
                getUserReservationsOnDate(inputData, userName, handler);
                return;
            }
        } else {
            if (inputData.containsKey("freebusytype")) {
                if (inputData.getString("freebusytype").equals("RESERVATION")) {
                    getUserAllReservations(userName, handler);
                    return;
                }
            }
            if (inputData.containsKey("freebusy")) {
                if (inputData.getString("freebusy").equals("FREE")) {
                    // TODO: implement
                }
            }
        }
        handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
    }

    public void addUserReservation(final JsonObject inputData, final String userName, Handler<AsyncResult<JsonObject>> handler) {
        if (inputData != null && inputData.containsKey("startDate") && inputData.containsKey("experiment") &&
                (inputData.getString("experiment").equals("SOLAR") || inputData.getString("experiment").equals("LUNAR"))) {
            ZonedDateTime goalDateTime;
            try {
                goalDateTime = ZonedDateTime.parse(inputData.getString("startDate"));
            } catch (DateTimeParseException exception) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
                return;
            }

            // Check user did not surpass maximum slots per day
            getUserReservationsOnDate(goalDateTime, userName, userSlots -> {
                if (userSlots.failed()) {
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), userSlots.cause())));
                } else if (userSlots.result().size() == 4) {
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.FORBIDDEN.toString())));
                } else {
                    if (isWellFormedSlot(goalDateTime)) {
                        // Check slot is not registered
                        getReservationOnStartDate(goalDateTime, result -> {
                            if (result.failed()) {
                                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
                            } else if (!result.result().isEmpty()) {
                                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.RESOURCE_NOT_FOUND.toString())));
                            } else {
                                // Register user slot reservation
                                JsonObject data = new JsonObject();
                                Instant now = Instant.now();
                                now.atZone(ZoneId.of("UTC"));
                                String currentTimestamp = now.toString();
                                data.put("startDate", goalDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                                data.put("endDate", goalDateTime.plusMinutes(15).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                                data.put("freebusy", "BUSY");
                                data.put("freebusytype", "RESERVATION");
                                JsonObject reservation = new JsonObject();
                                reservation.put("userName", userName);
                                reservation.put("experiment", inputData.getString("experiment"));
                                reservation.put("dateCreated", currentTimestamp);
                                data.put("reservation", reservation);
                                String query = "INSERT INTO calendar (data) VALUES (json('" + data.toString() + "'));";
                                databaseStorage.update(query, insertResult -> {
                                    if (result.failed()) {
                                        handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
                                        return;
                                    }
                                    handler.handle(Future.succeededFuture());
                                });
                            }
                        });
                    } else {
                        handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
                    }
                }
            });

        } else {
            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
        }
    }

    public void deleteUserReservation(final JsonObject inputData, final String userName, Handler<AsyncResult<Void>> handler) {
        if (inputData != null && inputData.containsKey("startDate")) {
            ZonedDateTime startDateTime;
            try {
                startDateTime = ZonedDateTime.parse(inputData.getString("startDate"));
            } catch (DateTimeParseException exception) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
                return;
            }
            String query = "DELETE FROM calendar WHERE (DATETIME(json_extract(data, '$.startDate')) = DATETIME('" + startDateTime.toString() + "')) AND (json_extract(data, '$.reservation.userName') ='" + userName + "');";
            databaseStorage.update(query, result -> {
                if (result.failed()) {
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
                } else {
                    handler.handle(Future.succeededFuture());
                }
            });
        } else {
            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
        }
    }

    public void ackReservation(final String userName, Handler<AsyncResult<Void>> handler) {
        Instant now = Instant.now();
        now.atZone(ZoneId.of("UTC"));
        String currentDateTime = now.toString();
        String query = "SELECT json(data) FROM calendar WHERE (DATETIME(json_extract(data, '$.startDate')) <= DATETIME('" + currentDateTime + "')) AND (DATETIME(json_extract(data, '$.endDate')) > DATETIME('" + currentDateTime + "')) AND (json_extract(data, '$.reservation.userName') = '" + userName + "');";
        databaseStorage.query(query, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
            } else {
                if (result.result().getRows().isEmpty()) {
                    handler.handle(Future.failedFuture(HttpResponseStatus.FORBIDDEN.toString()));
                } else {
                    handler.handle(Future.succeededFuture());
                }
            }
        });
    }

    private void getUserAllReservations(final String userName, Handler<AsyncResult<JsonArray>> handler) {
        // Get the user slot calendar in the next 7 days
        JsonArray parameters = new JsonArray();
        parameters.add(userName);
        databaseStorage.queryWithParameters(SEARCH_ALL_USER_RESERVATIONS, parameters, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
            } else {
                if (result.result().getRows().isEmpty() || result.result().getResults().get(0).getString(0).contentEquals("[]")) {
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.RESOURCE_NOT_FOUND.toString(), result.cause())));
                } else {
                    JsonArray results = new JsonArray(result.result().getResults().get(0).getString(0));
                    handler.handle(Future.succeededFuture(results));
                }
            }
        });
    }

    private void getUserReservationsOnDate(final JsonObject inputData, final String userName, Handler<AsyncResult<JsonArray>> handler) {
        // Check input
        if (inputData != null && inputData.containsKey("startDate")) {
            ZonedDateTime startDateTime;
            try {
                startDateTime = ZonedDateTime.parse(inputData.getString("startDate"));
            } catch (DateTimeParseException exception) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
                return;
            }
            getUserReservationsOnDate(startDateTime, userName, result -> {
                if (result.failed()) {
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
                } else {
                    handler.handle(Future.succeededFuture(result.result()));
                }
            });
        } else {
            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
        }
    }

    private void getFreeSlotsOnDate(JsonObject inputData, Handler<AsyncResult<JsonArray>> handler) {
        // TODO: Check start date is not beyond 7 days after??

        if (inputData != null && inputData.containsKey("startDate")) {
            ZonedDateTime goalDateTime;
            try {
                goalDateTime = ZonedDateTime.parse(inputData.getString("startDate"));
            } catch (DateTimeParseException exception) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
                return;
            }
            JsonArray allFreeSlots = generateAllRemainingDateSlots(goalDateTime);
            // GET ALL RESERVATIONS ON DATE
            getReservationsOnStartDate(goalDateTime, result -> {
                if (result.failed()) {
                    handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
                } else {
                    handler.handle(Future.succeededFuture(filterSlots(allFreeSlots, result.result())));
                }
            });
        } else {
            handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.BAD_REQUEST.toString())));
        }
    }

    private void getReservationsOnStartDate(ZonedDateTime reservationStartDate, Handler<AsyncResult<JsonArray>> handler) {
        String query = "SELECT json_group_array(json(data)) FROM calendar WHERE (DATE(json_extract(data, '$.startDate')) = DATE('" + reservationStartDate.toLocalDate().toString() + "')) AND " + IS_RESERVATION + " ORDER BY DATETIME(json_extract(data, '$.startDate')) ASC;";
        databaseStorage.query(query, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
            } else {
                handler.handle(Future.succeededFuture(new JsonArray(result.result().getResults().get(0).getString(0))));
            }
        });
    }

    private void getReservationOnStartDate(ZonedDateTime reservationStartDate, Handler<AsyncResult<JsonArray>> handler) {
        String query = "SELECT json_group_array(json(data)) FROM calendar WHERE (DATETIME(json_extract(data, '$.startDate')) = DATETIME('" + reservationStartDate.toLocalDateTime().toString() + "')) AND " + IS_RESERVATION + ";";
        databaseStorage.query(query, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
            } else {
                handler.handle(Future.succeededFuture(new JsonArray(result.result().getResults().get(0).getString(0))));
            }
        });
    }

    private void getUserReservationsOnDate(final ZonedDateTime zonedDateTime, final String userName, Handler<AsyncResult<JsonArray>> handler) {
        JsonArray parameters = new JsonArray();
        parameters.add(userName);
        String query = "SELECT json_group_array(json(data)) FROM calendar WHERE (DATE(json_extract(data, '$.startDate')) = DATE('" + zonedDateTime.toString() + "')) AND " + USERNAME_EQUALS + ";";
        databaseStorage.queryWithParameters(query, parameters, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(new Throwable(HttpResponseStatus.INTERNAL_ERROR.toString(), result.cause())));
            } else {
                handler.handle(Future.succeededFuture(new JsonArray(result.result().getResults().get(0).getString(0))));
            }
        });

    }

    private JsonArray generateAllRemainingDateSlots(final ZonedDateTime zonedDateTime) {
        JsonArray slotsArray = new JsonArray();
        LocalDate dateGoal = zonedDateTime.toLocalDate();
        LocalDateTime dateTimeToday = LocalDateTime.now();
        LocalTime startTimeGoal = LocalTime.parse("00:00:00");
        if (dateGoal.isBefore(dateTimeToday.toLocalDate())) {
            return slotsArray;
        } else if (dateGoal.isEqual(dateTimeToday.toLocalDate())) {
            startTimeGoal = dateTimeToday.toLocalTime();
        }

        int hour = startTimeGoal.getHour();
        int minutes = startTimeGoal.getMinute();
        if (minutes > 0 && minutes < 15) {
            minutes = 15;
        } else if (minutes > 15 && minutes < 30) {
            minutes = 30;
        } else if (minutes > 30 && minutes < 45) {
            minutes = 45;
        } else if (minutes > 45 && hour < 23) {
            hour = hour++;
            minutes = 0;
        } else if (minutes > 45 && hour == 23) {
            return slotsArray;
        }
        startTimeGoal = startTimeGoal.withMinute(minutes).withHour(hour).withSecond(0).withNano(0);

        while (hour < 24) {
            JsonObject slotObject = new JsonObject();
            ZonedDateTime startDate = LocalDateTime.parse(dateGoal.toString() + "T" + startTimeGoal.toString()).atZone(ZoneOffset.UTC);
            minutes = minutes + 15;
            if (minutes == 60) {
                minutes = 0;
                hour++;
            }
            int endHour = hour;
            if (hour == 24) {
                endHour = 0;
                dateGoal = dateGoal.plusDays(1);
            }
            startTimeGoal = startTimeGoal.withMinute(minutes).withHour(endHour);
            ZonedDateTime endDate = LocalDateTime.parse(dateGoal.toString() + "T" + startTimeGoal.toString()).atZone(ZoneOffset.UTC);
            slotObject.put("startDate", startDate.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)).put("endDate", endDate.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)).put("freebusy", "FREE");
            slotsArray.add(slotObject);
        }

        return slotsArray;
    }

    private JsonArray filterSlots(JsonArray slotArray, JsonArray slotsToFilter) {
        JsonArray filteredArray = slotArray.copy();
        JsonArray filteringArray = slotsToFilter.copy();

        while (!filteringArray.isEmpty()) {
            JsonObject filteringSlot = (JsonObject) filteringArray.getJsonObject(0);
            filteringArray.remove(filteringSlot);
            filteringSlot.put("freebusy", "FREE");
            filteringSlot.remove("freebusytype");
            filteringSlot.remove("reservation");
            filteredArray.remove(filteringSlot);
        }

        return filteredArray;
    }

    private boolean isWellFormedSlot(ZonedDateTime slot) {
        // Check slot is wellformed (15 minutes, with minutes in 00, 15, 30 or 45, and seconds in 00), and not beyond 7 days, neither in the past
        if (slot.toLocalDate().isBefore(LocalDate.now()) ||
                slot.toLocalDate().isAfter(LocalDate.now().plusDays(6))) {
            return false;
        }

        int goalMinutes = slot.getMinute();
        if (goalMinutes == 0 || goalMinutes == 15 || goalMinutes == 30 || goalMinutes == 45) {
            return true;
        }

        return false;
    }
}
