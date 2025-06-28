package com.proyecto.appclinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Habilita la programación de tareas
public class AppClinicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppClinicaApplication.class, args);
    }

}
