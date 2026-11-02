package io.github.rayoraq.parser.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Publication {
    public Publication(){}

    private String id;
    private String title;
    private String userpic;
    private String username;
    private LocalDateTime datetimePublished;
    private String readingTime;
    private Integer viewCount;
    private String images;
    private List<String> hubs;
    private List<String> previewText;
    private String difficulty;
    private List<String> variantCases;


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUserpic() {
        return userpic;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getDatetimePublished() {
        return datetimePublished;
    }

    public String getReadingTime() {
        return readingTime;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public String getImages() {
        return images;
    }

    public List<String> getHubs() {
        return hubs;
    }

    public List<String> getPreviewText() {
        return previewText;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<String> getVariantCases() {
        return variantCases;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserpic(String userpic) {
        this.userpic = userpic;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDatetimePublished(LocalDateTime datetimePublished) {
        this.datetimePublished = datetimePublished;
    }

    public void setReadingTime(String readingTime) {
        this.readingTime = readingTime;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setHubs(List<String> hubs) {
        this.hubs = hubs;
    }

    public void setPreviewText(List<String> previewText) {
        this.previewText = previewText;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setVariantCases(List<String> variantCases) {
        this.variantCases = variantCases;
    }

    @Override
    public String toString() {
        return "Publication{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", username='" + username + '\'' +
                ", datetimePublished=" + datetimePublished +
                ", viewCount=" + viewCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Publication that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}

