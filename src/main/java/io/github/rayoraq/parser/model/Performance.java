package io.github.rayoraq.parser.model;

import java.time.LocalDateTime;
import java.util.*;

public class Performance {
    private String title;
    private Set<LocalDateTime> dateTimes = new LinkedHashSet<>(); // Список дат проведения
    private String duration;
    private String ageLimit;
    private String imageUrl;

    public Performance() {}

    public String getTitle() { return title; }
    public Set<LocalDateTime> getDateTimes() { return dateTimes; }
    public String getDuration() { return duration; }
    public String getAgeLimit() { return ageLimit; }
    public String getImageUrl() { return imageUrl; }

    public void setTitle(String title) { this.title = title; }
    public void setDateTimes(Set<LocalDateTime> dateTimes) { this.dateTimes = dateTimes; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setAgeLimit(String ageLimit) { this.ageLimit = ageLimit; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Performance{" +
                "title='" + title + '\'' +
                ", dateTimes=" + dateTimes +
                ", duration='" + duration + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Performance that)) return false;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title);
    }
}
