package com.example.BookingRoom.Services;


import com.example.BookingRoom.Entities.Role;

public interface RoleService {
    public Role creer (Role role);
    Role getByNom(String nom);

}
