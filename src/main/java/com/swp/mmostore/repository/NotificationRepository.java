package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Notification;
import com.swp.mmostore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository <Notification, Long> {
    Page<Notification> findByUserOrderByCreateAtDesc(User user, Pageable pageable);

    List<Notification> findTop20ByUserStatusAndIsDeletedOrderByCreateAtDesc(Boolean userStatus, Boolean isDeleted);

    long countByUserAndStatusAndIsDeleted(User user, String status, boolean isDelete);

    long countByUserAndStatus(User user, String status);

    Page<Notification> findByUserAndStatusOrderByCreateAtDesc(User user, String status, Pageable pageable);

    long countByUserAndStatusAndIsDeleted(User user, String status, Boolean isDeleted);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.status = :toStatus where n.user.id = :userId and n.status = :fromStatus")
    int updateStatusForUserId(@Param("userId") Long userId,
                              @Param("fromStatus") String fromStatus,
                              @Param("toStatus") String toStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.status = :toStatus where lower(n.user.email) = lower(:email) and n.status = :fromStatus")
    int updateStatusForUserEmail(@Param("email") String email,
                                 @Param("fromStatus") String fromStatus,
                                 @Param("toStatus") String toStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.status = :toStatus " +
            "where n.id in (:ids) and lower(n.user.email) = lower(:email) and n.status = :fromStatus")
    int updateStatusForIdsAndEmail(@Param("email") String email,
                                   @Param("ids") List<Long> ids,
                                   @Param("fromStatus") String fromStatus,
                                   @Param("toStatus") String toStatus);

    @Query("SELECT n FROM Notification n WHERE n.user.email = :email AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY n.createAt DESC")
    Page<Notification> findByUser_EmailAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(@Param("email") String email, @Param("search") String search, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.email = :email AND n.status = :status AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY n.createAt DESC")
    Page<Notification> findByUser_EmailAndStatusAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(@Param("email") String email, @Param("status") String status, @Param("search") String search, Pageable pageable);

}
