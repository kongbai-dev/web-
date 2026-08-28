package com.campus.market.controller;

import com.campus.market.common.ApiResponse;
import com.campus.market.service.ImageStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController @RequestMapping("/api/uploads")
public class UploadController {
    private final ImageStorageService storage;
    public UploadController(ImageStorageService storage) { this.storage = storage; }
    @PostMapping("/images")
    public ApiResponse<Map<String,String>> image(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(Map.of("url", storage.store(file)));
    }
}
