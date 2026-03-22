package com.rozen.ui;

import com.rozen.service.ConfigStorage;
import com.rozen.service.KeycloakService;
import com.rozen.service.KeycloakService.PageResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

import static com.rozen.ui.DialogUtil.*;

public class KeycloakClientManagerUI extends JFrame {

    private static final String VERSION = "1.0.0";
    private static final String GITHUB_URL = "https://github.com/Rozenone/keycloak_manager_tool";

    private KeycloakService keycloakService;
    private ConfigStorage configStorage;
    private ConfigStorage.ConnectionConfig currentConfig;

    // 客户端相关
    private JTable clientTable;
    private DefaultTableModel clientTableModel;

    // 用户相关
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JTextField userSearchField;
    private JComboBox<String> searchTypeCombo;
    private JCheckBox exactMatchCheckBox;

    // 分页相关
    private int currentPage = 0;
    private int pageSize = 20;
    private int totalUsers = 0;
    private JLabel pageInfoLabel;
    private JButton prevPageBtn;
    private JButton nextPageBtn;

    // 连接配置
    private JTextField serverUrlField;
    private JTextField realmField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox skipSslCheckBox;
    private JLabel statusLabel;

    public KeycloakClientManagerUI(ConfigStorage.ConnectionConfig config) {
        this.currentConfig = config;
        this.configStorage = new ConfigStorage();

        setTitle("Keycloak 管理工具 - " + config.getName());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initMenuBar();
        initComponents();

        // 自动填充配置并连接
        populateConfigFields(config);
        autoConnect();
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");

        JMenuItem githubItem = new JMenuItem("GitHub 仓库");
        githubItem.addActionListener(e -> openGitHub());
        helpMenu.add(githubItem);

        helpMenu.addSeparator();

        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void openGitHub() {
        try {
            Desktop.getDesktop().browse(new URI(GITHUB_URL));
        } catch (Exception ex) {
            showError(this, "错误", "无法打开浏览器: " + ex.getMessage());
        }
    }

    private void showAbout() {
        String message = "Keycloak 管理工具\n" +
                "版本: " + VERSION + "\n" +
                "\n" +
                "GitHub: " + GITHUB_URL + "\n" +
                "\n" +
                "功能:\n" +
                "- Keycloak 客户端管理\n" +
                "- 用户管理和搜索\n" +
                "- 连接配置本地存储\n" +
                "- SSL 证书验证跳过\n" +
                "- 作者邮箱:rozenone@foxmail.com\n" +
                "\n" +
                "© 2026 Rozenone";

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this, textArea, "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initComponents() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 连接配置面板
        mainPanel.add(createConnectionPanel(), BorderLayout.NORTH);

        // 创建标签页
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("客户端管理", createClientPanel());
        tabbedPane.addTab("用户管理", createUserPanel());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 状态栏
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keycloak 连接配置"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 服务器地址
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("服务器地址:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        serverUrlField = new JTextField(30);
        panel.add(serverUrlField, gbc);

        // Realm
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Realm:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        realmField = new JTextField(15);
        panel.add(realmField, gbc);

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // 跳过 SSL 验证
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("跳过 SSL 验证:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        skipSslCheckBox = new JCheckBox("(用于自签名证书或 IP 地址访问)");
        panel.add(skipSslCheckBox, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton connectBtn = new JButton("连接");
        connectBtn.addActionListener(this::onConnect);
        buttonPanel.add(connectBtn);

        JButton saveBtn = new JButton("保存当前配置");
        saveBtn.addActionListener(this::onSaveConfig);
        buttonPanel.add(saveBtn);

        JButton saveAsBtn = new JButton("另存为新配置");
        saveAsBtn.addActionListener(this::onSaveAsNewConfig);
        buttonPanel.add(saveAsBtn);

        JButton switchBtn = new JButton("切换配置");
        switchBtn.addActionListener(this::onSwitchConfig);
        buttonPanel.add(switchBtn);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createClientPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 表格
        String[] columns = {"客户端ID", "名称", "启用状态", "协议", "根URL"};
        clientTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clientTable = new JTable(clientTableModel);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        // 添加双击事件
        clientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = clientTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        showClientDetail(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(clientTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.addActionListener(this::onRefreshClients);
        buttonPanel.add(refreshBtn);

        JButton addBtn = new JButton("新增客户端");
        addBtn.addActionListener(this::onAddClient);
        buttonPanel.add(addBtn);

        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(this::onEditClient);
        buttonPanel.add(editBtn);

        JButton deleteBtn = new JButton("删除");
        deleteBtn.addActionListener(this::onDeleteClient);
        buttonPanel.add(deleteBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 搜索面板
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 搜索类型
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        searchPanel.add(new JLabel("搜索类型:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        searchTypeCombo = new JComboBox<>(new String[]{"全部", "ID", "用户名", "邮箱", "自定义属性"});
        searchPanel.add(searchTypeCombo, gbc);

        // 精确匹配选项
        gbc.gridx = 2;
        gbc.weightx = 0;
        exactMatchCheckBox = new JCheckBox("精确匹配");
        searchPanel.add(exactMatchCheckBox, gbc);

        // 每页数量
        gbc.gridx = 3;
        searchPanel.add(new JLabel("每页:"), gbc);

        gbc.gridx = 4;
        JComboBox<Integer> pageSizeCombo = new JComboBox<>(new Integer[]{10, 20, 50, 100});
        pageSizeCombo.setSelectedItem(pageSize);
        pageSizeCombo.addActionListener(e -> {
            pageSize = (Integer) pageSizeCombo.getSelectedItem();
            currentPage = 0;
            loadUsers();
        });
        searchPanel.add(pageSizeCombo, gbc);

        row++;

        // 搜索关键词
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        searchPanel.add(new JLabel("关键词:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        userSearchField = new JTextField(30);
        searchPanel.add(userSearchField, gbc);

        // 搜索按钮
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> searchUsers());
        searchPanel.add(searchBtn, gbc);

        // 清除按钮
        gbc.gridx = 3;
        gbc.gridwidth = 2;
        JButton clearBtn = new JButton("清除");
        clearBtn.addActionListener(e -> {
            userSearchField.setText("");
            searchTypeCombo.setSelectedIndex(0);
            exactMatchCheckBox.setSelected(false);
            currentPage = 0;
            loadUsers();
        });
        searchPanel.add(clearBtn, gbc);

        panel.add(searchPanel, BorderLayout.NORTH);

        // 用户表格
        String[] columns = {"用户ID", "用户名", "表示名", "邮箱", "姓名", "启用状态", "邮箱验证"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        // 添加双击事件
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = userTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        showUserDetail(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 分页控制面板
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        prevPageBtn = new JButton("上一页");
        prevPageBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadUsers();
            }
        });
        paginationPanel.add(prevPageBtn);

        pageInfoLabel = new JLabel("第 1 页 / 共 1 页 (共 0 条)");
        paginationPanel.add(pageInfoLabel);

        nextPageBtn = new JButton("下一页");
        nextPageBtn.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadUsers();
            }
        });
        paginationPanel.add(nextPageBtn);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.addActionListener(e -> loadUsers());
        leftButtonPanel.add(refreshBtn);

        JButton addBtn = new JButton("新增用户");
        addBtn.addActionListener(this::onAddUser);
        leftButtonPanel.add(addBtn);

        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(this::onEditUser);
        leftButtonPanel.add(editBtn);

        JButton deleteBtn = new JButton("删除");
        deleteBtn.addActionListener(this::onDeleteUser);
        leftButtonPanel.add(deleteBtn);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(paginationPanel, BorderLayout.EAST);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void populateConfigFields(ConfigStorage.ConnectionConfig config) {
        serverUrlField.setText(config.getServerUrl());
        realmField.setText(config.getRealm());
        usernameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());
        skipSslCheckBox.setSelected(config.isSkipSslVerify());
    }

    private void autoConnect() {
        String serverUrl = serverUrlField.getText().trim();
        String realm = realmField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean skipSslVerify = skipSslCheckBox.isSelected();

        if (serverUrl.isEmpty() || realm.isEmpty() || username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("配置不完整，请手动连接");
            statusLabel.setForeground(Color.ORANGE);
            return;
        }

        try {
            keycloakService = new KeycloakService(serverUrl, realm, username, password, skipSslVerify);
            statusLabel.setText("已连接到: " + serverUrl);
            statusLabel.setForeground(Color.GREEN.darker());
            loadClients();
            loadUsers();
        } catch (Exception ex) {
            statusLabel.setText("自动连接失败: " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void onConnect(ActionEvent e) {
        String serverUrl = serverUrlField.getText().trim();
        String realm = realmField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean skipSslVerify = skipSslCheckBox.isSelected();

        if (serverUrl.isEmpty() || realm.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError(this, "错误", "请填写所有连接信息");
            return;
        }

        try {
            keycloakService = new KeycloakService(serverUrl, realm, username, password, skipSslVerify);
            statusLabel.setText("已连接到: " + serverUrl);
            statusLabel.setForeground(Color.GREEN.darker());
            loadClients();
            loadUsers();
        } catch (Exception ex) {
            showError(this, "连接失败", ex.getMessage());
            statusLabel.setText("连接失败");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void onSaveConfig(ActionEvent e) {
        if (currentConfig == null) {
            showError(this, "错误", "当前没有配置可保存");
            return;
        }

        currentConfig.setServerUrl(serverUrlField.getText().trim());
        currentConfig.setRealm(realmField.getText().trim());
        currentConfig.setUsername(usernameField.getText().trim());
        currentConfig.setPassword(new String(passwordField.getPassword()));
        currentConfig.setSkipSslVerify(skipSslCheckBox.isSelected());

        try {
            configStorage.saveConfig(currentConfig);
            showInfo(this, "成功", "配置已保存");
        } catch (Exception ex) {
            showError(this, "保存失败", ex.getMessage());
        }
    }

    private void onSaveAsNewConfig(ActionEvent e) {
        String name = JOptionPane.showInputDialog(this, "请输入新配置名称:", "保存新配置", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        ConfigStorage.ConnectionConfig newConfig = new ConfigStorage.ConnectionConfig();
        newConfig.setName(name.trim());
        newConfig.setServerUrl(serverUrlField.getText().trim());
        newConfig.setRealm(realmField.getText().trim());
        newConfig.setUsername(usernameField.getText().trim());
        newConfig.setPassword(new String(passwordField.getPassword()));
        newConfig.setSkipSslVerify(skipSslCheckBox.isSelected());

        try {
            configStorage.saveConfig(newConfig);
            currentConfig = newConfig;
            setTitle("Keycloak 管理工具 - " + newConfig.getName());
            showInfo(this, "成功", "新配置已保存");
        } catch (Exception ex) {
            showError(this, "保存失败", ex.getMessage());
        }
    }

    private void onSwitchConfig(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "切换配置将关闭当前窗口并重新启动，是否继续?",
            "确认切换",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();

            SwingUtilities.invokeLater(() -> {
                ConfigSelectionDialog dialog = new ConfigSelectionDialog(null);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    ConfigStorage.ConnectionConfig config = dialog.getSelectedConfig();
                    if (config != null) {
                        KeycloakClientManagerUI ui = new KeycloakClientManagerUI(config);
                        ui.setVisible(true);
                    }
                }
            });
        }
    }

    // ==================== 客户端相关方法 ====================

    private void loadClients() {
        if (keycloakService == null) {
            showWarning(this, "提示", "请先连接到 Keycloak");
            return;
        }

        clientTableModel.setRowCount(0);
        try {
            List<ClientInfo> clients = keycloakService.getClients();
            for (ClientInfo client : clients) {
                clientTableModel.addRow(new Object[]{
                    client.getClientId(),
                    client.getName(),
                    client.isEnabled() ? "启用" : "禁用",
                    client.getProtocol(),
                    client.getRootUrl()
                });
            }
            statusLabel.setText("共 " + clients.size() + " 个客户端");
        } catch (Exception ex) {
            showError(this, "加载客户端失败", ex.getMessage());
        }
    }

    private void onRefreshClients(ActionEvent e) {
        loadClients();
    }

    private void onAddClient(ActionEvent e) {
        if (keycloakService == null) {
            showWarning(this, "提示", "请先连接到 Keycloak");
            return;
        }

        ClientDialog dialog = new ClientDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.createClient(dialog.getClientInfo());
                loadClients();
                showInfo(this, "成功", "客户端创建成功");
            } catch (Exception ex) {
                showError(this, "创建失败", ex.getMessage());
            }
        }
    }

    private void onEditClient(ActionEvent e) {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, "提示", "请先选择一个客户端");
            return;
        }

        String clientId = (String) clientTableModel.getValueAt(selectedRow, 0);
        ClientInfo clientInfo = keycloakService.getClientByClientId(clientId);
        
        if (clientInfo == null) {
            showError(this, "错误", "无法获取客户端信息");
            return;
        }

        ClientDialog dialog = new ClientDialog(this, clientInfo);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.updateClient(clientId, dialog.getClientInfo());
                loadClients();
                showInfo(this, "成功", "客户端更新成功");
            } catch (Exception ex) {
                showError(this, "更新失败", ex.getMessage());
            }
        }
    }

    private void onDeleteClient(ActionEvent e) {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, "提示", "请先选择一个客户端");
            return;
        }

        String clientId = (String) clientTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除客户端 '" + clientId + "' 吗?",
            "确认删除",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                keycloakService.deleteClient(clientId);
                loadClients();
                showInfo(this, "成功", "删除成功");
            } catch (Exception ex) {
                showError(this, "删除失败", ex.getMessage());
            }
        }
    }

