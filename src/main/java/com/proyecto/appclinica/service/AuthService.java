package com.proyecto.appclinica.service;

import com.proyecto.appclinica.model.dto.VerificationStatusResponseDto;
import com.proyecto.appclinica.model.dto.auth.CodeSubmissionResponseDto;
import com.proyecto.appclinica.model.dto.auth.VerifyCodeResponse;

public interface AuthService {
    VerificationStatusResponseDto checkUserExists(String identifier);

    VerifyCodeResponse verifyCode(String identifier, String code);

    CodeSubmissionResponseDto resendCode(String identifier);

    Object getAuthenticatedUser();
}
