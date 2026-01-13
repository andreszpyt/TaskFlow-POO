package com.services;

import com.entities.*;
import com.repository.FileTaskRepository;
import java.time.LocalDateTime;
import java.util.*;

public class TaskService {
    private final FileTaskRepository repository = FileTaskRepository.getInstance();
    private List<Task> taskList = repository.loadAll();

    public void addTask(Task task) throws TaskValidationException {
        if (task instanceof DeadLineTask dt && dt.getPrazo().isBefore(LocalDateTime.now())) {
            throw new TaskValidationException("Erro: O prazo não pode ser no passado!");
        }
        taskList.add(task);
        repository.saveAll(taskList);
    }

    public void deleteTask(int id) {
        taskList.removeIf(t -> t.getId() == id);
        repository.saveAll(taskList);
    }

    public void toggleTask(int id) {
        taskList.stream().filter(t -> t.getId() == id).findFirst().ifPresent(t -> t.setCompleted(!t.isCompleted()));
        repository.saveAll(taskList);
    }

    public List<Task> getSortedTasks() {
        taskList.sort((t1, t2) -> {
            if (t1 instanceof DeadLineTask d1 && t2 instanceof DeadLineTask d2) return d1.getPrazo().compareTo(d2.getPrazo());
            if (t1 instanceof DeadLineTask) return -1;
            if (t2 instanceof DeadLineTask) return 1;
            return Integer.compare(t1.getId(), t2.getId());
        });
        return taskList;
    }
}