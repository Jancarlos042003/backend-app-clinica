package com.proyecto.appclinica.model.dto;

import com.proyecto.appclinica.model.entity.ESosStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SosUpdateRequest {
    private Long id;

    @NotNull(message = "El estado no puede ser nulo")
    private ESosStatus status;
}
