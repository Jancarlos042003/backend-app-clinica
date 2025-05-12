package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.auth.AuthResponseDto;
import com.proyecto.appclinica.model.dto.auth.LoginRequestDto;

public interface LoginService {

    AuthResponseDto login(LoginRequestDto requestDto);
}
