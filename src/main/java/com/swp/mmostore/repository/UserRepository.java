package com.swp.mmostore.repository;

import com.swp.mmostore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false AND u.status= true")
    User findByEmail(@Param("email") String email);



    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    User findActiveByEmail(@Param("email") String email);

    public List<User> findByRole(String role);

    public User findByProviderId(String providerId);

    @EntityGraph(attributePaths = "shop")
    @Query("""
                SELECT u FROM User u
                WHERE (:role IS NULL OR u.role LIKE LOWER(CONCAT('%', :role, '%')))
                  AND (:status IS NULL OR u.status = :status)
                  AND (:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<User> findFilteredWithShop(@Param("role") String role,
                                    @Param("status") Boolean status,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    @EntityGraph(attributePaths = "shop")
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.role) LIKE CONCAT('%', LOWER(:role), '%') " +
            "AND u.isDeleted = :isDeleted")
    List<User> findAllByRoleIgnoreCaseAndIsDeleted(@Param("role") String role,
                                                             @Param("isDeleted") Boolean isDeleted);


    // no validate data when reset password
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    void updatePasswordByEmail(@Param("password") String password, @Param("email") String email);


}
