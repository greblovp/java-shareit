package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findByBookerIdOrderByStartDateDesc(Long bookerId);

    Collection<Booking> findByBookerIdAndEndDateLessThanEqualOrderByStartDateDesc(Long bookerId, LocalDateTime endDate);

    Collection<Booking> findByBookerIdAndStartDateGreaterThanEqualOrderByStartDateDesc(Long bookerId, LocalDateTime startDate);

    Collection<Booking> findByBookerIdAndStatusOrderByStartDateDesc(Long bookerId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and ?2 >= b.startDate " +
            "and ?2 <= b.endDate " +
            "order by b.startDate desc")
    Collection<Booking> findByBookerIdAndCurrent(Long bookerId, LocalDateTime date);

    Collection<Booking> findByItem_OwnerIdOrderByStartDateDesc(Long ownerId);

    Collection<Booking> findByItem_OwnerIdAndEndDateLessThanEqualOrderByStartDateDesc(Long ownerId, LocalDateTime endDate);

    Collection<Booking> findByItem_OwnerIdAndStartDateGreaterThanEqualOrderByStartDateDesc(Long ownerId, LocalDateTime startDate);

    Collection<Booking> findByItem_OwnerIdAndStatusOrderByStartDateDesc(Long ownerId, BookingStatus status);

    @Query("select b from Booking b " +
            "where b.item.ownerId = ?1 " +
            "and ?2 >= b.startDate " +
            "and ?2 <= b.endDate ")
    Collection<Booking> findByItem_OwnerIdAndCurrent(Long ownerId, LocalDateTime date);

    Booking findFirst1ByItemIdAndStartDateGreaterThanOrderByStartDate(Long itemId, LocalDateTime dateTime);

    Booking findFirst1ByItemIdAndStartDateLessThanEqualOrderByStartDateDesc(Long itemId, LocalDateTime dateTime);


    Collection<Booking> findByItemId(Long itemId);

}
