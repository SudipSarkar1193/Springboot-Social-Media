package com.SSarkar.Xplore.utils;


import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;

public class Cloudinary {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        Cloudinary cloudinary = new Cloudinary();
        System.out.println(cloudinary.config.cloudName);
    }
}
