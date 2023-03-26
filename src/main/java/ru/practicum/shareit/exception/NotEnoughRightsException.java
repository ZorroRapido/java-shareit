package ru.practicum.shareit.exception;


public class NotEnoughRightsException extends RuntimeException {
    public NotEnoughRightsException(String msg) {
        super(msg);
    }
}
