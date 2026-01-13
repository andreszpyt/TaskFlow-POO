package com.view;

import com.entities.SimpleTask;
import com.entities.Task;
import com.services.TaskService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private List<Task> currentTasks;
    private TaskService service;
    private DefaultListModel<String> listModel;
    private JList<String> taskListDisplay;

    public MainFrame() {
        this.service = new TaskService();
        this.listModel = new DefaultListModel<>();

        setupLayout();
        refreshList();

        setTitle("TaskFlow Pro 🚀");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        taskListDisplay = new JList<>(listModel);
        add(new JScrollPane(taskListDisplay), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Nova Tarefa");
        JButton btnDone = new JButton("Concluir");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDone);
        add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(this, "Título da Tarefa:");

            if (title != null && !title.trim().isEmpty()) {
                try {
                    service.addTask(new SimpleTask(title));
                    refreshList();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
                }
            }
        });

        btnDone.addActionListener(e -> {
            int index = taskListDisplay.getSelectedIndex();
            if (index != -1) {
                Task selectedTask = currentTasks.get(index);
                service.toggleTaskCompletion(selectedTask.getId());
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma tarefa primeiro!");
            }
        });
    }

    private void refreshList() {
        listModel.clear();
        this.currentTasks = service.getSortedTasks();
        for (Task t : currentTasks) {
            listModel.addElement(t.getStatusTempo() + " | " + t.getTitle());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Erro ao carregar tema.");
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
