package com.proyecto.appclinica.service.impl;

import com.proyecto.appclinica.exception.ResourceNotFoundException;
import com.proyecto.appclinica.model.entity.PatientEntity;
import com.proyecto.appclinica.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        PatientEntity patient = patientRepository.findByDni(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "DNI", identifier));

        List<SimpleGrantedAuthority> authorities = patient.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        return User.builder()
                .username(patient.getDni())
                .password(patient.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
