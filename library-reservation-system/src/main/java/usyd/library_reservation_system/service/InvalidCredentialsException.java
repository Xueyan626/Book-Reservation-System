package usyd.library_reservation_system.library_reservation_system.service;

public class InvalidCredentialsException extends RuntimeException {
    private String field = "global";

    public InvalidCredentialsException() {
        super("Invalid email/telephone or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
