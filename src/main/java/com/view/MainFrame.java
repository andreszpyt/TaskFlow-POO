package com.view;

import com.entities.*;
import com.services.TaskService;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    // --- PALETA DE CORES ---
    private final Color COL_BG_DARK    = new Color(22, 22, 24);
    private final Color COL_SIDEBAR    = new Color(30, 30, 33);
    private final Color COL_ACCENT     = new Color(59, 130, 246); // Azul Primário

    // Cores de Status Solicitadas
    private final Color COL_ALL        = new Color(220, 220, 220); // Branco/Cinza
    private final Color COL_PROGRESS   = new Color(250, 204, 21);  // Amarelo
    private final Color COL_LATE       = new Color(239, 68, 68);   // Vermelho
    private final Color COL_DONE       = new Color(34, 197, 94);   // Verde

    private final TaskService service = new TaskService();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblStatusFilter;

    // Botões da Sidebar (Globais para podermos atualizar os contadores)
    private JButton btnFilterAll, btnFilterProgress, btnFilterLate, btnFilterDone;

    private String currentFilter = "ALL";
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MainFrame() {
        setTitle("TaskFlow Ultimate Pro");
        setSize(1300, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        refresh(); // Carrega dados iniciais
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. SIDEBAR
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. PAINEL PRINCIPAL
        JPanel main = new JPanel(new BorderLayout(30, 20));
        main.setBackground(COL_BG_DARK);
        main.setBorder(new EmptyBorder(30, 40, 30, 40));

        // 2.1 Header
        main.add(createHeader(), BorderLayout.NORTH);

        // 2.2 Tabela
        setupTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(COL_BG_DARK);
        main.add(scroll, BorderLayout.CENTER);

        // Clique no fundo limpa seleção
        main.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { table.clearSelection(); }
        });

        add(main, BorderLayout.CENTER);
    }

    // ============================================================================================
    // --- SIDEBAR & HEADER ---
    // ============================================================================================

    private JPanel createSidebar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COL_SIDEBAR);
        p.setPreferredSize(new Dimension(270, 0));

        // Logo
        JLabel logo = new JLabel("TASKFLOW", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logo.setBorder(new EmptyBorder(40, 0, 40, 0));
        p.add(logo, BorderLayout.NORTH);

        // Menu Itens
        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 10));
        menu.setBackground(COL_SIDEBAR);
        menu.setBorder(new EmptyBorder(0, 15, 0, 15));

        // Criação dos Botões com Cores Específicas
        btnFilterAll      = createFilterBtn("Todas", "ALL", COL_ALL);
        btnFilterProgress = createFilterBtn("Em Andamento", "PROGRESS", COL_PROGRESS);
        btnFilterLate     = createFilterBtn("Atrasadas", "LATE", COL_LATE);
        btnFilterDone     = createFilterBtn("Concluídas", "DONE", COL_DONE);

        menu.add(btnFilterAll);
        menu.add(btnFilterProgress);
        menu.add(btnFilterLate);
        menu.add(btnFilterDone);

        p.add(menu, BorderLayout.CENTER);

        JLabel ver = new JLabel("v3.0 Ultimate", SwingConstants.CENTER);
        ver.setForeground(Color.GRAY);
        ver.setBorder(new EmptyBorder(20,0,20,0));
        p.add(ver, BorderLayout.SOUTH);

        return p;
    }

    private JButton createFilterBtn(String text, String filterKey, Color color) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(color); // Cor do texto baseada no status
        btn.setFocusPainted(false);
        // Estilo: Alinhado à esquerda, sem fundo padrão
        btn.putClientProperty(FlatClientProperties.STYLE, "background:null; borderWidth:0; arc:15; margin:12,20,12,20; textAlign:left");

        // Indicador visual de hover
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(50, 50, 55));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(null);
            }
        });

        btn.addActionListener(e -> {
            this.currentFilter = filterKey;
            refresh();
        });
        return btn;
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setOpaque(false);

        lblStatusFilter = new JLabel("Todas as Tarefas");
        lblStatusFilter.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblStatusFilter.setForeground(Color.WHITE);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Pesquisar...");
        txtSearch.putClientProperty(FlatClientProperties.STYLE, "arc:15; padding:8,10,8,10; borderWidth:0");
        txtSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { refresh(); } });

        JButton btnNew = new JButton("+ Nova Tarefa");
        btnNew.setBackground(COL_ACCENT);
        btnNew.setForeground(Color.WHITE);
        btnNew.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNew.putClientProperty(FlatClientProperties.STYLE, "arc:15; borderWidth:0; margin:8,20,8,20");
        btnNew.addActionListener(e -> handleAddOrEdit(null));

        actions.add(txtSearch);
        actions.add(btnNew);

        p.add(lblStatusFilter, BorderLayout.WEST);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    // ============================================================================================
    // --- LÓGICA PRINCIPAL (REFRESH & CONTADORES) ---
    // ============================================================================================

    private void refresh() {
        String q = txtSearch.getText();
        List<Task> all = service.search(q); // Pega TUDO para calcular os contadores

        // 1. CALCULAR CONTADORES
        long countAll = all.size();
        long countDone = all.stream().filter(Task::isCompleted).count();
        long countLate = all.stream().filter(t -> isLate(t)).count();
        long countProgress = all.stream().filter(t -> !t.isCompleted() && !isLate(t)).count();

        // 2. ATUALIZAR TEXTO DOS BOTÕES DA SIDEBAR
        updateSidebarText(btnFilterAll, "Todas", countAll);
        updateSidebarText(btnFilterProgress, "Em Andamento", countProgress);
        updateSidebarText(btnFilterLate, "Atrasadas", countLate);
        updateSidebarText(btnFilterDone, "Concluídas", countDone);

        // 3. FILTRAR LISTA PARA A TABELA
        List<Task> filtered = all.stream().filter(t -> {
            switch (currentFilter) {
                case "DONE":     return t.isCompleted();
                case "LATE":     return isLate(t);
                case "PROGRESS": return !t.isCompleted() && !isLate(t);
                default:         return true; // ALL
            }
        }).collect(Collectors.toList());

        // 4. ATUALIZAR TÍTULO
        updateHeaderTitle();

        // 5. POVOAR TABELA
        model.setRowCount(0);
        for (Task t : filtered) {
            String dateStr = (t instanceof DeadLineTask dt) ? dt.getPrazo().format(fmt) : "—";
            String statusTxt = t.isCompleted() ? "Concluída" : t.getStatusTempo();

            model.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    dateStr,
                    statusTxt,
                    t.isCompleted(),
                    t
            });
        }
    }

    // Helper para verificar atraso
    private boolean isLate(Task t) {
        if (t instanceof DeadLineTask dt) {
            return dt.getPrazo().isBefore(LocalDateTime.now()) && !dt.isCompleted();
        }
        return false;
    }

    // Helper para atualizar texto do botão com contador
    private void updateSidebarText(JButton btn, String title, long count) {
        // Ex: "Atrasadas (3)"
        btn.setText(String.format("%s  (%d)", title, count));
    }

    private void updateHeaderTitle() {
        switch (currentFilter) {
            case "DONE":
                lblStatusFilter.setText("Tarefas Concluídas");
                lblStatusFilter.setForeground(COL_DONE);
                break;
            case "LATE":
                lblStatusFilter.setText("Tarefas Atrasadas");
                lblStatusFilter.setForeground(COL_LATE);
                break;
            case "PROGRESS":
                lblStatusFilter.setText("Em Andamento");
                lblStatusFilter.setForeground(COL_PROGRESS);
                break;
            default:
                lblStatusFilter.setText("Todas as Tarefas");
                lblStatusFilter.setForeground(COL_ALL);
                break;
        }
    }

    private void setupTable() {
        String[] cols = {"ID", "Tarefa", "Prazo", "Status", "Conclusão", "Opções"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4 || c == 5; }
        };

        table = new JTable(model);
        table.setRowHeight(60);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Larguras
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setMinWidth(110); table.getColumnModel().getColumn(4).setMaxWidth(110);
        table.getColumnModel().getColumn(5).setMinWidth(60);  table.getColumnModel().getColumn(5).setMaxWidth(60);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRender);

        table.getColumnModel().getColumn(4).setCellRenderer(new CompleteButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new CompleteButtonEditor());

        table.getColumnModel().getColumn(5).setCellRenderer(new MenuButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new MenuButtonEditor());

        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (table.rowAtPoint(e.getPoint()) == -1) table.clearSelection();
            }
        });
    }

    // ============================================================================================
    // --- FORMULÁRIO ---
    // ============================================================================================

    private void handleAddOrEdit(Task tEdit) {
        JDialog d = new JDialog(this, tEdit == null ? " Nova Tarefa" : " Editar Tarefa", true);
        d.setSize(480, 550);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);
        d.setResizable(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(25, 30, 25, 30));
        p.setBackground(UIManager.getColor("Panel.background"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridx = 0; gbc.weightx = 1;

        p.add(createLabel("Título"), gbc);
        JTextField title = new JTextField(tEdit != null ? tEdit.getTitle() : "");
        title.putClientProperty(FlatClientProperties.STYLE, "arc:12; padding:8,10,8,10");
        p.add(title, gbc);

        p.add(createLabel("Descrição"), gbc);
        JTextArea desc = new JTextArea(tEdit != null ? tEdit.getDescription() : "", 3, 20);
        desc.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(desc);
        scrollDesc.putClientProperty(FlatClientProperties.STYLE, "arc:12; borderWidth:0");
        scrollDesc.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        p.add(scrollDesc, gbc);

        JPanel row = new JPanel(new GridLayout(1, 2, 15, 0));
        row.setOpaque(false);

        JPanel pType = new JPanel(new BorderLayout()); pType.setOpaque(false);
        pType.add(createLabel("Tipo"), BorderLayout.NORTH);
        JComboBox<String> type = new JComboBox<>(new String[]{"Simples", "Com Prazo"});
        pType.add(type, BorderLayout.CENTER);

        JPanel pDate = new JPanel(new BorderLayout()); pDate.setOpaque(false);
        pDate.add(createLabel("Data Limite"), BorderLayout.NORTH);
        JSpinner date = new JSpinner(new SpinnerDateModel());
        date.setEditor(new JSpinner.DateEditor(date, "dd/MM/yyyy HH:mm"));
        date.setEnabled(false);
        pDate.add(date, BorderLayout.CENTER);

        row.add(pType); row.add(pDate);
        p.add(row, gbc);

        type.addActionListener(e -> date.setEnabled(type.getSelectedIndex() == 1));
        if (tEdit instanceof DeadLineTask dt) {
            type.setSelectedIndex(1);
            date.setEnabled(true);
            date.setValue(Date.from(dt.getPrazo().atZone(ZoneId.systemDefault()).toInstant()));
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Salvar");
        btnSave.setBackground(COL_ACCENT);
        btnSave.setForeground(Color.WHITE);
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc:12; margin:8,20,8,20; borderWidth:0");

        btnSave.addActionListener(e -> {
            try {
                if(title.getText().isEmpty()) throw new Exception("Título obrigatório");
                Task novo;
                if (type.getSelectedIndex() == 0) novo = new SimpleTask(title.getText(), desc.getText());
                else novo = new DeadLineTask(title.getText(), desc.getText(),
                        ((Date)date.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

                if (tEdit != null) {
                    novo.setId(tEdit.getId());
                    novo.setCompleted(tEdit.isCompleted());
                    service.updateTask(novo);
                } else service.addTask(novo);

                refresh();
                d.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d, ex.getMessage()); }
        });

        footer.add(btnSave);
        d.add(p, BorderLayout.CENTER);
        d.add(footer, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.GRAY);
        l.setBorder(new EmptyBorder(0,0,5,0));
        return l;
    }

    // ============================================================================================
    // --- RENDERIZADORES CUSTOMIZADOS ---
    // ============================================================================================

    class CompleteButtonRenderer extends JButton implements TableCellRenderer {
        public CompleteButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusable(false);
            putClientProperty(FlatClientProperties.STYLE, "arc:10; margin:0,0,0,0; borderWidth:0");
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            boolean done = (boolean) v;
            if (done) {
                setText("✔ Feito");
                setBackground(COL_DONE);
                setForeground(Color.WHITE);
            } else {
                setText("Concluir");
                setBackground(new Color(50, 50, 50));
                setForeground(Color.LIGHT_GRAY);
            }
            return this;
        }
    }

    class CompleteButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton();
        private boolean done;
        private int taskId;
        public CompleteButtonEditor() {
            btn.setOpaque(true);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.putClientProperty(FlatClientProperties.STYLE, "arc:10; borderWidth:0");
            btn.addActionListener(e -> {
                service.toggleTask(taskId);
                stopCellEditing();
                SwingUtilities.invokeLater(() -> refresh());
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            done = (boolean) value;
            taskId = (int) table.getValueAt(row, 0);
            btn.setText(done ? "✔ Feito" : "Concluir");
            btn.setBackground(done ? COL_DONE : new Color(50, 50, 50));
            btn.setForeground(Color.WHITE);
            return btn;
        }
        @Override public Object getCellEditorValue() { return done; }
    }

    class MenuButtonRenderer extends JButton implements TableCellRenderer {
        private final Icon icon = new ThreeDotsIcon();

        public MenuButtonRenderer() {
            // Removemos o setText("⋮") e usamos o ícone
            setIcon(icon);
            setText("");

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            // Ajusta a cor para garantir visibilidade
            setForeground(Color.WHITE);
            return this;
        }
    }

    // --- EDITOR DO MENU COM ÍCONES ---
    class MenuButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton(); // Sem texto no construtor
        private final JPopupMenu popup = new JPopupMenu();
        private Task currentTask;

        public MenuButtonEditor() {
            // Configura o ícone aqui
            btn.setIcon(new ThreeDotsIcon());
            btn.setText("");

            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusable(false);

            // --- ÍCONES DO MENU (Opcional: Mantive os seus com texto) ---
            JMenuItem edit = new JMenuItem("   ✎  Editar");
            edit.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JMenuItem del = new JMenuItem("   🗑  Excluir");
            del.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            del.setForeground(new Color(239, 68, 68)); // Vermelho

            edit.addActionListener(e -> {
                stopCellEditing();
                handleAddOrEdit(currentTask);
            });

            del.addActionListener(e -> {
                stopCellEditing();
                if (JOptionPane.showConfirmDialog(btn, "Apagar tarefa?") == 0) {
                    service.deleteTask(currentTask.getId());
                    refresh();
                }
            });

            popup.add(edit);
            popup.add(del);

            btn.addActionListener(e -> {
                // Ajuste fino da posição do menu
                popup.show(btn, btn.getWidth() - 120, btn.getHeight());
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentTask = (Task) value;
            return btn;
        }

        @Override public Object getCellEditorValue() { return null; }
    }

    class ThreeDotsIcon implements Icon {
        private final int width = 24;
        private final int height = 24;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Usa a cor do texto do componente (Branco ou Cinza)
            g2.setColor(c.getForeground());

            int dotSize = 4; // Tamanho da bolinha
            int gap = 5;     // Espaço entre as bolinhas
            int centerX = x + (width / 2) - (dotSize / 2);
            int centerY = y + (height / 2) - (dotSize / 2);

            // Desenha 3 bolinhas verticais
            g2.fillOval(centerX, centerY - gap - dotSize, dotSize, dotSize); // Cima
            g2.fillOval(centerX, centerY, dotSize, dotSize);                 // Meio
            g2.fillOval(centerX, centerY + gap + dotSize, dotSize, dotSize); // Baixo

            g2.dispose();
        }

        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }

    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}