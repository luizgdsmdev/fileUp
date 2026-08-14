package com.bytebybyte.fileup.Application.Interfaces.Auth;

import com.bytebybyte.fileup.Application.DTOs.Request.Auth.LoginRequest;
import com.bytebybyte.fileup.Application.DTOs.Request.Auth.RegisterRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Auth.LoginResponse;
import com.bytebybyte.fileup.Application.Utils.Auth.JwtClaimsData;
import org.springframework.http.ResponseEntity;

public interface IAuthService {
    ResponseEntity<LoginResponse> login(LoginRequest loginRequest);
    ResponseEntity<LoginResponse> register(RegisterRequest registerRequest);

    //  ------- Supportive methods -------
    private void _validatePassword(String rawPassword, String hashedPassword){};
    private JwtClaimsData _generateJwtClaimsSet(String userId){ return null;};
}
