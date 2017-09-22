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
import info.ciclope.wotgate.thing.component.ThingActionTask;
import info.ciclope.wotgate.thing.component.ThingRequest;
import info.ciclope.wotgate.thing.component.ThingResponse;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Calendar {
    private final GatekeeperDatabase database;

    public Calendar(GatekeeperDatabase database) {
        this.database = database;
    }


    public void getAllReservationsByDate(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String startDate;
        try {
            startDate = request.getBody().getString("startDate");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (startDate == null || startDate.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getAllReservationsByDate(startDate, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getAllReservations(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getAllReservations(result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    public void getUserReservationsByDate(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, startDate;
        try {
            name = request.getInteractionAuthorization().getUsername();
            startDate = request.getBody().getString("startDate");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || startDate == null || name.isEmpty() || startDate.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getReservationsByDateAndUser(name, startDate, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getUserReservationsByNameAndDate(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, startDate;
        try {
            name = request.getBody().getString("name");
            startDate = request.getBody().getString("startDate");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || startDate == null || name.isEmpty() || startDate.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getReservationsByDateAndUser(name, startDate, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getAllUserReservations(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getInteractionAuthorization().getUsername();
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getAllUserReservations(name, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getAllUserReservationsByName(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name;
        try {
            name = request.getBody().getString("name");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || name.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getAllUserReservations(name, result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getDateAvailability(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String startDate;
        try {
            startDate = request.getBody().getString("startDate");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (startDate == null || startDate.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getAllReservationsByDate(startDate, result -> {
            if (result.succeeded()) {
                JsonArray freeSlots = getFreeSlotsOnDate(result.result().getResult(), startDate);
                task.setOutputData(freeSlots);
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    public void addUserReservation(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, startDate, endDate;
        try {
            name = request.getInteractionAuthorization().getUsername();
            startDate = request.getBody().getString("startDate");
            endDate = request.getBody().getString("endDate");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || startDate == null || endDate == null ||
                name.isEmpty() || startDate.isEmpty() || endDate.isEmpty() ||
                !isWellFormedSlot(startDate, endDate)) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.addUserReservation(name, startDate, endDate, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void deleteUserReservation(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        String name, startDate;
        try {
            name = request.getInteractionAuthorization().getUsername();
            startDate = request.getBody().getString("startDate");
        } catch (ClassCastException exception) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        if (name == null || startDate == null || name.isEmpty() || startDate.isEmpty()) {
            message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), "").getResponse());
            return;
        }
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.deleteUserReservation(name, startDate, result -> {
            if (result.succeeded()) {
                if (result.result().getTotal() > 0) {
                    task.setStatus(ThingActionTask.TASK_STATUS_OK);
                    message.reply(new ThingResponse(HttpResponseStatus.NO_CONTENT, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                } else {
                    task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                    message.reply(new ThingResponse(HttpResponseStatus.BAD_REQUEST, new JsonObject(), task.getThingActionTaskJson()).getResponse());
                }
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });
    }

    public void getOngoingReservation(Message<JsonObject> message) {
        ThingRequest request = new ThingRequest(message.body());
        ThingActionTask task = new ThingActionTask(request.getBody());
        database.getOngoingReservation(result -> {
            if (result.succeeded()) {
                task.setOutputData(result.result().getResult());
                task.setStatus(ThingActionTask.TASK_STATUS_OK);
                message.reply(new ThingResponse(HttpResponseStatus.OK, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            } else {
                task.setStatus(ThingActionTask.TASK_STATUS_ERROR);
                message.reply(new ThingResponse(HttpResponseStatus.INTERNAL_ERROR, new JsonObject(), task.getThingActionTaskJson()).getResponse());
            }
        });

    }

    private JsonArray getFreeSlotsOnDate(JsonArray busySlots, String startDate) {
        ZonedDateTime goalDateTime;
        try {
            goalDateTime = ZonedDateTime.parse(startDate);
        } catch (DateTimeParseException exception) {
            return new JsonArray();
        }
        JsonArray allFreeSlots = generateAllRemainingDateSlots(goalDateTime);
        JsonArray allFreeSlotsCopy = allFreeSlots.copy();
        for (Object slot : busySlots) {
            JsonObject reservation = (JsonObject) slot;
            String slotStart = reservation.getString("startDate");
            for (Object freeSlot : allFreeSlotsCopy) {
                ZonedDateTime date, slotDate;
                try {
                    slotDate = ZonedDateTime.parse(slotStart);
                    date = ZonedDateTime.parse(((JsonObject) freeSlot).getString("startDate"));
                } catch (DateTimeParseException exception) {
                    return new JsonArray();
                }
                if (date.isEqual(slotDate)) {
                    allFreeSlots.remove(freeSlot);
                }
            }
        }

        return allFreeSlots;
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
            slotObject.put("startDate", startDate.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)).put("endDate", endDate.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            slotsArray.add(slotObject);
        }

        return slotsArray;
    }

    private boolean isWellFormedSlot(String startDate, String endDate) {
        ZonedDateTime startDateTime, endDateTime;
        try {
            startDateTime = ZonedDateTime.parse(startDate);
            endDateTime = ZonedDateTime.parse(endDate);
        } catch (DateTimeParseException exception) {
            return false;
        }

        // Check slot is wellformed (15 minutes, with minutes in 00, 15, 30 or 45, and seconds in 00), and not beyond 15 days, neither in the past
        if (startDateTime.toLocalDate().isBefore(LocalDate.now()) ||
                endDateTime.toLocalDate().isAfter(LocalDate.now().plusDays(14))) {
            return false;
        }

        int startMinutes = startDateTime.getMinute();
        if (startDateTime.plusMinutes(15).isEqual(endDateTime) && (
                startMinutes == 0 || startMinutes == 15 || startMinutes == 30 || startMinutes == 45)) {
            return true;
        }

        return false;
    }
}
