package com.SSarkar.Xplore.dto.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class VerifyOtpRequestDTO {
    private String email;
    private String otp;
}
