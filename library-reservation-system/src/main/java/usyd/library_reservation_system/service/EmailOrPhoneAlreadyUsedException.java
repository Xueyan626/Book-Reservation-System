package usyd.library_reservation_system.library_reservation_system.service;

public class EmailOrPhoneAlreadyUsedException extends RuntimeException {
    public EmailOrPhoneAlreadyUsedException(String msg) { super(msg); }
}
