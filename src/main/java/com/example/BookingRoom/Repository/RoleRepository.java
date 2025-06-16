package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByNom(String nom);
}
