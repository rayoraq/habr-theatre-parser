package io.github.rayoraq.parser.theatre;

import io.github.rayoraq.parser.ParserSettings;

public class TheatreSettings extends ParserSettings {
    public TheatreSettings() {
        this.startPoint = 1;
        this.endPoint = 1;
        this.baseUrl = "https://kirovdramteatr.ru";
        this.prefix = "afisha";
    }
}
