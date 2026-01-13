package com.view;

import com.entities.*;
import com.services.TaskService;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class MainFrame extends JFrame {
    private final TaskService service = new TaskService();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblT, lblC, lblP;
    private List<Task> currentTasks;

    public MainFrame() {
        setTitle("TaskFlow Ultimate Pro");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        refresh("");
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));

        // Sidebar Minimalista
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(UIManager.getColor("Panel.background"));
        JLabel logo = new JLabel("TASKFLOW PRO", 0);
        logo.setFont(new Font("SansSerif", 1, 20));
        logo.setForeground(new Color(46, 204, 113));
        logo.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        sidebar.add(logo, "North");
        add(sidebar, "West");

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header com Busca e Botão Novo
        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Pesquisar tarefas...");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { refresh(txtSearch.getText()); }
        });

        JButton btnAdd = new JButton("+ Adicionar");
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "background:#27ae60;foreground:#fff;arc:10");
        btnAdd.addActionListener(e -> handleAddOrEdit(null));

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.add(txtSearch, "Center");
        header.add(btnAdd, "East");
        main.add(header, "North");

        // Dashboard Stats
        JPanel stats = new JPanel(new GridLayout(1, 3, 20, 0));
        lblT = addStat(stats, "TOTAL", Color.CYAN);
        lblC = addStat(stats, "CONCLUÍDAS", Color.GREEN);
        lblP = addStat(stats, "PENDENTES", Color.ORANGE);

        // Tabela Estilizada
        model = new DefaultTableModel(new String[]{"ID", "Título", "Status", "Prazo", "Descrição"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(45);
        table.setShowHorizontalLines(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderizador de cores
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(0);
                l.setForeground(v.equals("Concluída") ? Color.GREEN : v.equals("Atrasada") ? Color.RED : Color.CYAN);
                return l;
            }
        });

        // Clique Duplo para Editar
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });

        // Botões de Ação
        JPanel actions = new JPanel(new FlowLayout(2, 10, 0));
        JButton btnToggle = new JButton("Alternar Status");
        JButton btnEdit = new JButton("Editar");
        JButton btnDel = new JButton("Remover");
        btnDel.setForeground(Color.RED);

        btnToggle.addActionListener(e -> { if(getSelectedId() != -1) { service.toggleTask(getSelectedId()); refresh(txtSearch.getText()); } });
        btnEdit.addActionListener(e -> handleEdit());
        btnDel.addActionListener(e -> { if(getSelectedId() != -1 && JOptionPane.showConfirmDialog(null, "Excluir?") == 0) { service.deleteTask(getSelectedId()); refresh(txtSearch.getText()); } });

        actions.add(btnToggle); actions.add(btnEdit); actions.add(btnDel);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.add(stats, "North");
        center.add(new JScrollPane(table), "Center");
        center.add(actions, "South");
        main.add(center, "Center");
        add(main, "Center");
    }

    private JLabel addStat(JPanel p, String t, Color c) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, c));
        card.add(new JLabel(t), "North");
        JLabel v = new JLabel("0", 0);
        v.setFont(new Font("Sans", 1, 28));
        card.add(v, "Center");
        p.add(card);
        return v;
    }

    private int getSelectedId() {
        int row = table.getSelectedRow();
        return row != -1 ? (int) model.getValueAt(row, 0) : -1;
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row != -1) handleAddOrEdit(currentTasks.get(row));
    }

    private void handleAddOrEdit(Task tToEdit) {
        JTextField titF = new JTextField(tToEdit != null ? tToEdit.getTitle() : "");
        JTextField desF = new JTextField(tToEdit != null ? tToEdit.getDescription() : "");
        JComboBox<String> typeB = new JComboBox<>(new String[]{"Simples", "Prazo"});

        // Melhoria: Seletor de Data (Spinner)
        JSpinner dateS = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor de = new JSpinner.DateEditor(dateS, "yyyy-MM-dd HH:mm");
        dateS.setEditor(de);

        if (tToEdit instanceof DeadLineTask dt) {
            typeB.setSelectedIndex(1);
            dateS.setValue(Date.from(dt.getPrazo().atZone(ZoneId.systemDefault()).toInstant()));
        }

        Object[] msg = {"Título:", titF, "Descrição:", desF, "Tipo:", typeB, "Prazo:", dateS};

        if (JOptionPane.showConfirmDialog(this, msg, tToEdit == null ? "Nova Tarefa" : "Editar Tarefa", 2) == 0) {
            try {
                Task res;
                if (typeB.getSelectedIndex() == 0) {
                    res = new SimpleTask(titF.getText(), desF.getText());
                } else {
                    LocalDateTime ldt = ((Date) dateS.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    res = new DeadLineTask(titF.getText(), desF.getText(), ldt);
                }

                if (tToEdit != null) {
                    res.setId(tToEdit.getId());
                    res.setCompleted(tToEdit.isCompleted());
                    service.updateTask(res);
                } else {
                    service.addTask(res);
                }
                refresh("");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void refresh(String q) {
        model.setRowCount(0);
        currentTasks = service.search(q);
        int done = 0;
        for (Task t : currentTasks) {
            if (t.isCompleted()) done++;
            model.addRow(new Object[]{t.getId(), t.getTitle(), t.getStatusTempo(),
                    t instanceof DeadLineTask ? ((DeadLineTask)t).getPrazo().toString().replace("T", " ") : "---",
                    t.getDescription()});
        }
        lblT.setText(String.valueOf(currentTasks.size()));
        lblC.setText(String.valueOf(done));
        lblP.setText(String.valueOf(currentTasks.size() - done));
    }

    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}