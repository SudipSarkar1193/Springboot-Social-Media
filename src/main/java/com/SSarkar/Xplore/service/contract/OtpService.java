package com.SSarkar.Xplore.service.contract;

public interface OtpService {
    String generateOtp(String key);

    String getOtp(String key);

    void clearOtp(String key);
}
