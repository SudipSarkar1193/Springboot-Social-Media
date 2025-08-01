package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.service.contract.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(String base64Image) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(base64Image, ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }
}