package info.ciclope.wotgate.thing.gatekeeper.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.gatekeeper.database.ReservationDao;
import info.ciclope.wotgate.thing.gatekeeper.model.Reservation;
import info.ciclope.wotgate.thing.gatekeeper.model.ReservationStatus;
import info.ciclope.wotgate.thing.gatekeeper.model.User;
import info.ciclope.wotgate.util.Interval;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@Singleton
public class ReservationService {

    private UserService userService;
    private ReservationDao reservationDao;

    @Inject
    public ReservationService(UserService userService, ReservationDao reservationDao) {
        this.userService = userService;
        this.reservationDao = reservationDao;
    }

    public void getAllReservationsInRange(Message<JsonObject> message) {
        LocalDateTime start, end;

        try {
            start = LocalDate.parse(message.body().getString("start"), DateTimeFormatter.ISO_DATE).atStartOfDay();
            end = LocalDate.parse(message.body().getString("end"), DateTimeFormatter.ISO_DATE).atTime(23, 59);

            reservationDao.getAllReservationsInRange(start, end, result -> {
                if (result.succeeded()) {
                    JsonArray jsonArray = result.result().stream()
                            .map(JsonObject::mapFrom)
                            .collect(Collectors.collectingAndThen(Collectors.toList(), JsonArray::new));
                    message.reply(jsonArray);
                } else {
                    message.fail(HttpResponseStatus.INTERNAL_ERROR, "Error");
                }
            });
        } catch (DateTimeParseException e) {
            message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
        }
    }

    public void getAllReservationsOfUser(Message<JsonObject> message) {
        String username = message.body().getString("username");

        reservationDao.getAllReservationsByUser(username, result -> {
            if (result.succeeded()) {
                JsonArray jsonArray = result.result().stream()
                        .map(JsonObject::mapFrom)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), JsonArray::new));
                message.reply(jsonArray);
            } else {
                message.fail(HttpResponseStatus.INTERNAL_ERROR, "Error");
            }
        });
    }

    public void getActualReservation(Message<JsonObject> message) {
        reservationDao.getActualReservation(result -> {
            if (result.succeeded() && result.result() != null) {
                message.reply(JsonObject.mapFrom(result.result()));
            } else {
                message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
            }
        });
    }

    public void createReservation(Message<JsonObject> message) {
        try {
            Reservation reservation = message.body().getJsonObject("body").mapTo(Reservation.class);
            // Validate reservation time
            if (!reservation.validate()) {
                message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
                return;
            }

            // Check reservation is on the future
            if (!reservation.getStartDate().isAfter(LocalDateTime.now())) {
                message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
                return;
            }

            // Check reservation not overlap
            LocalDateTime startSearch = reservation.getStartDate().toLocalDate().atStartOfDay();
            LocalDateTime endSearch = reservation.getEndDate().toLocalDate().atTime(23, 59);
            Interval reservationInterval = new Interval(reservation);
            reservationDao.getAllReservationsInRange(startSearch, endSearch, resultReservations -> {
                if (resultReservations.result().stream()
                        .map(Interval::new)
                        .anyMatch(r -> r.overlap(reservationInterval))) {

                    message.fail(HttpResponseStatus.CONFLICT, "Conflict");
                    return;
                }

                // Get user
                userService.getUserByUsername(message.body().getString("username"), resultUser -> {
                    User user = resultUser.result();

                    // Set data of reservation
                    reservation.setStatus(ReservationStatus.PENDING);
                    reservation.setUserId(user.getId());
                    reservation.setDateCreated(LocalDateTime.now());

                    reservationDao.createReservation(reservation, resultReservation -> {
                        if (resultReservation.succeeded()) {
                            int reservationId = resultReservation.result();
                            message.reply(reservationId);
                        } else {
                            message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
                        }
                    });
                });

            });
        } catch (IllegalArgumentException e) {
            message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
        }
    }

    public void cancelReservation(Message<JsonObject> message) {
        int reservationId = message.body().getInteger("reservationId");
        boolean isAdmin = message.body().getBoolean("admin");
        String username = message.body().getString("username");

        if (isAdmin) {
            reservationDao.cancelReservation(reservationId, result -> {
                if (result.succeeded()) {
                    message.reply(null);
                } else {
                    message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
                }
            });
        } else {
            // Check that it's his own reservation
            userService.getUserByUsername(username, resultUser ->
                    reservationDao.getReservationById(reservationId, resultReservation -> {
                        if (resultReservation.succeeded() && resultReservation.result() != null) {
                            if (resultReservation.result().getUserId() == resultUser.result().getId()) {
                                reservationDao.cancelReservation(reservationId, result -> {
                                    if (result.succeeded()) {
                                        message.reply(null);
                                    } else {
                                        message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
                                    }
                                });
                            } else {
                                message.fail(HttpResponseStatus.FORBIDDEN, "Forbidden");
                            }
                        } else {
                            message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
                        }
                    }));
        }
    }

    public void completeReservation(Message<JsonObject> message) {
        int reservationId = message.body().getInteger("reservationId");

        reservationDao.completeReservation(reservationId, result -> {
            if (result.succeeded() && result.result().getUpdated() != 0) {
                message.reply(null);
            } else {
                message.fail(HttpResponseStatus.RESOURCE_NOT_FOUND, "Not Found");
            }
        });
    }
}
