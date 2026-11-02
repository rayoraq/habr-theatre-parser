package io.github.rayoraq.parser;

public abstract class ParserSettings {
    protected String baseUrl;
    protected String prefix;
    protected int startPoint;
    protected int endPoint;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public int getEndPoint() {
        return endPoint;
    }
}
