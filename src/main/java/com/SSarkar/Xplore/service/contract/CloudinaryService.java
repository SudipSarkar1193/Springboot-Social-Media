package com.SSarkar.Xplore.service.contract;

import java.io.IOException;

public interface CloudinaryService {
    String upload(String base64Image) throws IOException;
    String upload(byte[] data) throws IOException; // Overloaded method to upload raw byte data
    void delete(String imageUrl) throws IOException;
}