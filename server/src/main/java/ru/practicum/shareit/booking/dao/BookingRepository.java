package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByBookerId(Long bookerId, Pageable page);

    Page<Booking> findByBookerIdAndEndDateLessThanEqual(Long bookerId, LocalDateTime endDate, Pageable page);

    Page<Booking> findByBookerIdAndStartDateGreaterThanEqual(Long bookerId, LocalDateTime startDate, Pageable page);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable page);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and ?2 >= b.startDate " +
            "and ?2 <= b.endDate ")
    Page<Booking> findByBookerIdAndCurrent(Long bookerId, LocalDateTime dateTime, Pageable page);

    Page<Booking> findByItem_OwnerId(Long ownerId, Pageable page);

    Page<Booking> findByItem_OwnerIdAndEndDateLessThanEqual(Long ownerId, LocalDateTime endDate, Pageable page);

    Page<Booking> findByItem_OwnerIdAndStartDateGreaterThanEqual(Long ownerId, LocalDateTime startDate, Pageable page);

    Page<Booking> findByItem_OwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable page);

    @Query("select b from Booking b " +
            "where b.item.ownerId = ?1 " +
            "and ?2 >= b.startDate " +
            "and ?2 <= b.endDate")
    Page<Booking> findByItem_OwnerIdAndCurrent(Long ownerId, LocalDateTime dateTime, Pageable page);

    Booking findFirst1ByItemIdAndStartDateGreaterThanAndStatusOrderByStartDate(Long itemId, LocalDateTime dateTime, BookingStatus status);

    Booking findFirst1ByItemIdAndStartDateLessThanEqualAndStatusOrderByStartDateDesc(Long itemId, LocalDateTime dateTime, BookingStatus status);


    Collection<Booking> findByItemIdAndBookerIdAndEndDateLessThanAndStatus(Long itemId, Long bookerId, LocalDateTime dateTime, BookingStatus status);

}
