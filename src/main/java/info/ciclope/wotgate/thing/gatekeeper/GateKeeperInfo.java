package info.ciclope.wotgate.thing.gatekeeper;

public class GateKeeperInfo {
    public static final String NAME = "gatekeeper";

    // Authority
    public static final String LOGIN = ".login";
    public static final String REGISTER = ".register";

    // User
    public static final String GET_USER = ".getUser";
    public static final String GET_ALL_USERS = ".getAllUsers";
    public static final String ACTIVATE_USER = ".activateUser";

    // Reservation
    public static final String GET_RESERVATIONS_RANGE = ".getReservationsRange";
    public static final String CREATE_RESERVATION = ".createReservation";
    public static final String CANCEL_RESERVATION = ".cancelReservation";
    public static final String COMPLETE_RESERVATION = ".completeReservation";
}
