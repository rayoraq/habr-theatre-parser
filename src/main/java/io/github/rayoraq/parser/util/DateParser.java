package io.github.rayoraq.parser.util;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class DateParser {

    private static final Locale RU_LOCALE = new Locale("ru");

    // 1. Формат Драмтеатра/Эквиуса: "18 октября 2026 в 11:00"
    private static final DateTimeFormatter WITH_YEAR_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy в HH:mm", RU_LOCALE);

    // 2. Формат Хабра: "18 октября в 11:00"
    private static final DateTimeFormatter WITHOUT_YEAR_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("d MMMM в HH:mm")
            .parseDefaulting(ChronoField.YEAR, Year.now().getValue()) // Автоподстановка текущего года (2026)
            .toFormatter(RU_LOCALE);

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            WITH_YEAR_FORMATTER,
            WITHOUT_YEAR_FORMATTER
    );

    private DateParser() {}

    /**
     * Универсальный метод парсинга даты.
     * Перебирает известные форматы, пока не найдет подходящий.
     *
     * @param raw Сырая строка с датой из HTML
     * @return Optional с LocalDateTime, либо Optional.empty() если формат не поддерживается
     */
    public static Optional<LocalDateTime> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String cleaned = raw.trim().replaceAll("\\s+", " ");

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDateTime dt = LocalDateTime.parse(cleaned, formatter);
                return Optional.of(dt);
            } catch (DateTimeParseException ignore) {
                //следующий формат
            }
        }

        return Optional.empty();
    }
}
