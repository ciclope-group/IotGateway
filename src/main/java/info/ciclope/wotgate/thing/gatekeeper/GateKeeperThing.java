package info.ciclope.wotgate.thing.gatekeeper;

import com.google.inject.Inject;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.gatekeeper.database.GatekeeperDatabase;
import info.ciclope.wotgate.thing.gatekeeper.service.AuthorityService;
import info.ciclope.wotgate.thing.gatekeeper.service.ReservationService;
import info.ciclope.wotgate.thing.gatekeeper.service.UserService;
import info.ciclope.wotgate.thing.handler.HandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class GateKeeperThing extends AbstractThing {

    @Inject
    private UserService userService;

    @Inject
    private AuthorityService authorityService;

    @Inject
    private ReservationService reservationService;

    @Inject
    private GatekeeperDatabase gatekeeperDatabase;

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        gatekeeperDatabase.initDatabaseStorage(handler);
    }

    @Override
    public void addHandlers(HandlerRegister register) {
        // Authority
        register.addHandler(GateKeeperInfo.LOGIN, authorityService::login);
        register.addHandler(GateKeeperInfo.REGISTER, authorityService::register);

        // User
        register.addHandler(GateKeeperInfo.GET_USER, userService::getUserByUsername);
        register.addHandler(GateKeeperInfo.GET_ALL_USERS, userService::getAllUsers);
        register.addHandler(GateKeeperInfo.ACTIVATE_USER, userService::activateUser);

        // Reservation
        register.addHandler(GateKeeperInfo.GET_RESERVATIONS_RANGE, reservationService::getAllReservationsInRange);
        register.addHandler(GateKeeperInfo.GET_RESERVATIONS_USER, reservationService::getAllReservationsOfUser);
        register.addHandler(GateKeeperInfo.CREATE_RESERVATION, reservationService::createReservation);
        register.addHandler(GateKeeperInfo.CANCEL_RESERVATION, reservationService::cancelReservation);
        register.addHandler(GateKeeperInfo.COMPLETE_RESERVATION, reservationService::completeReservation);
        register.addHandler(GateKeeperInfo.GET_ACTUAL_RESERVATION, reservationService::getActualReservation);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }
}
