package io.github.rayoraq.parser.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImageDownloader {

    private static final Logger log = LoggerFactory.getLogger(ImageDownloader.class);

    private final Path targetDir;

    public ImageDownloader(String dirPath) throws java.io.IOException {
        this.targetDir = Paths.get(dirPath);
        Files.createDirectories(this.targetDir);
    }

    public void download(String imageUrl, String baseName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("Нет URL картинки для '{}'", baseName);
            return;
        }
        if (baseName == null || baseName.isEmpty()) {
            log.warn("Нет имени файла для URL {}", imageUrl);
            return;
        }
        try {
            String ext = extractExtension(imageUrl);
            Path target = targetDir.resolve(baseName + "." + ext);
            if (Files.exists(target)) {
                log.debug("Файл уже существует: {}", target);
                return;
            }

            URLConnection conn = URI.create(imageUrl).toURL().openConnection();
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);

            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Скачано: {} -> {}", imageUrl, target);
        } catch (Exception e) {
            log.error("Ошибка скачивания {}: {}", imageUrl, e.getMessage(), e);
        }
    }

    private static String extractExtension(String url) {
        try {
            String path = URI.create(url).getPath();
            int dot = path.lastIndexOf('.');
            if (dot > 0 && dot < path.length() - 1) {
                String ext = path.substring(dot + 1).toLowerCase();
                if (ext.matches("[a-z0-9]{1,5}")) return ext;
            }
        } catch (Exception ignored) {}
        return "jpg";
    }
}