package com.proyecto.appclinica.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "medication_settings_id")
    private MedicationSettings medicationSettings;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_settings_id", referencedColumnName = "id")
    @Builder.Default
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public void addEmergencyContact(EmergencyContact contact) {
        if (contact != null) {
            emergencyContacts.add(contact);
        }
    }

    public void removeEmergencyContact(EmergencyContact contact) {
        if (contact != null) {
            emergencyContacts.remove(contact);
        }
    }
}
