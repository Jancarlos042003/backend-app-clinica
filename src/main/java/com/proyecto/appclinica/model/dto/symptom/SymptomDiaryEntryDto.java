package com.proyecto.appclinica.model.dto.symptom;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomDiaryEntryDto {
    @NotBlank
    private String patientId;

    @NotNull
    private LocalDateTime recordDateTime = LocalDateTime.now();

    @NotEmpty
    @Valid                       // <— para validar cada SymptomDTO
    private List<SymptomDto> symptoms;

    @Size(max = 1000)
    private String notes;
}
