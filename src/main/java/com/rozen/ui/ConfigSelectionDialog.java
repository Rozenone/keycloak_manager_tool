package com.rozen.ui;

import com.rozen.service.ConfigStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

import static com.rozen.ui.DialogUtil.*;

public class ConfigSelectionDialog extends JDialog {

    private boolean confirmed = false;
    private ConfigStorage.ConnectionConfig selectedConfig;
    private ConfigStorage configStorage;

    private JList<String> configList;
    private DefaultListModel<String> listModel;

    // 新建配置的输入字段
    private JTextField nameField;
    private JTextField serverUrlField;
    private JTextField realmField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox skipSslCheckBox;

    private JTabbedPane tabbedPane;

    public ConfigSelectionDialog(JFrame parent) {
        super(parent, "选择 Keycloak 连接配置", true);
        this.configStorage = new ConfigStorage();

        setSize(500, 400);
        setLocationRelativeTo(parent);
        initComponents();
        loadSavedConfigs();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建标签页
        tabbedPane = new JTabbedPane();

        // 标签页1：选择已有配置
        tabbedPane.addTab("选择已有配置", createSelectPanel());

        // 标签页2：新建配置
        tabbedPane.addTab("新建配置", createNewConfigPanel());

        panel.add(tabbedPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton connectBtn = new JButton("连接");
        connectBtn.addActionListener(this::onConnect);
        buttonPanel.add(connectBtn);

        JButton cancelBtn = new JButton("退出");
        cancelBtn.addActionListener(e -> System.exit(0));
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private JPanel createSelectPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 配置列表
        listModel = new DefaultListModel<>();
        configList = new JList<>(listModel);
        configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = configList.getSelectedValue();
                if (selectedName != null) {
                    selectedConfig = configStorage.loadConfig(selectedName);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(configList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton deleteBtn = new JButton("删除选中配置");
        deleteBtn.addActionListener(this::onDelete);
        buttonPanel.add(deleteBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNewConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // 配置名称
        addFormRow(panel, gbc, row++, "配置名称 *:", nameField = new JTextField(20));

        // 服务器地址
        addFormRow(panel, gbc, row++, "服务器地址 *:", serverUrlField = new JTextField("http://localhost:8080", 20));

        // Realm
        addFormRow(panel, gbc, row++, "Realm *:", realmField = new JTextField("master", 20));

        // 用户名
        addFormRow(panel, gbc, row++, "用户名 *:", usernameField = new JTextField("admin", 20));

        // 密码
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.weightx = 0;
        panel.add(new JLabel("密码 *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // 跳过 SSL 验证
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("跳过 SSL 验证:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        skipSslCheckBox = new JCheckBox("(用于自签名证书或 IP 地址访问)");
        skipSslCheckBox.setSelected(false);
        panel.add(skipSslCheckBox, gbc);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void loadSavedConfigs() {
        listModel.clear();
        java.util.List<String> names = configStorage.getConfigNames();
        for (String name : names) {
            listModel.addElement(name);
        }
    }

    private void onConnect(ActionEvent e) {
        int selectedTab = tabbedPane.getSelectedIndex();

        if (selectedTab == 0) {
            // 从列表选择
            if (selectedConfig == null) {
                showWarning(this, "提示", "请选择一个配置");
                return;
            }
        } else {
            // 新建配置
            String name = nameField.getText().trim();
            String serverUrl = serverUrlField.getText().trim();
            String realm = realmField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            boolean skipSslVerify = skipSslCheckBox.isSelected();

            if (name.isEmpty() || serverUrl.isEmpty() || realm.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showError(this, "错误", "请填写所有必填项");
                return;
            }

            selectedConfig = new ConfigStorage.ConnectionConfig(name, serverUrl, realm, username, password, skipSslVerify);

            // 保存配置
            configStorage.saveConfig(selectedConfig);
        }

        confirmed = true;
        dispose();
    }

    private void onDelete(ActionEvent e) {
        String selectedName = configList.getSelectedValue();
        if (selectedName == null) {
            showWarning(this, "提示", "请选择一个配置");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除配置 '" + selectedName + "' 吗?",
            "确认删除",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            configStorage.deleteConfig(selectedName);
            loadSavedConfigs();
            selectedConfig = null;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ConfigStorage.ConnectionConfig getSelectedConfig() {
        return selectedConfig;
    }
}
