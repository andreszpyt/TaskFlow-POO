package com.repository;

import com.entities.Task;
import com.factory.TaskFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileTaskRepository {
    private final String fileName = "tarefas.txt";
    private static FileTaskRepository instance;

    private FileTaskRepository() {}

    public static FileTaskRepository getInstance() {
        if (instance == null) instance = new FileTaskRepository();
        return instance;
    }

    public void saveAll(List<Task> tasks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Task t : tasks) {
                writer.write(t.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar: " + e.getMessage());
        }
    }

    public List<Task> loadAll() {
        List<Task> tasks = new ArrayList<>();
        File file = new File(fileName);

        if (!file.exists()) return tasks;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tasks.add(TaskFactory.createFromLine(line));
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar: " + e.getMessage());
        }
        return tasks;
    }
}
