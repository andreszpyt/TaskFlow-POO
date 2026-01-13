package com.factory;

import com.entities.*;
import java.time.LocalDateTime;

public class TaskFactory {
    public static Task createFromLine(String line) {
        String[] p = line.split(";");
        int id = Integer.parseInt(p[1]);
        boolean done = Boolean.parseBoolean(p[4]);
        if (p[0].equalsIgnoreCase("DEADLINE")) {
            return new DeadLineTask(id, p[2], p[3], done, LocalDateTime.parse(p[5]));
        }
        return new SimpleTask(id, p[2], p[3], done);
    }
}