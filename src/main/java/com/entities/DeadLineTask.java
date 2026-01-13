package com.entities;

import java.time.LocalDateTime;

public class DeadLineTask extends Task {
    private LocalDateTime prazo;

    public DeadLineTask(int id, String title, String desc, boolean done, LocalDateTime prazo) {
        super(id, title, desc, done);
        this.prazo = prazo;
    }
    public DeadLineTask(String title, String desc, LocalDateTime prazo) {
        super(title, desc);
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