package com.swp.mmostore.repository;

import com.swp.mmostore.dto.OrderEvent;
import com.swp.mmostore.dto.OrderStatisticDTO;
import com.swp.mmostore.dto.ProductSalesDTO;
import com.swp.mmostore.dto.ShopOrderHistoryDTO;
import com.swp.mmostore.entity.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Integer> {



    @Query("""
    SELECT new com.swp.mmostore.dto.OrderStatisticDTO(
        o.orderId,
        p.title,
        o.createAt,
        o.status,
        o.totalPrice,
        COALESCE(d.paymentMethod, 'N/A')
    )
    FROM Order o
    LEFT JOIN o.product p
    LEFT JOIN Deposit d ON d.order = o
    WHERE o.user.userId = :userId
      AND (:productName IS NULL OR p.title LIKE CONCAT('%', :productName, '%'))
      AND (:startDate IS NULL OR o.createAt >= :startDate)
      AND (:endDate IS NULL OR o.createAt < :endDate)
      AND (:status IS NULL OR o.status = :status)
      AND (:paymentMethod IS NULL OR d.paymentMethod = :paymentMethod)
      AND (:minTotal IS NULL OR o.totalPrice >= :minTotal)
      AND (:maxTotal IS NULL OR o.totalPrice <= :maxTotal)
      AND (:orderId IS NULL OR CAST(o.orderId AS string) LIKE CONCAT('%', :orderId, '%'))
""")
    Page<OrderStatisticDTO> findOrderHistoryByUserId(
            @Param("userId") Long userId,
            @Param("orderId") String orderId,
            @Param("productName") String productName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod,
            @Param("minTotal") BigDecimal minTotal,
            @Param("maxTotal") BigDecimal maxTotal,
            Pageable pageable
    );



    // Tổng số đơn hàng theo shop
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.shop.shopId = :shopId")
    long countByShopId(@Param("shopId") Integer shopId);

    // Tổng doanh thu (chỉ tính đơn hoàn thành)
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.product.shop.shopId = :shopId AND o.status = 'COMPLETED'")
    BigDecimal sumRevenueByShop(@Param("shopId") Integer shopId);

    // Đếm số đơn theo trạng thái (Pending / Completed / Canceled)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.shop.shopId = :shopId AND o.status = :status")
    long countByShopIdAndStatus(@Param("shopId") Integer shopId, @Param("status") String status);



    @Query("""
    SELECT new com.swp.mmostore.dto.ProductSalesDTO(
        p.productId,
        p.title,
        p.price,
        SUM(o.quantity),
        SUM(o.totalPrice)
    )
    FROM Order o
    JOIN o.product p
    JOIN p.shop s
    WHERE s.shopId = :shopId
      AND o.status = 'COMPLETED'
    GROUP BY p.productId, p.title, p.price
    ORDER BY SUM(o.totalPrice) DESC
""")
    Page<ProductSalesDTO> findSoldProductsByShop(
            @Param("shopId") Integer shopId,
            Pageable pageable
    );




    @Query("""
        SELECT new com.swp.mmostore.dto.ShopOrderHistoryDTO(
            o.orderId,
            p.productId,
            p.title,
            o.quantity,
            p.price,
            o.totalPrice,
            o.createAt,
            o.status
        )
        FROM Order o
        JOIN o.product p
        JOIN p.shop s
        WHERE s.shopId = :shopId
        ORDER BY o.createAt DESC
    """)
    Page<ShopOrderHistoryDTO> findOrderHistoryByShop(
            @Param("shopId") Integer shopId,
            Pageable pageable
    );




    @Query("""
        SELECT new com.swp.mmostore.dto.ShopOrderHistoryDTO(
            o.orderId,
            p.productId,
            p.title,
            o.quantity,
            p.price,
            o.totalPrice,
            o.createAt,
            o.status
        )
        FROM Order o
        JOIN o.product p
        JOIN p.shop s
        WHERE s.shopId = :shopId
          AND (:minQuantity IS NULL OR o.quantity >= :minQuantity)
          AND (:minTotal IS NULL OR o.totalPrice >= :minTotal)
          AND (:maxTotal IS NULL OR o.totalPrice <= :maxTotal)
          AND (:startDate IS NULL OR o.createAt >= :startDate)
          AND (:endDate IS NULL OR o.createAt <= :endDate)
          AND (:status IS NULL OR o.status = :status)
        ORDER BY o.createAt DESC
    """)
    Page<ShopOrderHistoryDTO> findFilteredOrdersByShop(
            @Param("shopId") Integer shopId,
            @Param("minQuantity") Integer minQuantity,
            @Param("minTotal") BigDecimal minTotal,
            @Param("maxTotal") BigDecimal maxTotal,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
        SELECT new com.swp.mmostore.dto.OrderEvent(
                o.orderId,
                o.user.userId,
                o.product.productId,
                o.quantity,
                o.totalPrice,
                o.status
            )
        from Order o
        where o.status = :status
        and o.orderId > :lastestOrderId
        order by o.orderId asc
    """)
    List<OrderEvent> findByStatusAfter(@Param("status") String status, @Param("lastestOrderId") Integer lastestOrderId);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :status WHERE o.orderId = :orderId")
    void updateOrderStatus(@Param("orderId") Integer orderId, @Param("status") String status);
}





