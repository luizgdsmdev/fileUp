package com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Roles;

import com.bytebybyte.fileup.Domain.Entities.Roles.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesInterface extends JpaRepository<Role, Long> {}
