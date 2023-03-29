package ru.practicum.shareit.exception;

public class NotAvailableForBookingException extends RuntimeException {
    public NotAvailableForBookingException(String msg) {
        super(msg);
    }
}
