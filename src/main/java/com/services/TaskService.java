package com.services;

import com.entities.DeadLineTask;
import com.entities.Task;
import com.repository.FileTaskRepository;
import java.time.LocalDateTime;
import java.util.List;

public class TaskService {
    private final FileTaskRepository repository;
    private List<Task> taskList;

    public TaskService() {
        this.repository = FileTaskRepository.getInstance();
        this.taskList = repository.loadAll();
    }

    public void addTask(Task task) throws Exception {
        if (task instanceof DeadLineTask) {
            DeadLineTask dt = (DeadLineTask) task;
            if (dt.getPrazo().isBefore(LocalDateTime.now())) {
                throw new Exception("O prazo não pode ser uma data no passado!");
            }
        }
        taskList.add(task);
        repository.saveAll(taskList);
    }

    public List<Task> getSortedTasks() {
        taskList.sort((t1, t2) -> {
            if (t1 instanceof DeadLineTask && t2 instanceof DeadLineTask) {
                return ((DeadLineTask) t1).getPrazo().compareTo(((DeadLineTask) t2).getPrazo());
            }
            return 0;
        });
        return taskList;
    }

    public void toggleTaskCompletion(int id) {
        for (Task t : taskList) {
            if (t.getId() == id) {
                t.setCompleted(!t.isCompleted());
                break;
            }
        }
        repository.saveAll(taskList);
    }
}