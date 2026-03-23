package com.rozen.ui;

import com.rozen.service.ConfigStorage;
import com.rozen.service.ConfigStorage.AuthType;
import com.rozen.service.ConfigStorage.ProxyProtocol;
import com.rozen.service.KeycloakService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

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

    // 登录方式选择
    private JComboBox<AuthType> authTypeComboBox;

    // 用户名密码方式
    private JTextField usernameField;
    private JPasswordField passwordField;

    // 客户端凭据方式
    private JTextField clientIdField;
    private JPasswordField clientSecretField;

    // 代理配置
    private JCheckBox useProxyCheckBox;
    private JComboBox<ProxyProtocol> proxyProtocolComboBox;
    private JTextField proxyHostField;
    private JSpinner proxyPortSpinner;

    private JCheckBox skipSslCheckBox;

    private JTabbedPane tabbedPane;

    // 面板容器
    private JPanel usernamePasswordPanel;
    private JPanel clientCredentialsPanel;
    private JPanel authCardPanel;

    public ConfigSelectionDialog(JFrame parent) {
        super(parent, "选择 Keycloak 连接配置", true);
        this.configStorage = new ConfigStorage();

        setSize(550, 650);
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

        JButton testBtn = new JButton("测试连接");
        testBtn.addActionListener(this::onTestConnection);
        buttonPanel.add(testBtn);

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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建主内容面板，使用 GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;

        // ========== 基本配置区域 ==========
        JPanel basicPanel = new JPanel(new GridBagLayout());
        basicPanel.setBorder(BorderFactory.createTitledBorder("基本配置"));
        GridBagConstraints gbcBasic = new GridBagConstraints();
        gbcBasic.insets = new Insets(5, 5, 5, 5);
        gbcBasic.fill = GridBagConstraints.HORIZONTAL;
        gbcBasic.anchor = GridBagConstraints.WEST;

        // 配置名称
        gbcBasic.gridx = 0;
        gbcBasic.gridy = 0;
        gbcBasic.weightx = 0;
        basicPanel.add(new JLabel("配置名称 *:"), gbcBasic);

        gbcBasic.gridx = 1;
        gbcBasic.weightx = 1.0;
        nameField = new JTextField(20);
        basicPanel.add(nameField, gbcBasic);

        // 服务器地址
        gbcBasic.gridx = 0;
        gbcBasic.gridy = 1;
        gbcBasic.weightx = 0;
        basicPanel.add(new JLabel("服务器地址 *:"), gbcBasic);

        gbcBasic.gridx = 1;
        gbcBasic.weightx = 1.0;
        serverUrlField = new JTextField("http://localhost:8080", 20);
        basicPanel.add(serverUrlField, gbcBasic);

        // Realm
        gbcBasic.gridx = 0;
        gbcBasic.gridy = 2;
        gbcBasic.weightx = 0;
        basicPanel.add(new JLabel("Realm *:"), gbcBasic);

        gbcBasic.gridx = 1;
        gbcBasic.weightx = 1.0;
        realmField = new JTextField("master", 20);
        basicPanel.add(realmField, gbcBasic);

        gbc.gridx = 0;
        gbc.gridy = row++;
        contentPanel.add(basicPanel, gbc);

        // ========== 认证配置区域 ==========
        JPanel authPanel = new JPanel(new BorderLayout(5, 5));
        authPanel.setBorder(BorderFactory.createTitledBorder("认证配置"));

        // 登录方式选择
        JPanel authTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authTypePanel.add(new JLabel("登录方式:"));
        authTypeComboBox = new JComboBox<>(AuthType.values());
        authTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AuthType) {
                    setText(((AuthType) value).getDisplayName());
                }
                return this;
            }
        });
        authTypeComboBox.addItemListener(this::onAuthTypeChanged);
        authTypePanel.add(authTypeComboBox);
        authPanel.add(authTypePanel, BorderLayout.NORTH);

        // 卡片布局切换面板
        authCardPanel = new JPanel(new CardLayout());

        // 用户名密码面板
        usernamePasswordPanel = createUsernamePasswordCard();
        authCardPanel.add(usernamePasswordPanel, "USERNAME_PASSWORD");

        // 客户端凭据面板
        clientCredentialsPanel = createClientCredentialsCard();
        authCardPanel.add(clientCredentialsPanel, "CLIENT_CREDENTIALS");

        authPanel.add(authCardPanel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = row++;
        contentPanel.add(authPanel, gbc);

        // ========== 代理配置区域 ==========
        JPanel proxyPanel = new JPanel(new GridBagLayout());
        proxyPanel.setBorder(BorderFactory.createTitledBorder("代理配置"));
        GridBagConstraints gbcProxy = new GridBagConstraints();
        gbcProxy.insets = new Insets(5, 5, 5, 5);
        gbcProxy.fill = GridBagConstraints.HORIZONTAL;
        gbcProxy.anchor = GridBagConstraints.WEST;

        // 使用代理
        gbcProxy.gridx = 0;
        gbcProxy.gridy = 0;
        gbcProxy.weightx = 0;
        proxyPanel.add(new JLabel("使用代理:"), gbcProxy);

        gbcProxy.gridx = 1;
        gbcProxy.weightx = 1.0;
        useProxyCheckBox = new JCheckBox("启用 HTTP/HTTPS 代理");
        useProxyCheckBox.addItemListener(this::onUseProxyChanged);
        proxyPanel.add(useProxyCheckBox, gbcProxy);

        // 代理协议
        gbcProxy.gridx = 0;
        gbcProxy.gridy = 1;
        gbcProxy.weightx = 0;
        proxyPanel.add(new JLabel("代理协议:"), gbcProxy);

        gbcProxy.gridx = 1;
        gbcProxy.weightx = 1.0;
        proxyProtocolComboBox = new JComboBox<>(ProxyProtocol.values());
        proxyProtocolComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ProxyProtocol) {
                    setText(((ProxyProtocol) value).getDisplayName());
                }
                return this;
            }
        });
        proxyProtocolComboBox.setEnabled(false);
        proxyPanel.add(proxyProtocolComboBox, gbcProxy);

        // 代理主机
        gbcProxy.gridx = 0;
        gbcProxy.gridy = 2;
        gbcProxy.weightx = 0;
        proxyPanel.add(new JLabel("代理主机:"), gbcProxy);

        gbcProxy.gridx = 1;
        gbcProxy.weightx = 1.0;
        proxyHostField = new JTextField(20);
        proxyHostField.setEnabled(false);
        proxyPanel.add(proxyHostField, gbcProxy);

        // 代理端口
        gbcProxy.gridx = 0;
        gbcProxy.gridy = 3;
        gbcProxy.weightx = 0;
        proxyPanel.add(new JLabel("代理端口:"), gbcProxy);

        gbcProxy.gridx = 1;
        gbcProxy.weightx = 1.0;
        SpinnerNumberModel portModel = new SpinnerNumberModel(8080, 1, 65535, 1);
        proxyPortSpinner = new JSpinner(portModel);
        proxyPortSpinner.setEnabled(false);
        proxyPanel.add(proxyPortSpinner, gbcProxy);

        gbc.gridx = 0;
        gbc.gridy = row++;
        contentPanel.add(proxyPanel, gbc);

        // ========== SSL 配置区域 ==========
        JPanel sslPanel = new JPanel(new GridBagLayout());
        sslPanel.setBorder(BorderFactory.createTitledBorder("SSL 配置"));
        GridBagConstraints gbcSsl = new GridBagConstraints();
        gbcSsl.insets = new Insets(5, 5, 5, 5);
        gbcSsl.fill = GridBagConstraints.HORIZONTAL;
        gbcSsl.anchor = GridBagConstraints.WEST;

        // 跳过 SSL 验证
        gbcSsl.gridx = 0;
        gbcSsl.gridy = 0;
        gbcSsl.weightx = 0;
        sslPanel.add(new JLabel("跳过 SSL 验证:"), gbcSsl);

        gbcSsl.gridx = 1;
        gbcSsl.weightx = 1.0;
        skipSslCheckBox = new JCheckBox("启用（用于自签名证书或 IP 地址访问）");
        sslPanel.add(skipSslCheckBox, gbcSsl);

        gbc.gridx = 0;
        gbc.gridy = row++;
        contentPanel.add(sslPanel, gbc);

        // 填充剩余空间
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(new JPanel(), gbc);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createUsernamePasswordCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("用户名密码认证"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("用户名 *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField("admin", 20);
        panel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("密码 *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        return panel;
    }

    private JPanel createClientCredentialsCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("客户端凭据认证"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 客户端ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("客户端 ID *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        clientIdField = new JTextField(20);
        panel.add(clientIdField, gbc);

        // 客户端密钥
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("客户端密钥 *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        clientSecretField = new JPasswordField(20);
        panel.add(clientSecretField, gbc);

        return panel;
    }

    private void onAuthTypeChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            updateAuthCard();
        }
    }

    private void updateAuthCard() {
        AuthType selectedType = (AuthType) authTypeComboBox.getSelectedItem();
        if (authCardPanel != null) {
            CardLayout cardLayout = (CardLayout) authCardPanel.getLayout();
            if (selectedType == AuthType.USERNAME_PASSWORD) {
                cardLayout.show(authCardPanel, "USERNAME_PASSWORD");
            } else {
                cardLayout.show(authCardPanel, "CLIENT_CREDENTIALS");
            }
        }
    }

    private void onUseProxyChanged(ItemEvent e) {
        boolean enabled = useProxyCheckBox.isSelected();
        proxyProtocolComboBox.setEnabled(enabled);
        proxyHostField.setEnabled(enabled);
        proxyPortSpinner.setEnabled(enabled);
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

    private ConfigStorage.ConnectionConfig buildConfigFromFields() {
        ConfigStorage.ConnectionConfig config = new ConfigStorage.ConnectionConfig();
        config.setName(nameField.getText().trim());
        config.setServerUrl(serverUrlField.getText().trim());
        config.setRealm(realmField.getText().trim());
        config.setAuthType((AuthType) authTypeComboBox.getSelectedItem());
        config.setUseProxy(useProxyCheckBox.isSelected());
        config.setProxyProtocol((ProxyProtocol) proxyProtocolComboBox.getSelectedItem());
        config.setProxyHost(proxyHostField.getText().trim());
        config.setProxyPort((Integer) proxyPortSpinner.getValue());
        config.setSkipSslVerify(skipSslCheckBox.isSelected());

        // 设置登录凭据
        if (config.getAuthType() == AuthType.USERNAME_PASSWORD) {
            config.setUsername(usernameField.getText().trim());
            config.setPassword(new String(passwordField.getPassword()));
        } else {
            config.setClientId(clientIdField.getText().trim());
            config.setClientSecret(new String(clientSecretField.getPassword()));
        }

        return config;
    }

    private boolean validateConfig(ConfigStorage.ConnectionConfig config) {
        // 验证必填项
        if (config.getName().isEmpty() || config.getServerUrl().isEmpty() || config.getRealm().isEmpty()) {
            showError(this, "错误", "请填写所有必填项（配置名称、服务器地址、Realm）");
            return false;
        }

        // 根据登录方式验证对应字段
        if (config.getAuthType() == AuthType.USERNAME_PASSWORD) {
            if (config.getUsername().isEmpty() || config.getPassword().isEmpty()) {
                showError(this, "错误", "请填写用户名和密码");
                return false;
            }
        } else {
            if (config.getClientId().isEmpty() || config.getClientSecret().isEmpty()) {
                showError(this, "错误", "请填写客户端 ID 和客户端密钥");
                return false;
            }
        }

        // 验证代理配置
        if (config.isUseProxy() && config.getProxyHost().isEmpty()) {
            showError(this, "错误", "请填写代理主机地址");
            return false;
        }

        return true;
    }

    private void onTestConnection(ActionEvent e) {
        int selectedTab = tabbedPane.getSelectedIndex();

        ConfigStorage.ConnectionConfig testConfig;
        if (selectedTab == 0) {
            // 从列表选择
            if (selectedConfig == null) {
                showWarning(this, "提示", "请选择一个配置或使用新建配置标签页");
                return;
            }
            testConfig = selectedConfig;
        } else {
            // 新建配置 - 从界面构建配置
            testConfig = buildConfigFromFields();
            if (!validateConfig(testConfig)) {
                return;
            }
        }

        // 显示测试中的提示
        setTitle("选择 Keycloak 连接配置 - 正在测试连接...");

        // 在后台线程中执行连接测试
        ConfigStorage.ConnectionConfig finalTestConfig = testConfig;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String resultMessage;
            private boolean isSuccess;

            @Override
            protected Void doInBackground() {
                try (KeycloakService testService = KeycloakService.fromConfig(finalTestConfig)) {
                    // 尝试获取服务器信息来验证连接
                    testService.getClients();
                    resultMessage = "连接测试成功！\n\n" +
                            "服务器: " + finalTestConfig.getServerUrl() + "\n" +
                            "Realm: " + finalTestConfig.getRealm() + "\n" +
                            "登录方式: " + finalTestConfig.getAuthType().getDisplayName();
                    if (finalTestConfig.isUseProxy()) {
                        resultMessage += "\n代理: " + finalTestConfig.getProxyProtocol().getDisplayName() +
                                "://" + finalTestConfig.getProxyHost() + ":" + finalTestConfig.getProxyPort();
                    }
                    isSuccess = true;
                } catch (Exception ex) {
                    resultMessage = "连接测试失败！\n\n错误信息: " + ex.getMessage();
                    isSuccess = false;
                }
                return null;
            }

            @Override
            protected void done() {
                setTitle("选择 Keycloak 连接配置");
                if (isSuccess) {
                    showInfo(ConfigSelectionDialog.this, "连接测试结果", resultMessage);
                } else {
                    showError(ConfigSelectionDialog.this, "连接测试结果", resultMessage);
                }
            }
        };

        worker.execute();
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
            selectedConfig = buildConfigFromFields();
            if (!validateConfig(selectedConfig)) {
                return;
            }

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
