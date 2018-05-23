package info.ciclope.wotgate.thing.gatekeeper.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.gatekeeper.model.Reservation;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
public class ReservationDao {
    private DatabaseStorage databaseStorage;

    @Inject
    public ReservationDao(@Named("gatekeeper") DatabaseStorage databaseStorage) {
        this.databaseStorage = databaseStorage;
    }

    public void getAllReservationsInRange(LocalDateTime start, LocalDateTime end,
                                          Handler<AsyncResult<List<Reservation>>> handler) {

        String query = "SELECT * FROM reservation WHERE startDate BETWEEN datetime(?) AND datetime(?);";
        JsonArray params = new JsonArray().add(start.toString()).add(end.toString());

        databaseStorage.queryWithParameters(query, params, result -> DatabaseResultParser.reservationList(result, handler));
    }

    public void getAllReservationsByUser(String username, Handler<AsyncResult<List<Reservation>>> handler) {
        String query = "SELECT * FROM reservation r JOIN user u on r.user_id = u.id WHERE u.username = ?";
        JsonArray params = new JsonArray().add(username);

        databaseStorage.queryWithParameters(query, params, result -> DatabaseResultParser.reservationList(result, handler));
    }

    public void createReservation(Reservation reservation, Handler<AsyncResult<Integer>> handler) {
        String query = "INSERT INTO reservation(startDate, endDate, user_id, dateCreated, status_id) VALUES(?, ?, ?, ?, 1);";
        JsonArray params = new JsonArray()
                .add(reservation.getStartDate().toString())
                .add(reservation.getEndDate().toString())
                .add(reservation.getUserId())
                .add(LocalDateTime.now().toString());

        databaseStorage.updateWithParameters(query, params, result -> DatabaseResultParser.insert(result, handler));
    }

    public void getActualReservation(Handler<AsyncResult<Reservation>> handler) {
        LocalDateTime dateTime = LocalDateTime.now();

        String query = "SELECT * FROM reservation WHERE startDate >= ? AND endDate <= ?";
        JsonArray params = new JsonArray().add(dateTime.toString()).add(dateTime.toString());

        databaseStorage.queryWithParameters(query, params, result -> {
            if (result.succeeded()) {
                if (result.result().getNumRows() != 0) {
                    handler.handle(Future.succeededFuture(new Reservation(result.result().getRows().get(0))));
                } else {
                    handler.handle(Future.succeededFuture(null));
                }
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }
}
