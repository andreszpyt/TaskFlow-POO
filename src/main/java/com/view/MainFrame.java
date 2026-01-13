package com.view;

import com.entities.*;
import com.services.TaskService;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

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
    private JTextField txtSearch;

    // Labels para o Dashboard de Estatísticas
    private JLabel lblTotal, lblCompleted, lblPending;

    public MainFrame() {
        setTitle("TaskFlow Ultimate 🚀");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setupUI();
        refreshUI("");
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(UIManager.getColor("Panel.background"));

        JPanel pnlMenu = new JPanel(new GridLayout(10, 1, 5, 5));
        pnlMenu.setOpaque(false);
        pnlMenu.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JLabel logo = new JLabel("TASKFLOW PRO", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton btnAll = new JButton("📂 Todas as Tarefas");
        JButton btnSettings = new JButton("⚙️ Configurações");

        pnlMenu.add(logo);
        pnlMenu.add(btnAll);
        pnlMenu.add(btnSettings);

        sidebar.add(pnlMenu, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Pesquisar tarefas por título ou descrição...");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) { refreshUI(txtSearch.getText()); }
        });

        JButton btnNew = new JButton("+ Nova Tarefa");
        btnNew.putClientProperty(FlatClientProperties.STYLE, "background: #27ae60; foreground: #ffffff; font: bold");
        btnNew.addActionListener(e -> showAddDialog());

        header.add(txtSearch, BorderLayout.CENTER);
        header.add(btnNew, BorderLayout.EAST);
        mainContent.add(header, BorderLayout.NORTH);

        JPanel pnlStats = new JPanel(new GridLayout(1, 3, 20, 0));
        lblTotal = createStatCard(pnlStats, "Total", new Color(52, 152, 219));
        lblCompleted = createStatCard(pnlStats, "Concluídas", new Color(46, 204, 113));
        lblPending = createStatCard(pnlStats, "Pendentes", new Color(230, 126, 34));
        mainContent.add(pnlStats, BorderLayout.CENTER);

        JPanel pnlTable = new JPanel(new BorderLayout(0, 10));
        String[] cols = {"ID", "Título", "Status", "Prazo", "Descrição"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(40);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getTableHeader().setReorderingAllowed(false);

        pnlTable.add(new JScrollPane(taskTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnToggle = new JButton("Alternar Status");
        JButton btnDelete = new JButton("Remover");
        btnDelete.putClientProperty(FlatClientProperties.STYLE, "foreground: #e74c3c");

        btnToggle.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1) { service.toggleTask(currentTasks.get(row).getId()); refreshUI(txtSearch.getText()); }
        });

        btnDelete.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1 && JOptionPane.showConfirmDialog(this, "Remover esta tarefa?") == 0) {
                service.deleteTask(currentTasks.get(row).getId());
                refreshUI(txtSearch.getText());
            }
        });

        footer.add(btnToggle);
        footer.add(btnDelete);
        pnlTable.add(footer, BorderLayout.SOUTH);

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 20));
        pnlCenter.add(pnlStats, BorderLayout.NORTH);
        pnlCenter.add(pnlTable, BorderLayout.CENTER);

        mainContent.add(pnlCenter, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private JLabel createStatCard(JPanel parent, String title, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIManager.getColor("EditorPane.background"));
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, color));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 10));

        JLabel lblVal = new JLabel("0");
        lblVal.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblVal.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 10));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        parent.add(card);
        return lblVal;
    }

    private void showAddDialog() {
        JTextField txtTitle = new JTextField();
        JTextField txtDesc = new JTextField();
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Tarefa Simples", "Tarefa com Prazo"});
        JTextField txtDate = new JTextField(LocalDateTime.now().plusDays(1).toString());

        Object[] message = {
                "Título da Tarefa:", txtTitle,
                "Descrição:", txtDesc,
                "Tipo:", cbType,
                "Prazo (Formato: AAAA-MM-DDTHH:MM):", txtDate
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Criar Nova Tarefa", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Task t = cbType.getSelectedIndex() == 0
                        ? new SimpleTask(txtTitle.getText(), txtDesc.getText())
                        : new DeadLineTask(txtTitle.getText(), txtDesc.getText(), LocalDateTime.parse(txtDate.getText()));

                service.addTask(t);
                refreshUI(txtSearch.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro de Validação: " + ex.getMessage());
            }
        }
    }

    private void refreshUI(String query) {
        tableModel.setRowCount(0);
        currentTasks = service.searchTasks(query);

        int completed = 0;
        for (Task t : currentTasks) {
            if (t.isCompleted()) completed++;
            String extra = (t instanceof DeadLineTask dt) ? dt.getPrazo().toString() : "---";
            tableModel.addRow(new Object[]{t.getId(), t.getTitle(), t.getStatusTempo(), extra, t.getDescription()});
        }

        lblTotal.setText(String.valueOf(currentTasks.size()));
        lblCompleted.setText(String.valueOf(completed));
        lblPending.setText(String.valueOf(currentTasks.size() - completed));
    }

    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}