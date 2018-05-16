package info.ciclope.wotgate.thing.driver.gatekeeper;

import com.google.inject.Inject;
import info.ciclope.wotgate.thing.AbstractThing;
import info.ciclope.wotgate.thing.driver.gatekeeper.database.GatekeeperDatabase;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.AuthorityService;
import info.ciclope.wotgate.thing.driver.gatekeeper.interaction.UserService;
import info.ciclope.wotgate.thing.handler.HandlerRegister;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class GateKeeperThing extends AbstractThing {
    private final long processExecution = 60000;

    @Inject
    private UserService userService;

    @Inject
    private AuthorityService authorityService;

    @Inject
    private GatekeeperDatabase gatekeeperDatabase;
    private long deleteExpiredUserRegistrationsProcess;
    private long deleteExpiredPasswordRecoveriesProcess;

    @Override
    public void startThing(Handler<AsyncResult<Void>> handler) {
        gatekeeperDatabase.initDatabaseStorage(result -> {
            if (result.succeeded()) {
                this.userService = new UserService(gatekeeperDatabase, getVertx());
                deleteExpiredUserRegistrationsProcess = getVertx().setPeriodic(processExecution, userService::deleteExpiredUserRegistrations);
                deleteExpiredPasswordRecoveriesProcess = getVertx().setPeriodic(processExecution, userService::deleteExpiredPasswordRecoveries);
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }

    @Override
    public void addHandlers(HandlerRegister register) {
        register.addHandler(GateKeeperInfo.LOGIN, authorityService::login);
        register.addHandler(GateKeeperInfo.REGISTER, authorityService::register);
        register.addHandler(GateKeeperInfo.ACTIVATE_USER, authorityService::activateUser);
        register.addHandler(GateKeeperInfo.GET_USER, authorityService::getUser);

//        register.registerGetInteractionHandler(getThingDescription(), THING_INTERACTION_STATE, this::getState);
//        // Rol operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_ROLES, role::getAllRoles);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ROLE_BY_NAME, role::getRoleByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ROLES_BY_LEVEL, role::getRolesByLevel);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_ROLE, role::addRole);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_ROLE_BY_NAME, role::deleteRoleByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_USER_TO_ROLE, role::addUserToRole);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_FROM_ROLE, role::deleteUserFromRole);
//        // User operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_USERS, user::getAllUsers);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER, user::getUser);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_BY_NAME, user::getUserByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_BY_EMAIL, user::getUserByEmail);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER, user::deleteUser);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_BY_NAME, user::deleteUserByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CHANGE_USER_HASH, user::changeUserPassword);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REGISTER_USER, user::registerUser);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CONFIRM_USER_REGISTRATION, user::confirmUserRegistration);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_RECOVER_USER_HASH, user::recoverUserPassword);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_CONFIRM_HASH_RECOVERY, user::confirmPasswordRecovery);
//        // Reservation operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_RESERVATIONS_BY_DATE, calendar::getAllReservationsByDate);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_RESERVATIONS, calendar::getAllReservations);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_RESERVATIONS_BY_DATE, calendar::getUserReservationsByDate);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_RESERVATIONS_BY_NAME_AND_DATE, calendar::getUserReservationsByNameAndDate);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_USER_RESERVATIONS, calendar::getAllUserReservations);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ALL_USER_RESERVATIONS_BY_NAME, calendar::getAllUserReservationsByName);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_DATE_AVAILABILITY, calendar::getDateAvailability);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_ADD_USER_RESERVATION, calendar::addUserReservation);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_DELETE_USER_RESERVATION, calendar::deleteUserReservation);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_ONGOING_RESERVATION, calendar::getOngoingReservation);
//        // Authorization operations
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GENERATE_USER_TOKEN, authorityService::generateUserToken);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_REVOKE_USER_TOKEN, authorityService::revokeUserToken);
//        register.registerPostInteractionHandler(getThingDescription(), THING_INTERACTION_GET_USER_PERMISSIONS, authorityService::getUserPermissions);
//        getVertx().eventBus().consumer(ThingAddress.getThingInteractionAuthenticationAddress(), this::getAuthorization);
    }

    @Override
    public void stopThing(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
        getVertx().cancelTimer(deleteExpiredUserRegistrationsProcess);
        getVertx().cancelTimer(deleteExpiredPasswordRecoveriesProcess);
    }
}
