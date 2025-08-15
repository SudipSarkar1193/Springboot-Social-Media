package com.SSarkar.Xplore.service.implementation;

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
    private final LoadingCache<String, String> otpCache;

    public OtpServiceImpl() {
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String key) {
                        return "";
                    }
                });
    }

    @Override
    public String generateOtp(String key) {
        String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
        otpCache.put(key, otp);
        return otp;
    }

    @Override
    public String getOtp(String key) {
        try {
            return otpCache.get(key);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void clearOtp(String key) {
        otpCache.invalidate(key);
    }
}