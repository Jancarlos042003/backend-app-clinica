package com.proyecto.appclinica.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactResponseDTO {

    private Long id;

    private String name;

    private String phoneNumber;

    private String relationship;
}
