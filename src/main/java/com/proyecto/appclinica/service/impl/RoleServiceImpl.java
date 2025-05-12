package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.dto.RoleRequestDto;
import com.proyecto.appclinica.model.entity.RoleEntity;
import com.proyecto.appclinica.repository.RoleRepository;
import com.proyecto.appclinica.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleEntity findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", name));
    }

    @Override
    public RoleEntity createRole(RoleRequestDto dto) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(dto.name().toUpperCase());

        return roleRepository.save(roleEntity);
    }

    @Override
    public RoleEntity update(RoleRequestDto dto, Long id) {
        return roleRepository.findById(id)
                // Utilizamos el map() de Optional para actualizar el rol existente
                .map(existingRole -> {
                    existingRole.setName(dto.name());
                    return roleRepository.save(existingRole);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));
    }

    @Override
    public void delete(Long id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));
        roleRepository.delete(role);
    }

    @Override
    public Set<RoleEntity> getAllRoles() {
        Set<RoleEntity> roles = Set.copyOf(roleRepository.findAll());
        if (roles.isEmpty()) {
            throw new ResourceNotFoundException("No hay roles disponibles");
        }
        return roles;
    }

    @Override
    public RoleEntity findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));
    }
}
