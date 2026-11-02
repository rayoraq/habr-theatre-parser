package io.github.rayoraq.parser.habr;

import io.github.rayoraq.parser.Parser;
import io.github.rayoraq.parser.model.Publication;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrParser implements Parser<ArrayList<Publication>> {

    private static final Logger log = LoggerFactory.getLogger(HabrParser.class);

    @Override
    public ArrayList<Publication> parse(Document document) {
        ArrayList<Publication> result = new ArrayList<>();
        Elements articles = document.select(HabrSelectors.ARTICLE);
        log.info("Найдено карточек статей: {}", articles.size());

        for (Element article : articles) {
            try {
                Publication p = new Publication();

                String id = article.id();
                p.setId((id == null || id.isEmpty()) ? "unknown_" + result.size() : id);

                Element titleEl = article.selectFirst(HabrSelectors.TITLE_TEXT);
                p.setTitle(titleEl != null ? titleEl.text() : null);
                if (titleEl == null) log.warn("Нет заголовка у article id={}", article.id());

                Element authorEl = article.selectFirst(HabrSelectors.AUTHOR_NAME);
                p.setUsername(authorEl != null ? authorEl.text() : null);

                Element avatarEl = article.selectFirst(HabrSelectors.AUTHOR_AVATAR);
                p.setUserpic(avatarEl != null ? normalizeUrl(avatarEl.attr(HabrSelectors.ATTR_SRC)) : null);

                Element timeEl = article.selectFirst(HabrSelectors.PUBLISHED_TIME);
                if (timeEl != null) {
                    String dt = timeEl.attr(HabrSelectors.ATTR_DATETIME);
                    try {
                        p.setDatetimePublished(OffsetDateTime.parse(dt).toLocalDateTime());
                    } catch (Exception e) {
                        log.warn("Не удалось распарсить дату '{}': {}", dt, e.getMessage());
                    }
                }

                Element readEl = article.selectFirst(HabrSelectors.READING_TIME);
                p.setReadingTime(readEl != null ? readEl.text() : null);

                Element viewsEl = article.selectFirst(HabrSelectors.VIEWS_VALUE);
                if (viewsEl != null) {
                    String raw = viewsEl.attr(HabrSelectors.ATTR_TITLE).replaceAll("\\D", "");
                    if (!raw.isEmpty()) {
                        try {
                            p.setViewCount(Integer.parseInt(raw));
                        } catch (NumberFormatException e) {
                            log.warn("Просмотры не распарсились: '{}'", raw);
                        }
                    }
                }

                Element diffEl = article.selectFirst(HabrSelectors.DIFFICULTY);
                p.setDifficulty(diffEl != null ? diffEl.text() : null);

                List<String> hubs = new ArrayList<>();
                for (Element hubEl : article.select(HabrSelectors.HUB_NAME)) {
                    String hub = hubEl.text().trim();
                    if (!hub.isEmpty()) hubs.add(hub);
                }
                p.setHubs(hubs);

                List<String> labels = new ArrayList<>();
                for (Element labelEl : article.select(HabrSelectors.LABEL)) {
                    String label = labelEl.text().trim();
                    if (!label.isEmpty()) labels.add(label);
                }
                p.setVariantCases(labels);

                List<String> preview = new ArrayList<>();
                for (Element pEl : article.select(HabrSelectors.PREVIEW_PARAGRAPH)) {
                    String txt = pEl.text().trim();
                    if (!txt.isEmpty()) preview.add(txt);
                }
                p.setPreviewText(preview);

                Element coverEl = article.selectFirst(HabrSelectors.COVER_IMAGE);
                p.setImages(coverEl != null
                        ? normalizeUrl(coverEl.attr(HabrSelectors.ATTR_SRC))
                        : null);

                result.add(p);
                log.debug("Обработана статья id={} - '{}'", p.getId(), p.getTitle());
            } catch (Exception e) {
                log.error("Ошибка парсинга article id={}: {}", article.id(), e.getMessage(), e);
            }
        }
        return result;
    }

    private static String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("//")) return "https:" + url;
        if (url.startsWith("/")) return HabrSelectors.SITE_BASE + url;
        if (!url.startsWith("http")) return HabrSelectors.SITE_BASE + "/" + url;
        return url;
    }
}