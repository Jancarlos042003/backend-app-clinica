package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.auth.CredentialsRequestDto;

public interface CredentialsService {
    void createCredentials(CredentialsRequestDto credentialsRequestDto);
}
