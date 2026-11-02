package io.github.rayoraq.parser.habr;

/**
 * CSS-селекторы для карточек статей в ленте habr.com/ru/all
 * Все селекторы (кроме ARTICLE) применяются к элементу одной статьи, полученному по селектору ARTICLE
 */
public class HabrSelectors {
    private HabrSelectors(){
        throw new UnsupportedOperationException("Это вспомогательный класс, его нельзя инстанцировать");//защита от вызова внутри класса и от reflection api
    }

    public static final String ARTICLE = "article.tm-articles-list__item";
    public static final String TITLE_LINK = "a.tm-title__link";
    public static final String TITLE_TEXT = "a.tm-title__link span";
    public static final String AUTHOR_NAME   = "a.tm-user-info__username";
    public static final String AUTHOR_AVATAR = "img.tm-entity-image__pic";
    public static final String PUBLISHED_TIME = "time[datetime]";
    public static final String READING_TIME = "span.tm-article-reading-time__label";
    public static final String VIEWS_VALUE = "span.tm-icon-counter__value";
    public static final String DIFFICULTY = "span.tm-article-complexity__label";
    public static final String HUB_LINK = "a.tm-publication-hub__link";
    public static final String HUB_NAME = "a.tm-publication-hub__link > span:first-child";
    public static final String LABEL = ".publication-label span";
    public static final String COVER_IMAGE = "img.lead-image";
    public static final String PREVIEW_PARAGRAPH = "div.lead div.article-formatted-body p";
    public static final String ATTR_DATETIME = "datetime";
    public static final String ATTR_TITLE = "title";
    public static final String ATTR_SRC = "src";
    public static final String ATTR_HREF = "href";
    public static final String ATTR_ID = "id";

    public static final String SITE_BASE = "https://habr.com";
}
