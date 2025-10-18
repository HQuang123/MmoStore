package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;


public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Lấy danh sách đơn theo user
    Page<Order> findByUser_UserId(Integer userId, Pageable pageable);

    // Tìm theo user và mã đơn hàng
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.orderId = :orderId")
    Page<Order> findByUserAndOrderId(@Param("userId") Integer userId,
                                     @Param("orderId") Integer orderId,
                                     Pageable pageable);

    // Lọc theo user và khoảng thời gian
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.createAt BETWEEN :startDate AND :endDate")
    Page<Order> findByUserAndDateRange(@Param("userId") Integer userId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);
}

