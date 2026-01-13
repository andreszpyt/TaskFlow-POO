package com.services;

import com.entities.*;
import com.repository.FileTaskRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {
    private final FileTaskRepository repo = FileTaskRepository.getInstance();
    private List<Task> tasks;

    public TaskService() {
        this.tasks = repo.loadAll();
    }

    public void addTask(Task t) throws TaskValidationException {
        // Validação leve: Só impede datas passadas na CRIAÇÃO de novas tarefas
        // (Se quiser permitir criar tarefas retroativas, remova essa linha)
        validateCreation(t);

        // Gera um ID novo se for 0 (caso seu repo não faça isso)
        if (t.getId() == 0) {
            int maxId = tasks.stream().mapToInt(Task::getId).max().orElse(0);
            t.setId(maxId + 1);
        }

        tasks.add(t);
        repo.saveAll(tasks);
    }

    public void updateTask(Task updatedTask) {
        // Na edição, não validamos data passada, pois o usuário pode estar
        // apenas corrigindo o texto de uma tarefa que já venceu.

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == updatedTask.getId()) {
                tasks.set(i, updatedTask);
                break;
            }
        }
        repo.saveAll(tasks);
    }

    public void deleteTask(int id) {
        tasks.removeIf(t -> t.getId() == id);
        repo.saveAll(tasks);
    }

    public void toggleTask(int id) {
        tasks.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .ifPresent(t -> t.setCompleted(!t.isCompleted()));
        repo.saveAll(tasks);
    }

    // Validação específica apenas para NOVAS tarefas
    private void validateCreation(Task t) throws TaskValidationException {
        if (t instanceof DeadLineTask dt && dt.getPrazo().isBefore(LocalDateTime.now())) {
            throw new TaskValidationException("Para novas tarefas, o prazo deve ser futuro!");
        }
    }

    public List<Task> search(String query) {
        String q = query == null ? "" : query.toLowerCase();

        return tasks.stream()
                .filter(t -> matches(t, q))
                .sorted(this::compareTasks) // Usa lógica de ordenação inteligente
                .collect(Collectors.toList());
    }

    private boolean matches(Task t, String q) {
        if (q.isEmpty()) return true;
        return t.getTitle().toLowerCase().contains(q) ||
                t.getDescription().toLowerCase().contains(q);
    }

    // Lógica de Ordenação Inteligente ("Smart Sort")
    private int compareTasks(Task t1, Task t2) {
        // 1. Concluídas vão para o final da lista
        if (t1.isCompleted() != t2.isCompleted()) {
            return t1.isCompleted() ? 1 : -1;
        }

        // 2. Se ambas pendentes, quem tem prazo vem primeiro
        if (t1 instanceof DeadLineTask d1 && t2 instanceof DeadLineTask d2) {
            return d1.getPrazo().compareTo(d2.getPrazo());
        }

        // 3. Tarefas com prazo vêm antes de tarefas simples
        if (t1 instanceof DeadLineTask && t2 instanceof SimpleTask) return -1;
        if (t1 instanceof SimpleTask && t2 instanceof DeadLineTask) return 1;

        // 4. Desempate por ID (ordem de criação)
        return Integer.compare(t1.getId(), t2.getId());
    }
}