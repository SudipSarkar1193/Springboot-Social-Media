package com.SSarkar.Xplore.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequestDTO {
    private String email;
    private String otp;
}