    private void showClientDetail(int row) {
        String clientId = (String) clientTableModel.getValueAt(row, 0);
        ClientInfo clientInfo = keycloakService.getClientByClientId(clientId);
        
        if (clientInfo != null) {
            ClientDetailDialog dialog = new ClientDetailDialog(this, clientInfo);
            dialog.setVisible(true);
        }
    }

    // ==================== 用户相关方法 ====================

    private void loadUsers() {
        if (keycloakService == null) {
            return;
        }

        try {
            PageResult<UserInfo> result = keycloakService.getUsers(currentPage, pageSize);
            displayUsers(result);
        } catch (Exception ex) {
            showError(this, "加载用户失败", ex.getMessage());
        }
    }

    private void searchUsers() {
        if (keycloakService == null) {
            showWarning(this, "提示", "请先连接到 Keycloak");
            return;
        }

        String search = userSearchField.getText().trim();
        if (search.isEmpty()) {
            currentPage = 0;
            loadUsers();
            return;
        }

        String searchType = (String) searchTypeCombo.getSelectedItem();
        boolean exactMatch = exactMatchCheckBox.isSelected();

        try {
            PageResult<UserInfo> result;

            if ("自定义属性".equals(searchType)) {
                if (search.contains("=")) {
                    String[] parts = search.split("=", 2);
                    String attrName = parts[0].trim();
                    String attrValue = parts[1].trim();
                    List<UserInfo> users = keycloakService.searchUsersByAttribute(attrName, attrValue);
                    result = new PageResult<>(users, users.size(), 0, users.size());
                } else {
                    showWarning(this, "提示", "自定义属性搜索格式：属性名=属性值");
                    return;
                }
            } else {
                String type = "全部".equals(searchType) ? "all" : 
                              "ID".equals(searchType) ? "id" :
                              "用户名".equals(searchType) ? "username" : "email";
                result = keycloakService.searchUsers(search, type, exactMatch, currentPage, pageSize);
            }

            displayUsers(result);
        } catch (Exception ex) {
            showError(this, "搜索用户失败", ex.getMessage());
        }
    }

