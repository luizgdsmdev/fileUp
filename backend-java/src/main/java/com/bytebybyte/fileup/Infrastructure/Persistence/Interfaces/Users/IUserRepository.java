package com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Users;

import com.bytebybyte.fileup.Domain.Entities.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * UserEntity repository interface.
 * Extend JpaRepository to use JPA features.
 * @method  findByEmail(string email) -> Optional<UserEntity>
 */
@Repository
public interface IUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}
