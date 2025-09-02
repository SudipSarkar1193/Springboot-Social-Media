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

    @Override
    // Overloaded method to upload raw byte data
    public String upload(byte[] data) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(data, ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }

    @Override
    public String uploadVideo(byte[] data) throws IOException {
        // We tell Cloudinary to treat this as a video file
        Map<?, ?> uploadResult = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                "resource_type", "video"
        ));
        return (String) uploadResult.get("secure_url");
    }

    @Override
    public void delete(String imageUrl) throws IOException {
        // Extract the public ID from the URL
        String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}