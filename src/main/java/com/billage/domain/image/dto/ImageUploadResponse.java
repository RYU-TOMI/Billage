package com.billage.domain.image.dto;

/** POST /api/images -> { "imageUrl": "https://.../images/xxx.jpg" } */
public record ImageUploadResponse(String imageUrl) {
}
