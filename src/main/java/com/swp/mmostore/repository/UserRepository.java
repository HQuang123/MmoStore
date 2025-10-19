package com.swp.mmostore.repository;

import com.swp.mmostore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByEmail(String email);

    public List<User> findByRole(String role);

    public User findByResetToken(String resetToken);

    public User findByProviderId(String providerId);

    @EntityGraph(attributePaths = "shop")
    @Query("""
                SELECT u FROM User u
                WHERE (:role IS NULL OR u.role = :role)
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
}
