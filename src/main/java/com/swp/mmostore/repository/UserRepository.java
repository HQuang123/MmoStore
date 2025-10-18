package com.swp.mmostore.repository;

import com.swp.mmostore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
                SELECT u FROM User u
                WHERE (:status IS NULL OR u.status = :status)
                  AND (:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  AND (
                      :role IS NULL
                      OR :role = ''
                      OR u.role LIKE CONCAT('%', :role, '%')
                  )
            """)
    Page<User> findFiltered(@Param("role") String role,
                            @Param("status") Boolean status,
                            @Param("keyword") String keyword,
                            Pageable pageable);

}
