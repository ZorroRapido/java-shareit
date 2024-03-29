package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public ResponseEntity<Object> createBooking(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> updateBookingStatus(Long userId, Boolean approved, Long bookingId) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        if (approved != null) {
            parameters.put("approved", approved);
            sb.append("?approved={approved}");
        }

        return patch("/" + bookingId + sb, userId, parameters, null);
    }

    public ResponseEntity<Object> getBooking(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllBookingsByUserId(Long userId, BookingState state, Integer from, Integer size) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        if (state != null) {
            parameters.put("state", state);
            sb.append("state={state}&");
        }

        if (from != null) {
            parameters.put("from", from);
            sb.append("from={from}&");
        }

        if (size != null) {
            parameters.put("size", size);
            sb.append("size={size}");
        }

        return get("?" + sb, userId, parameters);
    }

    public ResponseEntity<Object> getBookingForUserItems(Long userId, BookingState state, Integer from, Integer size) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        if (state != null) {
            parameters.put("state", state);
            sb.append("state={state}&");
        }

        if (from != null) {
            parameters.put("from", from);
            sb.append("from={from}&");
        }

        if (size != null) {
            parameters.put("size", size);
            sb.append("size={size}");
        }

        return get("/owner?" + sb, userId, parameters);
    }
}