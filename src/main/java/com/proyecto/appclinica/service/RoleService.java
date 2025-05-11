package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.RoleRequestDto;
import com.proyecto.appclinica.model.entity.RoleEntity;

import java.util.Set;

public interface RoleService {

    RoleEntity findByName(String name);

    RoleEntity createRole(RoleRequestDto dto);

    RoleEntity update(RoleRequestDto dto, Long id);

    void delete(Long id);

    Set<RoleEntity> getAllRoles();

    RoleEntity findById(Long id);
}
