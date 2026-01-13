package com.entities;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SimpleTask extends Task {

    public SimpleTask(int id, String title, String desc, boolean done) {
        super(id, title, desc, done);
    }

    public SimpleTask(String title) {
        super(title);
    }

    @Override
    public String getStatusTempo() {
        if (isCompleted()) {
            return "Concluída";
        }
        return "Sem prazo";
    }

    @Override
    public String toFileString() {
        return "SIMPLE;" + getId() + ";" + getTitle() + ";" +
                getDescription() + ";" + isCompleted();
    }
}