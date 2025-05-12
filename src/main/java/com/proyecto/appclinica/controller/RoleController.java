package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.RoleRequestDto;
import com.proyecto.appclinica.model.entity.RoleEntity;
import com.proyecto.appclinica.service.impl.RoleServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleServiceImpl roleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        roleService.createRole(requestDto);
        return ResponseEntity.ok("Rol creado correctamente");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateRole(@Valid @RequestBody RoleRequestDto requestDto, @PathVariable Long id) {
        roleService.update(requestDto, id);
        return ResponseEntity.ok("Rol actualizado correctamente");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok("Rol eliminado correctamente");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Set<RoleEntity>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleEntity> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }
}
