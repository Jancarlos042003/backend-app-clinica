package com.proyecto.appclinica.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private String id;
    private String name;
    private String email;
    // Más datos del usuario
}
