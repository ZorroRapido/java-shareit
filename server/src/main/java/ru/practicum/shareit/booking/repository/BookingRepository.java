package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Pageable pageable);

    Page<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end,
                                                              Pageable pageable);

    Page<Booking> findByBookerIdAndStatusAndEndIsBefore(Long bookerId, Status status, LocalDateTime end,
                                                        Pageable pageable);

    Page<Booking> findByBookerIdAndStatusInAndStartIsAfter(Long bookerId, List<Status> statuses, LocalDateTime start,
                                                           Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.id in (" +
            "      select i.id from Item i" +
            "       where i.owner.id = ?1) " +
            "order by b.start desc")
    Page<Booking> findAllBookingsByOwner(Long ownerIds, Pageable pageable);

    @Query("select b from Booking b " +
            "where b.item.id in (" +
            "      select i.id from Item i" +
            "       where i.owner.id = ?1) " +
            "  and b.status in ?2 " +
            " order by b.start desc")
    Page<Booking> findAllBookingsByOwner(Long ownerId, List<Status> statuses, Pageable pageable);

    List<Booking> findAllBookingsByItemId(Long itemId);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "  and b.item.id = ?2 " +
            "  and b.status = 'APPROVED' " +
            "  and b.start < ?3")
    List<Booking> findPastAndCurrentActiveBookingsByBookerIdAndItemId(Long userId, Long itemId, LocalDateTime dateTime);
}
