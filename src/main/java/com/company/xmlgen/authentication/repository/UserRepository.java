package com.company.xmlgen.authentication.repository;

import com.company.xmlgen.authentication.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link UserEntity}.
 *
 * @see docs/11-implementation-guide/authentication.md §8
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
}
