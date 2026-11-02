package io.github.rayoraq.parser.theatre;

import io.github.rayoraq.parser.Parser;
import io.github.rayoraq.parser.model.Performance;
import io.github.rayoraq.parser.util.DateParser;
import io.github.rayoraq.parser.util.TheatreMappingSchema;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Асинхронный парсер театра "На Спасской"
 * Собирает все ссылки с главной страницы, скачивает их параллельно и агрегирует сеансы
 */
public class EkvusParser implements Parser<List<Performance>> {

    private static final Logger log = LoggerFactory.getLogger(EkvusParser.class);

    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "^.*?(?:час[а-я]{0,2}(?:\\s+\\d+\\s+минут[а-я]{0,2})?|минут[а-я]{0,2})(?:\\s+с\\s+антрактом)?"
    );

    private final String baseUrl;
    private final TheatreMappingSchema schema;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Semaphore networkSemaphore = new Semaphore(4);

    public EkvusParser(String baseUrl,TheatreMappingSchema schema) {
        this.baseUrl = baseUrl;
        this.schema = schema;
    }

    @Override
    public List<Performance> parse(Document mainDocument) {
        Map<String, Performance> groupedPerformances = new HashMap<>();

        Elements performanceLinks = mainDocument.select("a[href*=afisha/show/]");
        log.info("EkvusParser: Найдено {} ссылок на сеансы. Запуск асинхронного скачивания...", performanceLinks.size());

        List<String> detailUrls = performanceLinks.stream()
                .map(link -> link.attr("href"))
                .map(this::normalizeUrl)
                .distinct()
                .collect(Collectors.toList());

        List<CompletableFuture<Optional<Performance>>> futures = detailUrls.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> downloadAndParseCard(url), executor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<Optional<Performance>> future : futures) {
            Optional<Performance> performanceOpt = future.join();

            performanceOpt.ifPresent(p -> {
                String key = p.getTitle().toLowerCase();
                groupedPerformances.merge(key, p, (existing, next) -> {
                    existing.getDateTimes().addAll(next.getDateTimes());
                    return existing;
                });
            });
        }


        return new ArrayList<>(groupedPerformances.values());
    }

    /**
     * Внутренний изолированный метод для скачивания и парсинга ОДНОЙ карточки.
     * Он запускается в отдельном потоке для каждого URL.
     */
    /**
     * Внутренний изолированный метод для скачивания и парсинга ОДНОЙ карточки.
     * Реализует паттерн Retry Policy (повторные попытки при сетевых сбоях).
     */
    private Optional<Performance> downloadAndParseCard(String url) {
        int maxAttempts = 3;
        long retrySleepTimeMs = 2000;
        Document detailDoc = null;

        try {
            networkSemaphore.acquire();
            long jitter = ThreadLocalRandom.current().nextLong(300, 700);
            Thread.sleep(jitter);

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    detailDoc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(5000)
                            .get();
                    break;
                } catch (IOException e) {
                    log.warn("Попытка {} из {} не удалась для: {}. Причина: {}", attempt, maxAttempts, url, e.getMessage());
                    if (attempt == maxAttempts) {
                        return Optional.empty();
                    }
                    Thread.sleep(retrySleepTimeMs);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Поток парсинга карточки был прерван: {}", url, e);
            return Optional.empty();
        } finally {
            networkSemaphore.release();
        }

        try {
            Element h1El = detailDoc.selectFirst(schema.titleSelector());
            if (h1El == null) return Optional.empty();

            String title = extractTitle(h1El);
            if (title.equals("Спектакли") || title.isEmpty()) return Optional.empty();

            Performance performance = new Performance();
            performance.setTitle(title);
            performance.setDateTimes(new LinkedHashSet<>());
            performance.setAgeLimit(extractAgeLimit(detailDoc));
            performance.setImageUrl(extractImageUrl(detailDoc));
            performance.setDuration(extractDuration(detailDoc));

            Elements dateLinks = detailDoc.select(schema.dateLinksSelector());
            for (Element dateLink : dateLinks) {
                DateParser.parse(dateLink.text()).ifPresent(dt -> performance.getDateTimes().add(dt));
            }

            return Optional.of(performance);
        } catch (Exception e) {
            log.error("Ошибка разбора HTML-структуры карточки: {}", url, e);
            return Optional.empty();
        }
    }


    private String normalizeUrl(String href) {
        if (href.startsWith("/")) {
            return baseUrl + href;
        } else if (!href.startsWith("http")) {
            return baseUrl + "/" + href;
        }
        return href;
    }

    private String extractTitle(Element h1El) {
        String title = h1El.ownText().trim();
        if (title.isEmpty()) {
            title = h1El.text().replaceAll("\\s*\\d+\\+\\s*$", "").trim();
        }
        return title.replace("ПРЕМЬЕРА", "").replace("арт-кафе", "").trim();
    }

    private String extractAgeLimit(Document doc) {
        Element ageEl = doc.selectFirst(schema.ageLimitSelector());
        return ageEl != null ? ageEl.text().trim() : "";
    }

    private String extractImageUrl(Document doc) {
        Element imgEl = doc.selectFirst(schema.imageSelector());
        if (imgEl == null) {
            return baseUrl + "/templates/ekvus/images/logo.png";
        }
        String src = imgEl.attr("src");
        return src.startsWith("/") ? baseUrl + src : src;
    }

    private String extractDuration(Document doc) {
        String bodyText = doc.body().text();
        int idx = bodyText.indexOf("Продолжительность спектакля:");
        if (idx < 0) return null;

        String tail = bodyText.substring(idx + "Продолжительность спектакля:".length()).trim();
        Matcher durationMatcher = DURATION_PATTERN.matcher(tail);

        if (durationMatcher.find()) {
            return durationMatcher.group(0).trim();
        }

        int end = tail.indexOf('.');
        if (end < 0) end = Math.min(tail.length(), 35);
        return tail.substring(0, end).trim();
    }

    /**
     * Полезно закрывать пул потоков при завершении работы приложения,
     * чтобы предотвратить зависание JVM демонических потоков.
     */
    public void shutdown() {
        executor.shutdown();
    }
}
