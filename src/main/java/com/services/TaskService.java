package com.services;

import com.entities.*;
import com.repository.FileTaskRepository;
import java.time.LocalDateTime;
import java.util.*;

public class TaskService {
    private final FileTaskRepository repo = FileTaskRepository.getInstance();
    private List<Task> tasks = repo.loadAll();

    public void addTask(Task t) throws TaskValidationException {
        validateDeadline(t);
        tasks.add(t);
        repo.saveAll(tasks);
    }

    public void updateTask(Task updatedTask) throws TaskValidationException {
        validateDeadline(updatedTask);
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == updatedTask.getId()) {
                tasks.set(i, updatedTask);
                break;
            }
        }
        repo.saveAll(tasks);
    }

    private void validateDeadline(Task t) throws TaskValidationException {
        if (t instanceof DeadLineTask dt && dt.getPrazo().isBefore(LocalDateTime.now()) && !dt.isCompleted())
            throw new TaskValidationException("O prazo não pode ser no passado!");
    }

    public void deleteTask(int id) {
        tasks.removeIf(t -> t.getId() == id);
        repo.saveAll(tasks);
    }

    public void toggleTask(int id) {
        tasks.stream().filter(t -> t.getId() == id).findFirst().ifPresent(t -> t.setCompleted(!t.isCompleted()));
        repo.saveAll(tasks);
    }

    public List<Task> search(String q) {
        String query = q == null ? "" : q.toLowerCase();
        return tasks.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(query) || t.getDescription().toLowerCase().contains(query))
                .sorted((t1, t2) -> {
                    if (t1 instanceof DeadLineTask d1 && t2 instanceof DeadLineTask d2) return d1.getPrazo().compareTo(d2.getPrazo());
                    return t1 instanceof DeadLineTask ? -1 : 1;
                }).toList();
    }
}