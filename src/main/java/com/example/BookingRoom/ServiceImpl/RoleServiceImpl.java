package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Role;
import com.example.BookingRoom.Repository.RoleRepository;
import com.example.BookingRoom.Services.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    private RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role creer(Role role) {
        return this.roleRepository.save(role);
    }

    @Override
    public Role getByNom(String nom) {
        return roleRepository.findByNom(nom);
    }
}
