package com.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeadLineTask extends Task {
    private LocalDateTime prazo;

    public DeadLineTask(int id, String title, String description, boolean completed, LocalDateTime prazo) {
        super(id, title, description, completed);
        this.prazo = prazo;
    }

    public DeadLineTask(String title, String description, LocalDateTime prazo) {
        super(title, description);
        this.prazo = prazo;
    }

    public LocalDateTime getPrazo() { return prazo; }

    @Override
    public String getStatusTempo() {
        if (isCompleted()) return "Concluída";
        return LocalDateTime.now().isAfter(prazo) ? "Atrasada" : "No prazo";
    }

    @Override
    public String toFileString() {
        return "DEADLINE;" + getId() + ";" + getTitle() + ";" + getDescription() + ";" + isCompleted() + ";" + prazo.toString();
    }
}