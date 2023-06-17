package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findByBookerIdOrderByEndDateDesc(Long bookerId);

    Collection<Booking> findByBookerIdAndEndDateLessThanEqualOrderByEndDateDesc(Long bookerId, LocalDateTime endDate);

    Collection<Booking> findByBookerIdAndStartDateGreaterThanEqualOrderByEndDateDesc(Long bookerId, LocalDateTime startDate);

    Collection<Booking> findByBookerIdAndStatusOrderByEndDateDesc(Long bookerId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and ?2 >= b.startDate " +
            "and ?2 <= b.endDate " +
            "order by b.endDate desc")
    Collection<Booking> findByBookerIdAndCurrent(Long bookerId, LocalDateTime dateTime);

    Collection<Booking> findByItem_OwnerIdOrderByEndDateDesc(Long ownerId);

    Collection<Booking> findByItem_OwnerIdAndEndDateLessThanEqualOrderByEndDateDesc(Long ownerId, LocalDateTime endDate);

    Collection<Booking> findByItem_OwnerIdAndStartDateGreaterThanEqualOrderByEndDateDesc(Long ownerId, LocalDateTime startDate);

    Collection<Booking> findByItem_OwnerIdAndStatusOrderByEndDateDesc(Long ownerId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.item.ownerId = ?1 " +
            "and ?2 >= b.startDate " +
            "and ?2 <= b.endDate " +
            "order by b.endDate desc")
    Collection<Booking> findByItem_OwnerIdAndCurrent(Long ownerId, LocalDateTime dateTime);

    Booking findFirst1ByItemIdAndStartDateGreaterThanAndStatusOrderByStartDate(Long itemId, LocalDateTime dateTime, BookingStatus status);

    Booking findFirst1ByItemIdAndStartDateLessThanEqualAndStatusOrderByStartDateDesc(Long itemId, LocalDateTime dateTime, BookingStatus status);


    Collection<Booking> findByItemIdAndBookerIdAndEndDateLessThanAndStatus(Long itemId, Long bookerId, LocalDateTime dateTime, BookingStatus status);

}
