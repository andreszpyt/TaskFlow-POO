package com.view;

import com.entities.*;
import com.services.TaskService;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class MainFrame extends JFrame {
    private TaskService service = new TaskService();
    private DefaultTableModel tableModel;
    private JTable taskTable;
    private List<Task> currentTasks;

    public MainFrame() {
        setTitle("TaskFlow Pro 🚀");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupUI();
        refreshTable();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        String[] cols = {"ID", "Título", "Descrição", "Status", "Prazo"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        taskTable = new JTable(tableModel);
        add(new JScrollPane(taskTable), BorderLayout.CENTER);

        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Nova Tarefa");
        JButton btnDone = new JButton("Concluir/Desfazer");
        JButton btnDel = new JButton("Remover");

        btnAdd.addActionListener(e -> showAddDialog());
        btnDone.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1) { service.toggleTask(currentTasks.get(row).getId()); refreshTable(); }
        });
        btnDel.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1) { service.deleteTask(currentTasks.get(row).getId()); refreshTable(); }
        });

        pnlBot.add(btnAdd); pnlBot.add(btnDone); pnlBot.add(btnDel);
        add(pnlBot, BorderLayout.SOUTH);
    }

    private void showAddDialog() {
        JTextField txtTitle = new JTextField();
        JTextField txtDesc = new JTextField();
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Simples", "Com Prazo"});
        JTextField txtDate = new JTextField(LocalDateTime.now().plusDays(1).toString());

        Object[] message = { "Título:", txtTitle, "Descrição:", txtDesc, "Tipo:", cbType, "Prazo (ISO):", txtDate };

        int option = JOptionPane.showConfirmDialog(this, message, "Nova Tarefa", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Task newTask;
                if (cbType.getSelectedIndex() == 0) {
                    newTask = new SimpleTask(txtTitle.getText(), txtDesc.getText());
                } else {
                    newTask = new DeadLineTask(txtTitle.getText(), txtDesc.getText(), LocalDateTime.parse(txtDate.getText()));
                }
                service.addTask(newTask);
                refreshTable();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        currentTasks = service.getSortedTasks();
        for (Task t : currentTasks) {
            String prazo = (t instanceof DeadLineTask dt) ? dt.getPrazo().toString() : "N/A";
            tableModel.addRow(new Object[]{t.getId(), t.getTitle(), t.getDescription(), t.getStatusTempo(), prazo});
        }
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}