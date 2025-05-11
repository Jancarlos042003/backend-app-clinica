package com.proyecto.appclinica.controller;

import com.proyecto.appclinica.model.dto.RoleRequestDto;
import com.proyecto.appclinica.model.entity.RoleEntity;
import com.proyecto.appclinica.service.impl.RoleServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleServiceImpl roleService;

    @PostMapping
    public ResponseEntity<String> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        roleService.createRole(requestDto);
        return ResponseEntity.ok("Rol creado correctamente");
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateRole(@Valid @RequestBody RoleRequestDto requestDto, @PathVariable Long id) {
        roleService.update(requestDto, id);
        return ResponseEntity.ok("Rol actualizado correctamente");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok("Rol eliminado correctamente");
    }

    @GetMapping
    public ResponseEntity<Set<RoleEntity>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleEntity> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }
}
