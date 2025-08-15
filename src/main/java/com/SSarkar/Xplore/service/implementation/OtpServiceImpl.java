package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.dto.auth.UserRegistrationRequestDTO;
import com.SSarkar.Xplore.model.OtpRegistrationData;
import com.SSarkar.Xplore.service.contract.OtpService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Integer EXPIRE_MINS = 5;

    private final LoadingCache<String, OtpRegistrationData> otpCache;

    public OtpServiceImpl() {
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public OtpRegistrationData load(String key) {
                        return null;
                    }
                });
    }

    @Override
    public String generateAndStoreOtp(String email, UserRegistrationRequestDTO registrationRequest) {
        String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
        otpCache.put(email, new OtpRegistrationData(otp, registrationRequest));
        return otp;
    }

    @Override
    public String getOtp(String email) {
        try {
            OtpRegistrationData data = otpCache.getIfPresent(email);
            return (data != null) ? data.getOtp() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public UserRegistrationRequestDTO getRegistrationRequest(String email) {
        try {
            OtpRegistrationData data = otpCache.getIfPresent(email);
            return (data != null) ? data.getRegistrationRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void clearOtp(String email) {
        otpCache.invalidate(email);
    }
}
