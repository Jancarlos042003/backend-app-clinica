package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.auth.ResponseUserExistsDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;

public interface AuthService {
    ResponseUserExistsDto checkUserExists(String identifier);
    VerifyCodeResponse verifyCode(String identifier, String code);
}
