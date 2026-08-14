package com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Roles;

import com.bytebybyte.fileup.Domain.Entities.Roles.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
