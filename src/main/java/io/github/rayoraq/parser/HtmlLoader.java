package io.github.rayoraq.parser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class HtmlLoader {

    private static final Logger log = LoggerFactory.getLogger(HtmlLoader.class);

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    private static final Properties LOCAL_CONFIG = loadLocalConfig();

    private final String baseUrl;
    private final String url;
    private final String cookieHeader;

    public HtmlLoader(ParserSettings settings) {
        this.baseUrl = settings.getBaseUrl();
        this.url = settings.getBaseUrl() + "/" + settings.getPrefix();
        this.cookieHeader = resolveCookies(baseUrl);
    }

    public Document getSourceByPageId(int id) throws IOException {
        String currentUrl = url;
        if (currentUrl.contains("{CurrentId}")) {
            currentUrl = currentUrl.replace("{CurrentId}", Integer.toString(id));
        } else if (!currentUrl.endsWith("/")) {
            currentUrl += "/";
        }
        if (currentUrl.startsWith("http://")) {
            currentUrl = currentUrl.replace("http://", "https://");
        }

        log.info("GET {}", currentUrl);

        try {
            Connection connection = Jsoup.connect(currentUrl)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8")
                    .userAgent(USER_AGENT)
                    .timeout(30_000);

            if (cookieHeader != null && !cookieHeader.isBlank()) {
                connection = connection.header("Cookie", cookieHeader);
                log.debug("Использую куки для {}", baseUrl);
            } else {
                log.warn("Куки для {} не заданы (application-local.properties)", baseUrl);
            }

            Document doc = connection.get();
            log.info("Успешно загружено {} символов с {}", doc.outerHtml().length(), currentUrl);
            return doc;
        } catch (IOException e) {
            log.error("Ошибка загрузки {}: {}", currentUrl, e.getMessage(), e);
            throw e;
        }
    }

    /** Ищем cookies.<host> в application-local.properties */
    private static String resolveCookies(String baseUrl) {
        try {
            String host = URI.create(baseUrl).getHost();
            return LOCAL_CONFIG.getProperty("cookies." + host);
        } catch (Exception e) {
            return null;
        }
    }

    private static Properties loadLocalConfig() {
        Properties props = new Properties();
        Path path = Paths.get("application-local.properties");
        if (!Files.exists(path)) {
            log.info("application-local.properties не найден — работаем без куков");
            return props;
        }
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
            log.info("Загружено {} ключей из application-local.properties", props.size());
        } catch (IOException e) {
            log.error("Не удалось прочитать application-local.properties: {}", e.getMessage(), e);
        }
        return props;
    }
}