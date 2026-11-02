package io.github.rayoraq.parser;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

//координирует работу всего парсера
public class ParserWorker<T> {
    private Parser<T> parser;
    private ParserSettings parserSettings;
    private HtmlLoader loader;
    private boolean isActive;
    //списки интерфейсов
    public final ArrayList<OnNewDataHandler> onNewDataList = new ArrayList<>();
    public final ArrayList<OnCompleted> onCompletedList = new ArrayList<>();

    public ParserWorker(Parser<T> parser) {
        this.parser = parser;
    }

    public Parser<T> getParser(){
        return  parser;
    }
    public ParserSettings getParserSettings(){
        return parserSettings;
    }

    public void setParser(Parser<T> parser) {
        this.parser = parser;
    }
    public void setParserSettings(ParserSettings parserSettings) {
        this.parserSettings = parserSettings;
        this.loader = new HtmlLoader(parserSettings);
    }

    //начало и конец парсинга
    public void start() throws IOException {
        isActive = true;
        worker();
    }
    public void abort() {
        isActive = false;
    }

    //метод выполнения работы парсинга
    private void worker() throws IOException {
        for (int i = parserSettings.getStartPoint(); i <= parserSettings.getEndPoint(); i++) {
            if (!isActive) {
                notifyCompleted();
                return;
            }
            Document document = loader.getSourceByPageId(i);
            T result = parser.parse(document);
            for (OnNewDataHandler<T> handler : onNewDataList) {
                handler.onNewData(this, result);
            }
        }
        notifyCompleted();
        isActive = false;
    }

    private void notifyCompleted() {
        for (OnCompleted handler : onCompletedList) {
            handler.onCompleted(this);
        }
    }

    public interface OnNewDataHandler<T> {
        void onNewData(Object sender, T e);
    }
    public interface OnCompleted {
        void onCompleted(Object sender);
    }
}
