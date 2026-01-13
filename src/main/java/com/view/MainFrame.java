package com.view;

import com.entities.*;
import com.services.TaskService;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class MainFrame extends JFrame {
    private final TaskService service = new TaskService();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblT, lblC, lblP;

    public MainFrame() {
        setTitle("TaskFlow Pro");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        refresh("");
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        JLabel logo = new JLabel("TASKFLOW", 0);
        logo.setFont(new Font("Inter", 1, 20));
        logo.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.add(logo, "North");
        add(sidebar, "West");

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Pesquisar tarefas...");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { refresh(txtSearch.getText()); }
        });

        JButton btnAdd = new JButton("+ Nova Tarefa");
        btnAdd.putClientProperty(FlatClientProperties.STYLE, "background:#27ae60;foreground:#fff;arc:10");
        btnAdd.addActionListener(e -> openAddDialog());

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.add(txtSearch, "Center");
        header.add(btnAdd, "East");
        main.add(header, "North");

        JPanel stats = new JPanel(new GridLayout(1, 3, 20, 0));
        lblT = addStat(stats, "TOTAL", Color.CYAN);
        lblC = addStat(stats, "CONCLUÍDAS", Color.GREEN);
        lblP = addStat(stats, "PENDENTES", Color.ORANGE);

        model = new DefaultTableModel(new String[]{"ID", "Título", "Status", "Prazo", "Descrição"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground(v.equals("Concluída") ? Color.GREEN : v.equals("Atrasada") ? Color.RED : Color.CYAN);
                return l;
            }
        });

        JPanel actions = new JPanel(new FlowLayout(2));
        JButton btnT = new JButton("Alternar");
        JButton btnD = new JButton("Remover");
        btnD.setForeground(Color.RED);
        btnT.addActionListener(e -> { if(table.getSelectedRow()!=-1) { service.toggleTask((int)model.getValueAt(table.getSelectedRow(), 0)); refresh(txtSearch.getText()); } });
        btnD.addActionListener(e -> { if(table.getSelectedRow()!=-1 && JOptionPane.showConfirmDialog(null, "Excluir?") == 0) { service.deleteTask((int)model.getValueAt(table.getSelectedRow(), 0)); refresh(txtSearch.getText()); } });
        actions.add(btnT); actions.add(btnD);

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
        v.setFont(new Font("Sans", 1, 25));
        card.add(v, "Center");
        p.add(card);
        return v;
    }

    private void openAddDialog() {
        JTextField tit = new JTextField(), des = new JTextField(), dat = new JTextField(LocalDateTime.now().plusDays(1).toString());
        JComboBox<String> type = new JComboBox<>(new String[]{"Simples", "Prazo"});
        Object[] msg = {"Título:", tit, "Descrição:", des, "Tipo:", type, "Prazo:", dat};
        if (JOptionPane.showConfirmDialog(this, msg, "Novo", 2) == 0) {
            try {
                Task t = type.getSelectedIndex() == 0 ? new SimpleTask(tit.getText(), des.getText()) : new DeadLineTask(tit.getText(), des.getText(), LocalDateTime.parse(dat.getText()));
                service.addTask(t); refresh("");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void refresh(String q) {
        model.setRowCount(0);
        List<Task> list = service.search(q);
        int done = 0;
        for (Task t : list) {
            if (t.isCompleted()) done++;
            model.addRow(new Object[]{t.getId(), t.getTitle(), t.getStatusTempo(), t instanceof DeadLineTask ? ((DeadLineTask)t).getPrazo() : "---", t.getDescription()});
        }
        lblT.setText(String.valueOf(list.size())); lblC.setText(String.valueOf(done)); lblP.setText(String.valueOf(list.size()-done));
    }

    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}