package io.github.rayoraq.parser.habr;

import io.github.rayoraq.parser.ParserSettings;

/**
 * Настройки парсинга для сайта habr.com.
 * Полностью совместимы с динамической (не статической) моделью конфигурации
 */
public class HabrSettings extends ParserSettings {

    public HabrSettings(int start, int end) {
        this.startPoint = start;
        this.endPoint = end;

        this.baseUrl = "https://habr.com/ru/all";
        this.prefix = "page{CurrentId}";
    }
}
