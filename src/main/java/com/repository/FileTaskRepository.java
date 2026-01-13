package com.repository;

import com.entities.Task;
import com.factory.TaskFactory;
import java.io.*;
import java.util.*;

public class FileTaskRepository {
    private final String fileName = "tarefas.txt";
    private static FileTaskRepository instance;
    private FileTaskRepository() {}
    public static FileTaskRepository getInstance() {
        if (instance == null) instance = new FileTaskRepository();
        return instance;
    }
    public void saveAll(List<Task> tasks) {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
            for (Task t : tasks) { w.write(t.toFileString()); w.newLine(); }
        } catch (IOException e) { e.printStackTrace(); }
    }
    public List<Task> loadAll() {
        List<Task> tasks = new ArrayList<>();
        File f = new File(fileName);
        if (!f.exists()) return tasks;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) tasks.add(TaskFactory.createFromLine(line));
        } catch (IOException e) { e.printStackTrace(); }
        return tasks;
    }
}