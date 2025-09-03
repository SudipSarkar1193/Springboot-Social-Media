package com.SSarkar.Xplore.service.contract;

import java.io.IOException;
import java.io.InputStream;

public interface CloudinaryService {
    String upload(String base64Image) throws IOException;
    String upload(byte[] data) throws IOException; // Overloaded method to upload raw byte data
    String upload(InputStream inputStream) throws IOException; // Overloaded method to upload from InputStream
    String uploadVideo(byte[] data) throws IOException;
    String uploadVideo(InputStream inputStream) throws IOException; // Overloaded method to upload video from InputStream
    void delete(String imageUrl) throws IOException;
}