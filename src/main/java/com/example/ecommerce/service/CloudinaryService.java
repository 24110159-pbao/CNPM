package com.example.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType =
                file.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {
            return null;
        }

        Map<?, ?> result =
                cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "resource_type",
                                "image"
                        )
                );

        Object secureUrl =
                result.get("secure_url");

        if (secureUrl == null) {
            return null;
        }

        return secureUrl.toString();
    }

    public boolean deleteImage(
            String publicId
    ) {
        if (publicId == null
                || publicId.trim().isEmpty()) {
            return false;
        }

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
