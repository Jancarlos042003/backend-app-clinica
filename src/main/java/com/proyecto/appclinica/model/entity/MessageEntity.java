package com.proyecto.appclinica.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "messages", indexes = {
        @Index(name = "idx_session_created", columnList = "session_id, created_at") // Índice compuesto
})
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El rol no puede estar vacío.")
    private String rol;

    @Column(columnDefinition = "TEXT") // Para permitir mensajes largos
    @NotBlank(message = "El contenido del mensaje no puede estar vacío.")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotBlank(message = "El ID de sesión no puede estar vacío.")
    @Column(name = "session_id")
    private String sessionId;
}
