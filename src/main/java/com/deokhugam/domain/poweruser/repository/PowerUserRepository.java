package com.deokhugam.domain.poweruser.repository;

import com.deokhugam.domain.poweruser.entity.PowerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {
}
