package io.github.rayoraq.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.rayoraq.parser.habr.HabrParser;
import io.github.rayoraq.parser.habr.HabrSettings;
import io.github.rayoraq.parser.theatre.EkvusParser;
import io.github.rayoraq.parser.theatre.EkvusSettings;
import io.github.rayoraq.parser.theatre.TheatreParser;
import io.github.rayoraq.parser.theatre.TheatreSettings;
import io.github.rayoraq.parser.util.ImageDownloader;
import io.github.rayoraq.parser.util.JsonWriter;
import io.github.rayoraq.parser.model.Performance;
import io.github.rayoraq.parser.model.Publication;
import io.github.rayoraq.parser.model.Theater;
import org.example.theatre.*;
import io.github.rayoraq.parser.util.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final List<Publication> collectedPublications = new ArrayList<>();
    private static ImageDownloader habrImageDownloader;

    public static void main(String[] args) throws IOException {
        String mode = args.length > 0 ? args[0] : askMode();
        switch (mode) {
            case "1", "habr" -> runHabr(parseIntArg(args, 1, 3));   // по умолчанию 3 страницы
            case "2", "theatres" -> runTheatres();
            default -> log.error("Неизвестный режим - '{}'. Используйте '1' или '2'", mode);
        }
    }

    private static int parseIntArg(String[] args, int idx, int fallback) {
        if (args.length <= idx) return fallback;
        try {
            return Math.max(1, Integer.parseInt(args[idx]));
        } catch (NumberFormatException e) {
            log.warn("Не число: '{}', беру {}", args[idx], fallback);
            return fallback;
        }
    }

    private static void runHabr(int pages) throws IOException {
        log.info("Старт парсера habr.ru, страниц: {}", pages);
        collectedPublications.clear();

        int start = 1;
        int end = pages;

        habrImageDownloader = new ImageDownloader("images");

        ParserWorker<ArrayList<Publication>> parser = new ParserWorker<>(new HabrParser());
        parser.setParserSettings(new HabrSettings(start, end));
        parser.onNewDataList.add(new HabrNewData());
        parser.onCompletedList.add(new HabrCompleted());

        parser.start();
        log.info("Конец парсера habr.ru. Публикаций собрано: {}", collectedPublications.size());
    }

    private static String askMode() {
        System.out.println("Выберите данные для парсинга:");
        System.out.println("  1 - Habr");
        System.out.println("  2 - Театры Кирова");
        System.out.print("> ");
        try (Scanner sc = new Scanner(System.in)) {
            return sc.nextLine().trim();
        }
    }

    static class HabrNewData implements ParserWorker.OnNewDataHandler<ArrayList<Publication>> {
        @Override
        public void onNewData(Object sender, ArrayList<Publication> args) {
            for (Publication p : args) {
                collectedPublications.add(p);
                habrImageDownloader.download(p.getImages(), p.getId());
            }
            log.info("Накоплено публикаций: {}", collectedPublications.size());
        }
    }

    static class HabrCompleted implements ParserWorker.OnCompleted {
        @Override
        public void onCompleted(Object sender) {
            log.info("Парсинг Хабра завершен. Сохраняем JSON...");
            try {
                JsonWriter<Publication> publicationWriter = new JsonWriter<>(MAPPER, "output/publications.json");
                publicationWriter.write(collectedPublications);
            } catch (IOException e) {
                log.error("Не удалось сохранить JSON: {}", e.getMessage(), e);
            }
        }
    }

    private static void runTheatres() throws IOException {
        log.info("Старт парсера театров Кирова");

        EkvusSettings ekvuSettings = new EkvusSettings();
        HtmlLoader ekvuLoader = new HtmlLoader(ekvuSettings);
        EkvusParser ekvuParser = new EkvusParser(
                ekvuSettings.getBaseUrl(),
                SchemaFactory.createEkvusSchema()
        );
        List<Performance> ekvuPerformances = ekvuParser.parse(ekvuLoader.getSourceByPageId(1));
        Theater ekvu = new Theater("Театр на Спасской", ekvuPerformances);
        log.info("Театр на Спасской: {} спектаклей", ekvuPerformances.size());
        ekvuParser.shutdown();

        TheatreSettings theatreSettings = new TheatreSettings();
        HtmlLoader theatreLoader = new HtmlLoader(theatreSettings);
        TheatreParser theatreParser = new TheatreParser(
                theatreSettings.getBaseUrl(),
                SchemaFactory.createTheatreSchema()
        );
        List<Performance> theatrePerformances = theatreParser.parse(theatreLoader.getSourceByPageId(1));
        Theater dram = new Theater("Драмтеатр им. С.М. Кирова", theatrePerformances);
        log.info("Драмтеатр: {} спектаклей", theatrePerformances.size());
        List<Theater> theaters = List.of(ekvu, dram);
        JsonWriter<Theater> theaterWriter = new JsonWriter<>(MAPPER, "output/theatres.json");
        theaterWriter.write(theaters);

        log.info("Конец парсера театров Кирова");
    }
}
