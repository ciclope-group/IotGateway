package info.ciclope.wotgate.thing.gatekeeper.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.ciclope.wotgate.storage.DatabaseStorage;
import info.ciclope.wotgate.thing.gatekeeper.model.Reservation;
import info.ciclope.wotgate.thing.gatekeeper.model.ReservationStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.UpdateResult;

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

    public void getReservationById(int reservationId, Handler<AsyncResult<Reservation>> handler) {
        String query = "SELECT * FROM reservation WHERE id = ?";
        JsonArray params = new JsonArray().add(reservationId);

        databaseStorage.queryWithParameters(query, params, result -> DatabaseResultParser.reservation(result, handler));
    }

    public void getAllReservationsInRange(LocalDateTime start, LocalDateTime end,
                                          Handler<AsyncResult<List<Reservation>>> handler) {

        String query = "SELECT * FROM reservation WHERE startDate BETWEEN ? AND ?;";
        JsonArray params = new JsonArray().add(start.toString()).add(end.toString());

        databaseStorage.queryWithParameters(query, params, result -> DatabaseResultParser.reservationList(result, handler));
    }

    public void checkReservationInRange(LocalDateTime start, LocalDateTime end,
                                        Handler<AsyncResult<Reservation>> handler) {

        String query = "SELECT * FROM reservation WHERE (startDate BETWEEN ? AND ?) OR (endDate BETWEEN ? AND ?)";
        JsonArray params = new JsonArray().add(start.toString()).add(end.toString())
                .add(start.toString()).add(end.toString());

        databaseStorage.queryWithParameters(query, params, result -> DatabaseResultParser.reservation(result, handler));
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

        databaseStorage.queryWithParameters(query, params, result -> DatabaseResultParser.reservation(result, handler));
    }

    public void cancelReservation(long reservationId, Handler<AsyncResult<UpdateResult>> handler) {
        String query = "UPDATE reservation SET status_id = ? WHERE id = ?";
        JsonArray params = new JsonArray().add(ReservationStatus.CANCELED).add(reservationId);

        databaseStorage.updateWithParameters(query, params, handler);
    }

    public void completeReservation(long reservationId, Handler<AsyncResult<UpdateResult>> handler) {
        String query = "UPDATE reservation SET status_id = ? WHERE id = ?";
        JsonArray params = new JsonArray().add(ReservationStatus.COMPLETED).add(reservationId);

        databaseStorage.updateWithParameters(query, params, handler);
    }
}
