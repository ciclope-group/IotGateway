package info.ciclope.wotgate.thing.driver.gatekeeper.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.http.HttpResponseStatus;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
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

    private GatekeeperDatabase database;

    @Inject
    public ReservationService(GatekeeperDatabase database) {
        this.database = database;
    }

    public void getAllReservationsInRange(Message<JsonObject> message) {
        LocalDateTime start, end;

        try {
            start = LocalDate.parse(message.body().getString("start"), DateTimeFormatter.ISO_DATE).atStartOfDay();
            end = LocalDate.parse(message.body().getString("end"), DateTimeFormatter.ISO_DATE).atTime(23, 59);

            database.getAllReservationsInRange(start, end, result -> {
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

}
