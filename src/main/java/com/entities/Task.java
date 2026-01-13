package com.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public abstract class Task {
    private int id;
    private String title;
    private String description;
    private boolean completed;
    private static int contadorId = 0;

    public Task(int id, String title, String description, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        if (id >= contadorId) contadorId = id + 1;
    }

    public Task(String title, String description) {
        this.id = contadorId++;
        this.title = title;
        this.description = description;
        this.completed = false;
    }

    public abstract String getStatusTempo();
    public abstract String toFileString();
}