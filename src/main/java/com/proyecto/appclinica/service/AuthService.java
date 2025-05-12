package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;

public interface AuthService {
    CodeSubmissionResponseDto checkUserExists(String identifier);
    VerifyCodeResponse verifyCode(String identifier, String code);
}
