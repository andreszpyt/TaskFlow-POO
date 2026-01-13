package com.factory;

import com.entities.*;
import java.time.LocalDateTime;

public class TaskFactory {
    public static Task createFromLine(String line) {
        String[] parts = line.split(";");
        String type = parts[0];
        int id = Integer.parseInt(parts[1]);
        String title = parts[2];
        String desc = parts[3];
        boolean done = Boolean.parseBoolean(parts[4]);

        if (type.equalsIgnoreCase("DEADLINE")) {
            return new DeadLineTask(id, title, desc, done, LocalDateTime.parse(parts[5]));
        }
        return new SimpleTask(id, title, desc, done);
    }
}