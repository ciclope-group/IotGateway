package info.ciclope.wotgate.thing.gatekeeper.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.gatekeeper.database.ReservationDao;
import info.ciclope.wotgate.thing.gatekeeper.model.Reservation;
import info.ciclope.wotgate.thing.gatekeeper.model.ReservationStatus;
import info.ciclope.wotgate.thing.gatekeeper.model.User;
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

    public void createReservation(Message<JsonObject> message) {
        try {
            Reservation reservation = message.body().getJsonObject("body").mapTo(Reservation.class);
            if (!reservation.validate()) {
                message.fail(HttpResponseStatus.BAD_REQUEST, "Bad Request");
                return;
            }

            // Check reservation not overlap
            reservationDao.getAllReservationsInRange(reservation.getStartDate(), reservation.getEndDate(), result -> {
                if (!result.result().isEmpty()) {
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

    }

    public void completeReservation(Message<JsonObject> message) {

    }

}
