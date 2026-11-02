package io.github.rayoraq.parser.theatre;

import io.github.rayoraq.parser.Parser;
import io.github.rayoraq.parser.model.Performance;
import io.github.rayoraq.parser.util.DateParser;
import io.github.rayoraq.parser.util.TheatreMappingSchema;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TheatreParser implements Parser<List<Performance>> {

    private static final Logger log = LoggerFactory.getLogger(TheatreParser.class);
    private final String baseUrl;
    private final TheatreMappingSchema schema;

    public TheatreParser(String baseUrl, TheatreMappingSchema schema) {
        this.baseUrl = baseUrl;
        this.schema = schema;
    }

    @Override
    public List<Performance> parse(Document document) {
        Map<String, Performance> groupedPerformances = new HashMap<>();
        Elements blocks = document.select("div.t_afisha");
        log.info("TheatreParser: Найдено блоков t_afisha: {}", blocks.size());

        for (Element block : blocks) {
            try {
                Element titleEl = block.selectFirst(schema.titleSelector());
                if (titleEl == null) continue;
                String titleRaw = titleEl.text().trim();

                String title = titleRaw.replaceAll("\\s*\\d+\\+\\s*$", "").trim();
                String key = title.toLowerCase();

                String dateText = "";
                Element dateEl = block.selectFirst(schema.dateLinksSelector());
                if (dateEl != null) {
                    dateText = dateEl.text().trim();
                }

                DateParser.parse(dateText).ifPresent(dt -> {
                    Performance p = groupedPerformances.get(key);
                    if (p == null) {
                        p = new Performance();
                        p.setTitle(title);
                        p.setDateTimes(new LinkedHashSet<>());

                        Element durEl = block.selectFirst(schema.durationSelector());
                        if (durEl != null) {
                            String duration = durEl.ownText().trim();
                            if (duration.isEmpty()) duration = durEl.text().trim();
                            p.setDuration(duration);
                        }

                        Element ageEl = block.selectFirst(schema.ageLimitSelector());
                        if (ageEl != null) p.setAgeLimit(ageEl.text().trim());

                        Element img = block.selectFirst(schema.imageSelector());
                        if (img != null) {
                            String src = img.attr("src");
                            if (src.startsWith("/")) src = baseUrl + src;
                            else if (!src.startsWith("http")) src = baseUrl + src;
                            p.setImageUrl(src);
                        }

                        groupedPerformances.put(key, p);
                    }
                    p.getDateTimes().add(dt);
                });
            } catch (Exception e) {
                log.error("Ошибка парсинга блока Драмтеатра", e);
            }
        }
        return new ArrayList<>(groupedPerformances.values());
    }
}
