package io.github.rayoraq.parser;

import org.jsoup.nodes.Document;

public interface Parser<T> {
    T parse(Document document);
}
