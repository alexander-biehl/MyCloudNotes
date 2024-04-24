package com.alexbiehl.mycloudnotes.repository;

import com.alexbiehl.mycloudnotes.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Role findByName(final String name);
}