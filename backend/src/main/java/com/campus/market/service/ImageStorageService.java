package com.campus.market.service;

import com.campus.market.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg","jpeg","png","webp","gif");
    private final Path productDir;
    public ImageStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.productDir = Path.of(uploadDir).toAbsolutePath().normalize().resolve("products");
    }
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException(40001,"请选择图片");
        if (file.getSize() > 5L * 1024 * 1024) throw new BusinessException(40001,"图片大小不能超过 5MB");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) throw new BusinessException(40001,"只能上传图片文件");
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String extension = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) throw new BusinessException(40001,"仅支持 jpg、png、webp、gif 图片");
        try {
            Files.createDirectories(productDir);
            String filename = UUID.randomUUID() + "." + extension;
            Files.copy(file.getInputStream(), productDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            throw new BusinessException(50000,"图片保存失败");
        }
    }
}
