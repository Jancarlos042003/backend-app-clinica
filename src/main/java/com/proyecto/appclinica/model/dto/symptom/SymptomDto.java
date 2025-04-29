package com.proyecto.appclinica.model.dto.symptom;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomDto {
    @NotBlank
    private String code;

    @NotBlank
    private String description;

    @NotNull
    @Min(1) @Max(10)
    private Integer severity;

    @NotBlank
    private String bodySite;

    @NotNull
    private LocalTime onsetTime;
}
