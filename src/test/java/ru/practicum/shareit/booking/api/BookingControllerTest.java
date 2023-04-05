package ru.practicum.shareit.booking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private final BookingInputDto bookingInputDto = new BookingInputDto(
            1L,
            LocalDateTime.of(2030, 12, 25, 12, 0, 0),
            LocalDateTime.of(2030, 12, 26, 12, 0, 0));

    private final BookingDto bookingDto = new BookingDto(
            1L,
            LocalDateTime.of(2030, 12, 25, 12, 0, 0),
            LocalDateTime.of(2030, 12, 26, 12, 0, 0),
            new ItemDto(1L, "itemDto1", "description1", true, null, null),
            new UserDto(2L, "userDto2", "second@user.ru"),
            Status.WAITING);

    @Test
    void addBookingTest() throws Exception {
        when(bookingService.add(any(), any(Long.class)))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingInputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.get(any(Long.class), any(Long.class)))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString()), Status.class));
    }


    @Test
    void getAllBookingsByUserIdTest() throws Exception {
        when(bookingService.getAllBookingsByUserId(any(String.class), nullable(Integer.class), nullable(Integer.class),
                any(Long.class)))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].end", is(bookingDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void getAllBookingsForUserItemsTest() throws Exception {
        when(bookingService.getAllBookingsForUserItems(any(String.class), any(Integer.class),
                nullable(Integer.class), any(Long.class)))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner?from=0&size=10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].end", is(bookingDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.[0].item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void updateBookingStatusTest() throws Exception {
        when(bookingService.updateStatus(any(Long.class), any(Boolean.class), any(Long.class)))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/1")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, 1)
                        .queryParam("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        is(bookingDto.getStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end",
                        is(bookingDto.getEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }
}