package io.github.rayoraq.parser.util;

public final class TheatreMappingSchema{
    private final String titleSelector;
    private final String ageLimitSelector;
    private final String imageSelector;
    private final String dateLinksSelector;
    private final String durationSelector;

    public TheatreMappingSchema(String titleSelector, String ageLimitSelector,
                                String imageSelector, String dateLinksSelector, String durationSelector) {
        this.titleSelector = titleSelector;
        this.ageLimitSelector = ageLimitSelector;
        this.imageSelector = imageSelector;
        this.dateLinksSelector = dateLinksSelector;
        this.durationSelector = durationSelector;
    }

    public String titleSelector() { return titleSelector; }
    public String ageLimitSelector() { return ageLimitSelector; }
    public String imageSelector() { return imageSelector; }
    public String dateLinksSelector() { return dateLinksSelector; }
    public String durationSelector() { return durationSelector; }
}
