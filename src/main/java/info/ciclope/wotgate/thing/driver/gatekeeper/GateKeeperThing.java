package info.ciclope.wotgate.thing.driver.gatekeeper;

import com.google.inject.Inject;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import info.ciclope.wotgate.thing.driver.gatekeeper.service.AuthorityService;
import info.ciclope.wotgate.thing.driver.gatekeeper.service.UserService;
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
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }
}
