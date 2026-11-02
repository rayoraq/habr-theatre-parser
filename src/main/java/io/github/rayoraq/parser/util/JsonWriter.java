package io.github.rayoraq.parser.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Универсальный компонент для записи списков любых объектов в JSON.
 * Соответствует принципам SOLID (Single Responsibility, Open-Closed) и DRY.
 */
public class JsonWriter<T> {

    private static final Logger log = LoggerFactory.getLogger(JsonWriter.class);

    private final ObjectMapper mapper;
    private final String outputPath;

    /**
     * Конструктор принимает уже готовый ObjectMapper (Dependency Injection).
     * Это позволяет переиспользовать один маппер по всему приложению.
     */
    public JsonWriter(ObjectMapper mapper, String outputPath) {
        this.mapper = mapper;
        this.outputPath = outputPath;
    }

    /**
     * Записывает список элементов любого типа T в файл.
     */
    public void write(List<T> data) throws IOException {
        if (data == null) {
            log.warn("Попытка записать null-list в файл: {}", outputPath);
            return;
        }
        Path path = Paths.get(outputPath);
        ensureParentDirectoriesExist(path);
        File file = path.toFile();
        mapper.writeValue(file, data);
        log.info("JSON успешно сохранен: {} (Элементов: {}, Размер: {} байт)", file.getAbsolutePath(), data.size(), file.length());
    }

    /**
     * Безопасное создание директорий с использованием современного java.nio.file API
     */
    private void ensureParentDirectoriesExist(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                log.error("Критическая ошибка: Не удалось создать директории для пути {}", parent.toAbsolutePath(), e);
                throw new IOException("Не удалось создать папку: " + parent.toAbsolutePath(), e);
            }
        }
    }
}
