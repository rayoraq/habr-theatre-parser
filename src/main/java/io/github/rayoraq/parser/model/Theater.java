package io.github.rayoraq.parser.model;

import java.util.List;

public class Theater {
    private String name;
    private List<Performance> performances;

    public Theater() {}

    public Theater(String name, List<Performance> performances) {
        this.name = name;
        this.performances = performances;
    }

    public String getName() { return name; }
    public List<Performance> getPerformances() { return performances; }

    public void setName(String name) { this.name = name; }
    public void setPerformances(List<Performance> performances) { this.performances = performances; }
}
