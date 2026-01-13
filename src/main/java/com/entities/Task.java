package com.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class Task {
    private int id;
    private String title;
    private String description;
    private boolean completed;

    private static int contadorId = 0;

    public abstract String getStatusTempo();

    public Task(String title, String description, boolean completed) {
        this.id = contadorId++;
        this.title = title;
        this.description = description;
        this.completed = completed;

        if (id >= contadorId) {
            contadorId = id + 1;
        }
    }

    public Task(String title) {
        this.id = contadorId++;
        this.title = title;
        this.completed = false;

        if (id >= contadorId) {
            contadorId = id + 1;
        }
    }

    public Task(String title, boolean completed) {
        this.id = contadorId++;
        this.title = title;
        this.completed = completed;

        if (id >= contadorId) {
            contadorId = id + 1;
        }
    }

    public abstract String toFileString();
}