    private void displayUsers(PageResult<UserInfo> result) {
        userTableModel.setRowCount(0);
        List<UserInfo> users = result.getContent();
        totalUsers = result.getTotalCount();

        for (UserInfo user : users) {
            userTableModel.addRow(new Object[]{
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail() != null ? user.getEmail() : "",
                user.getFullName(),
                user.isEnabled() ? "启用" : "禁用",
                user.isEmailVerified() ? "是" : "否"
            });
        }

        // 更新分页信息
        int totalPages = result.getTotalPages();
        int currentPageNum = result.getPage() + 1;
        pageInfoLabel.setText(String.format("第 %d 页 / 共 %d 页 (共 %d 条)", 
            currentPageNum, Math.max(1, totalPages), totalUsers));

        // 更新按钮状态
        prevPageBtn.setEnabled(result.getPage() > 0);
        nextPageBtn.setEnabled(result.getPage() < totalPages - 1);

        statusLabel.setText(String.format("共 %d 个用户，当前显示 %d 条", totalUsers, users.size()));
    }

    private void onAddUser(ActionEvent e) {
        if (keycloakService == null) {
            showWarning(this, "提示", "请先连接到 Keycloak");
            return;
        }

        UserDialog dialog = new UserDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.createUser(dialog.getUserInfo());
                loadUsers();
                showInfo(this, "成功", "用户创建成功");
            } catch (Exception ex) {
                showError(this, "创建失败", ex.getMessage());
            }
        }
    }

    private void onEditUser(ActionEvent e) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, "提示", "请先选择一个用户");
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        UserInfo userInfo = keycloakService.getUserById(userId);
        
        if (userInfo == null) {
            showError(this, "错误", "无法获取用户信息");
            return;
        }

        UserDialog dialog = new UserDialog(this, userInfo);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.updateUser(userId, dialog.getUserInfo());
                loadUsers();
                showInfo(this, "成功", "用户更新成功");
            } catch (Exception ex) {
                showError(this, "更新失败", ex.getMessage());
            }
        }
    }

    private void onDeleteUser(ActionEvent e) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, "提示", "请先选择一个用户");
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除用户 '" + username + "' 吗?",
            "确认删除",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                keycloakService.deleteUser(userId);
                loadUsers();
                showInfo(this, "成功", "删除成功");
            } catch (Exception ex) {
                showError(this, "删除失败", ex.getMessage());
            }
        }
    }

    private void showUserDetail(int row) {
        String userId = (String) userTableModel.getValueAt(row, 0);
        UserInfo userInfo = keycloakService.getUserById(userId);
        
        if (userInfo != null) {
            UserDetailDialog dialog = new UserDetailDialog(this, userInfo);
            dialog.setVisible(true);
        }
    }
}
