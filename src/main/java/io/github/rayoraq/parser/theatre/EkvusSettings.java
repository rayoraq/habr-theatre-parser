package io.github.rayoraq.parser.theatre;

import io.github.rayoraq.parser.ParserSettings;

public class EkvusSettings extends ParserSettings {
    public EkvusSettings() {
        this.startPoint = 1;
        this.endPoint = 1;
        this.baseUrl = "https://ekvus-kirov.ru";
        this.prefix = "afisha";

    }
}
