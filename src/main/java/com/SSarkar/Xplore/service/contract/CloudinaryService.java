package com.SSarkar.Xplore.service.contract;

import java.io.IOException;

public interface CloudinaryService {
    String upload(String base64Image) throws IOException;
}