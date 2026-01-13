package com.entities;

public class SimpleTask extends Task {
    public SimpleTask(int id, String title, String desc, boolean done) {
        super(id, title, desc, done);
    }
    public SimpleTask(String title, String desc) {
        super(title, desc);
    }
    @Override
    public String getStatusTempo() {
        return isCompleted() ? "Concluída" : "Sem prazo";
    }
    @Override
    public String toFileString() {
        return "SIMPLE;" + getId() + ";" + getTitle() + ";" + getDescription() + ";" + isCompleted();
    }
}