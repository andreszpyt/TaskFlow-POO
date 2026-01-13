package com.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeadLineTask extends Task {
    private LocalDateTime prazo;

    public DeadLineTask(int id, String title, String description, boolean completed, LocalDateTime prazo) {
        super(id, title, description, completed);
        this.prazo = prazo;
    }

    @Override
    public String getStatusTempo() {
        if(isCompleted()){
            return "Concluida";
        }
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(prazo)){
            return "No prazo";
        } return "Atrasada";
    }

    @Override
    public String toFileString() {
        return "DeadLine;" + getId() + ";" + getTitle() + ";" + getDescription()  + ";" + isCompleted() + ";" + this.prazo.toString() + ";";
    }
}
