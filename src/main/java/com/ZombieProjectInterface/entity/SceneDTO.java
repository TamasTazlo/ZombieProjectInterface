package com.ZombieProjectInterface.entity;

import java.util.HashMap;
import java.util.Map;

public class SceneDTO {
    private String description;
    private String name;
    private Map<String, String> options = new HashMap<>();

    public SceneDTO() {

    }

    public SceneDTO(String description, String name, Map options) {
        this.description = description;
        this.name = name;
        this.options = options;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "SceneDTO{" +
                "description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", options=" + options+
                '}';
    }
}
