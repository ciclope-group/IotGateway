package info.ciclope.wotgate.thing.gatekeeper.database;

import info.ciclope.wotgate.thing.gatekeeper.model.Reservation;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;
import java.util.stream.Collectors;

class DatabaseResultParser {

    static void reservationList(AsyncResult<ResultSet> result, Handler<AsyncResult<List<Reservation>>> handler) {
        if (result.succeeded()) {
            List<Reservation> reservationList = result.result().getRows().stream()
                    .map(Reservation::new)
                    .collect(Collectors.toList());

            handler.handle(Future.succeededFuture(reservationList));
        } else {
            handler.handle(Future.failedFuture(result.cause()));
        }
    }

    static void insert(AsyncResult<UpdateResult> result, Handler<AsyncResult<Integer>> handler) {
        if (result.succeeded()) {
            handler.handle(Future.succeededFuture(result.result().getKeys().getInteger(0)));
        } else {
            handler.handle(Future.failedFuture(result.cause()));
        }
    }
}
