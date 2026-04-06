package com.rozen.ui;

import com.rozen.constant.MessageConstants;
import com.rozen.model.ClientInfo;
import com.rozen.model.UserInfo;
import com.rozen.service.ConfigStorage;
import com.rozen.service.ConfigStorage.AuthType;
import com.rozen.service.I18nManager;
import com.rozen.service.KeycloakService;
import com.rozen.service.KeycloakService.PageResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

import static com.rozen.ui.DialogUtil.*;
import static com.rozen.service.I18nManager.t;

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
    private int pageSize = 50;
    private int totalUsers = 0;
    private JLabel pageInfoLabel;
    private JButton prevPageBtn;
    private JButton nextPageBtn;
    private JButton firstPageBtn;
    private JButton lastPageBtn;

    // 连接配置
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
    private JComboBox<ConfigStorage.ProxyProtocol> proxyProtocolComboBox;
    private JTextField proxyHostField;
    private JSpinner proxyPortSpinner;

    private JCheckBox skipSslCheckBox;
    private JLabel statusLabel;

    // 面板容器
    private JPanel usernamePasswordPanel;
    private JPanel clientCredentialsPanel;
    private JPanel authCardPanel;

    // 自定义属性搜索条件
    private java.util.List<JTextField> attrNameFields = new java.util.ArrayList<>();
    private java.util.List<JTextField> attrValueFields = new java.util.ArrayList<>();
    private JPanel attributeConditionsPanel;
    private JSplitPane userPanelSplitPane; // 用户面板的分隔条，用于动态调整高度

    public KeycloakClientManagerUI(ConfigStorage.ConnectionConfig config) {
        this.currentConfig = config;
        this.configStorage = new ConfigStorage();

        setTitle(t(MessageConstants.App.TITLE) + " - " + config.getName());
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
        JMenu fileMenu = new JMenu(t(MessageConstants.Menu.FILE));
        JMenuItem exitItem = new JMenuItem(t(MessageConstants.Menu.FILE_EXIT));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // 语言菜单
        JMenu languageMenu = new JMenu(t(MessageConstants.Menu.LANGUAGE));
        for (I18nManager.Language lang : I18nManager.Language.values()) {
            JMenuItem langItem = new JMenuItem(lang.getDisplayName());
            langItem.addActionListener(e -> switchLanguage(lang));
            languageMenu.add(langItem);
        }

        // 帮助菜单
        JMenu helpMenu = new JMenu(t(MessageConstants.Menu.HELP));

        JMenuItem githubItem = new JMenuItem(t(MessageConstants.Menu.HELP_GITHUB));
        githubItem.addActionListener(e -> openGitHub());
        helpMenu.add(githubItem);

        helpMenu.addSeparator();

        JMenuItem aboutItem = new JMenuItem(t(MessageConstants.Menu.HELP_ABOUT));
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(languageMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 切换语言并自动重启程序
     */
    private void switchLanguage(I18nManager.Language language) {
        // 如果选择的语言与当前语言相同，不执行任何操作
        if (I18nManager.getInstance().getCurrentLanguage() == language) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            t(MessageConstants.Msg.RESTART_CONFIRM) + " " + language.getDisplayName() + "?",
            t(MessageConstants.Menu.LANGUAGE),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            restartApplication(language);
        }
    }

    /**
     * 重启应用程序并应用新的语言设置
     */
    private void restartApplication(I18nManager.Language language) {
        try {
            // 获取当前运行的 Java 命令
            String javaBin = System.getProperty("java.home") + "/bin/java";
            String jarPath = System.getProperty("java.class.path");

            // 构建重启命令，添加语言参数
            ProcessBuilder builder = new ProcessBuilder(
                javaBin,
                "-jar",
                jarPath,
                "--lang=" + language.getCode()
            );

            // 继承当前进程的环境变量和工作目录
            builder.inheritIO();
            builder.directory(new java.io.File(System.getProperty("user.dir")));

            // 启动新进程
            builder.start();

            // 关闭当前进程
            System.exit(0);

        } catch (Exception e) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Msg.RESTART_FAILED) + ": " + e.getMessage());
        }
    }

    private void openGitHub() {
        try {
            Desktop.getDesktop().browse(new URI(GITHUB_URL));
        } catch (Exception ex) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Msg.BROWSER_OPEN_FAILED) + ": " + ex.getMessage());
        }
    }

    private void showAbout() {
        String message = t(MessageConstants.About.DESCRIPTION) + "\n" +
                t(MessageConstants.App.VERSION) + ": " + VERSION + "\n" +
                "\n" +
                "GitHub: " + GITHUB_URL + "\n" +
                "\n" +
                t(MessageConstants.About.FEATURES) + "\n" +
                "- " + t(MessageConstants.About.FEATURE1) + "\n" +
                "- " + t(MessageConstants.About.FEATURE2) + "\n" +
                "- " + t(MessageConstants.About.FEATURE3) + "\n" +
                "- " + t(MessageConstants.About.FEATURE4) + "\n" +
                "- " + t(MessageConstants.About.AUTHOR) + ":rozenone@foxmail.com\n" +
                "\n" +
                t(MessageConstants.About.COPYRIGHT);

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this, textArea, t(MessageConstants.About.TITLE), JOptionPane.INFORMATION_MESSAGE);
    }

    private void initComponents() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建主标签页
        JTabbedPane mainTabbedPane = new JTabbedPane();

        // 客户端管理 Tab
        mainTabbedPane.addTab(t(MessageConstants.Tab.CLIENT), createClientPanel());

        // 用户管理 Tab
        mainTabbedPane.addTab(t(MessageConstants.Tab.USER), createUserPanel());

        // 连接配置 Tab
        mainTabbedPane.addTab(t(MessageConstants.Tab.CONFIG), createConnectionConfigPanel());

        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);

        // 状态栏
        statusLabel = new JLabel(t(MessageConstants.Status.READY));
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createConnectionConfigPanel() {
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
        basicPanel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.DialogConfig.BASIC)));
        GridBagConstraints gbcBasic = new GridBagConstraints();
        gbcBasic.insets = new Insets(5, 5, 5, 5);
        gbcBasic.fill = GridBagConstraints.HORIZONTAL;
        gbcBasic.anchor = GridBagConstraints.WEST;

        // 服务器地址
        gbcBasic.gridx = 0;
        gbcBasic.gridy = 0;
        gbcBasic.weightx = 0;
        basicPanel.add(new JLabel(t(MessageConstants.DialogConfig.SERVER_URL) + " *:"), gbcBasic);

        gbcBasic.gridx = 1;
        gbcBasic.weightx = 1.0;
        serverUrlField = new JTextField(30);
        basicPanel.add(serverUrlField, gbcBasic);

        // Realm
        gbcBasic.gridx = 0;
        gbcBasic.gridy = 1;
        gbcBasic.weightx = 0;
        basicPanel.add(new JLabel(t(MessageConstants.DialogConfig.REALM) + " *:"), gbcBasic);

        gbcBasic.gridx = 1;
        gbcBasic.weightx = 1.0;
        realmField = new JTextField(15);
        basicPanel.add(realmField, gbcBasic);

        gbc.gridx = 0;
        gbc.gridy = row++;
        contentPanel.add(basicPanel, gbc);

        // ========== 认证配置区域 ==========
        JPanel authPanel = new JPanel(new BorderLayout(5, 5));
        authPanel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.DialogConfig.AUTH)));

        // 登录方式选择
        JPanel authTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authTypePanel.add(new JLabel(t(MessageConstants.DialogConfig.AUTH_TYPE) + ":"));
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
        proxyPanel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.DialogConfig.PROXY)));
        GridBagConstraints gbcProxy = new GridBagConstraints();
        gbcProxy.insets = new Insets(5, 5, 5, 5);
        gbcProxy.fill = GridBagConstraints.HORIZONTAL;
        gbcProxy.anchor = GridBagConstraints.WEST;

        // 使用代理
        gbcProxy.gridx = 0;
        gbcProxy.gridy = 0;
        gbcProxy.weightx = 0;
        proxyPanel.add(new JLabel(t(MessageConstants.DialogConfig.USE_PROXY) + ":"), gbcProxy);

        gbcProxy.gridx = 1;
        gbcProxy.weightx = 1.0;
        useProxyCheckBox = new JCheckBox(t(MessageConstants.DialogConfig.ENABLE_PROXY));
        useProxyCheckBox.addItemListener(this::onUseProxyChanged);
        proxyPanel.add(useProxyCheckBox, gbcProxy);

        // 代理协议
        gbcProxy.gridx = 0;
        gbcProxy.gridy = 1;
        gbcProxy.weightx = 0;
        proxyPanel.add(new JLabel(t(MessageConstants.DialogConfig.PROXY_PROTOCOL) + ":"), gbcProxy);

        gbcProxy.gridx = 1;
        gbcProxy.weightx = 1.0;
        proxyProtocolComboBox = new JComboBox<>(ConfigStorage.ProxyProtocol.values());
        proxyProtocolComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ConfigStorage.ProxyProtocol) {
                    setText(((ConfigStorage.ProxyProtocol) value).getDisplayName());
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
        proxyPanel.add(new JLabel(t(MessageConstants.DialogConfig.PROXY_HOST) + ":"), gbcProxy);

        gbcProxy.gridx = 1;
        gbcProxy.weightx = 1.0;
        proxyHostField = new JTextField(20);
        proxyHostField.setEnabled(false);
        proxyPanel.add(proxyHostField, gbcProxy);

        // 代理端口
        gbcProxy.gridx = 0;
        gbcProxy.gridy = 3;
        gbcProxy.weightx = 0;
        proxyPanel.add(new JLabel(t(MessageConstants.DialogConfig.PROXY_PORT) + ":"), gbcProxy);

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
        sslPanel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.DialogConfig.SSL)));
        GridBagConstraints gbcSsl = new GridBagConstraints();
        gbcSsl.insets = new Insets(5, 5, 5, 5);
        gbcSsl.fill = GridBagConstraints.HORIZONTAL;
        gbcSsl.anchor = GridBagConstraints.WEST;

        // 跳过 SSL 验证
        gbcSsl.gridx = 0;
        gbcSsl.gridy = 0;
        gbcSsl.weightx = 0;
        sslPanel.add(new JLabel(t(MessageConstants.DialogConfig.SKIP_SSL) + ":"), gbcSsl);

        gbcSsl.gridx = 1;
        gbcSsl.weightx = 1.0;
        skipSslCheckBox = new JCheckBox(t(MessageConstants.DialogConfig.SKIP_SSL_HINT));
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

        // 按钮面板 - 使用普通按钮样式
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton testBtn = new JButton(t(MessageConstants.Button.TEST));
        testBtn.addActionListener(this::onTestConnection);
        buttonPanel.add(testBtn);

        JButton connectBtn = new JButton(t(MessageConstants.Button.CONNECT));
        connectBtn.addActionListener(this::onConnect);
        buttonPanel.add(connectBtn);

        JButton saveBtn = new JButton(t(MessageConstants.Button.SAVE_CURRENT_CONFIG));
        saveBtn.addActionListener(this::onSaveConfig);
        buttonPanel.add(saveBtn);

        JButton saveAsBtn = new JButton(t(MessageConstants.Button.SAVE_AS_NEW_CONFIG));
        saveAsBtn.addActionListener(this::onSaveAsNewConfig);
        buttonPanel.add(saveAsBtn);

        JButton switchBtn = new JButton(t(MessageConstants.Button.SWITCH_CONFIG));
        switchBtn.addActionListener(this::onSwitchConfig);
        buttonPanel.add(switchBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUsernamePasswordCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.DialogConfig.USERNAME_PASSWORD)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(t(MessageConstants.DialogConfig.USERNAME_LABEL) + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(t(MessageConstants.DialogConfig.PASSWORD_LABEL) + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        return panel;
    }

    private JPanel createClientCredentialsCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.DialogConfig.CLIENT_CREDENTIALS)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 客户端ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(t(MessageConstants.DialogConfig.CLIENT_ID_LABEL) + " *:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        clientIdField = new JTextField(20);
        panel.add(clientIdField, gbc);

        // 客户端密钥
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(t(MessageConstants.DialogConfig.CLIENT_SECRET_LABEL) + " *:"), gbc);

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

    private JPanel createClientPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 表格
        String[] columns = {
            t(MessageConstants.Client.ID), t(MessageConstants.Client.NAME), t(MessageConstants.Client.ENABLED),
            t(MessageConstants.Client.PROTOCOL), t(MessageConstants.Client.ROOT_URL)
        };
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

        JButton refreshBtn = new JButton(t(MessageConstants.Button.REFRESH_LIST));
        refreshBtn.addActionListener(this::onRefreshClients);
        buttonPanel.add(refreshBtn);

        JButton addBtn = new JButton(t(MessageConstants.Button.ADD_CLIENT));
        addBtn.addActionListener(this::onAddClient);
        buttonPanel.add(addBtn);

        JButton editBtn = new JButton(t(MessageConstants.Button.EDIT));
        editBtn.addActionListener(this::onEditClient);
        buttonPanel.add(editBtn);

        JButton deleteBtn = new JButton(t(MessageConstants.Button.DELETE));
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
        searchPanel.add(new JLabel(t(MessageConstants.Label.SEARCH_TYPE) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        searchTypeCombo = new JComboBox<>(new String[]{
            t(MessageConstants.Search.ALL), "ID", t(MessageConstants.User.USERNAME), t(MessageConstants.User.EMAIL), t(MessageConstants.Search.ATTRIBUTE)
        });
        searchPanel.add(searchTypeCombo, gbc);

        // 精确匹配选项
        gbc.gridx = 2;
        gbc.weightx = 0;
        exactMatchCheckBox = new JCheckBox(t(MessageConstants.Label.EXACT_MATCH));
        searchPanel.add(exactMatchCheckBox, gbc);

        // 每页数量
        gbc.gridx = 3;
        searchPanel.add(new JLabel(t(MessageConstants.Label.PER_PAGE) + ":"), gbc);

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

        // 搜索关键词（普通搜索）
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        searchPanel.add(new JLabel(t(MessageConstants.Label.KEYWORD) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        userSearchField = new JTextField(30);
        searchPanel.add(userSearchField, gbc);

        // 搜索按钮
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton searchBtn = new JButton(t(MessageConstants.Button.SEARCH));
        searchBtn.addActionListener(e -> searchUsers());
        searchPanel.add(searchBtn, gbc);

        // 清除按钮
        gbc.gridx = 3;
        gbc.gridwidth = 2;
        JButton clearBtn = new JButton(t(MessageConstants.Button.CLEAR));
        clearBtn.addActionListener(e -> {
            userSearchField.setText("");
            searchTypeCombo.setSelectedIndex(0);
            exactMatchCheckBox.setSelected(false);
            clearAttributeConditions();
            currentPage = 0;
            loadUsers();
        });
        searchPanel.add(clearBtn, gbc);

        // 自定义属性条件面板（默认隐藏）
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel attributePanel = createAttributeSearchPanel();
        searchPanel.add(attributePanel, gbc);

        // 监听搜索类型变化，显示/隐藏属性条件面板
        searchTypeCombo.addActionListener(e -> {
            boolean isCustomAttr = t(MessageConstants.Search.ATTRIBUTE).equals(searchTypeCombo.getSelectedItem());
            attributePanel.setVisible(isCustomAttr);
            userSearchField.setEnabled(!isCustomAttr);
            if (isCustomAttr) {
                userSearchField.setText("");
            }
            searchPanel.revalidate();
            searchPanel.repaint();

            // 自动调整 JSplitPane 分隔位置
            SwingUtilities.invokeLater(() -> {
                if (userPanelSplitPane != null) {
                    Component topComponent = userPanelSplitPane.getTopComponent();
                    if (topComponent != null) {
                        int preferredHeight = topComponent.getPreferredSize().height;
                        userPanelSplitPane.setDividerLocation(preferredHeight);
                    }
                }
            });
        });
        attributePanel.setVisible(false);

        // 用户表格
        String[] columns = {
            t(MessageConstants.User.ID), t(MessageConstants.User.USERNAME), t(MessageConstants.User.DISPLAY_NAME),
            t(MessageConstants.User.EMAIL), t(MessageConstants.User.FULL_NAME), t(MessageConstants.User.ENABLED),
            t(MessageConstants.User.EMAIL_VERIFIED)
        };
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

        JScrollPane tableScrollPane = new JScrollPane(userTable);

        // 使用 JSplitPane 实现搜索面板和用户列表之间的可拖拽调整
        // 将搜索面板放入一个可自适应高度的容器
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.add(searchPanel, BorderLayout.NORTH);

        userPanelSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchContainer, tableScrollPane);
        userPanelSplitPane.setDividerSize(5);
        userPanelSplitPane.setResizeWeight(0.0); // 上方不扩展，下方优先扩展
        userPanelSplitPane.setBorder(null);

        panel.add(userPanelSplitPane, BorderLayout.CENTER);

        // 在面板显示后设置分隔位置为搜索面板的首选高度
        SwingUtilities.invokeLater(() -> {
            int preferredHeight = searchPanel.getPreferredSize().height;
            userPanelSplitPane.setDividerLocation(preferredHeight);
        });

        // 分页控制面板
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        // 首页按钮
        firstPageBtn = new JButton("首页");
        firstPageBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage = 0;
                loadUsers();
            }
        });
        paginationPanel.add(firstPageBtn);

        prevPageBtn = new JButton("上一页");
        prevPageBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadUsers();
            }
        });
        paginationPanel.add(prevPageBtn);

        // 页码信息
        pageInfoLabel = new JLabel("Page 1 / 1");
        paginationPanel.add(pageInfoLabel);

        // 跳转到指定页
        paginationPanel.add(new JLabel(t(MessageConstants.Label.GOTO_PAGE) + ":"));
        JTextField gotoPageField = new JTextField(3);
        gotoPageField.setHorizontalAlignment(JTextField.CENTER);
        paginationPanel.add(gotoPageField);
        paginationPanel.add(new JLabel(t(MessageConstants.Label.PAGE)));

        JButton gotoPageBtn = new JButton(t(MessageConstants.Label.GO));
        gotoPageBtn.addActionListener(e -> {
            try {
                int targetPage = Integer.parseInt(gotoPageField.getText().trim()) - 1; // 转换为0-based
                int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
                if (targetPage < 0) targetPage = 0;
                if (targetPage >= totalPages) targetPage = totalPages - 1;
                if (targetPage != currentPage) {
                    currentPage = targetPage;
                    loadUsers();
                }
                gotoPageField.setText("");
            } catch (NumberFormatException ex) {
                showWarning(this, t(MessageConstants.Msg.WARNING), t(MessageConstants.Validation.INVALID_PAGE));
            }
        });
        paginationPanel.add(gotoPageBtn);

        nextPageBtn = new JButton(t(MessageConstants.Button.NEXT_PAGE));
        nextPageBtn.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadUsers();
            }
        });
        paginationPanel.add(nextPageBtn);

        // 末页按钮
        lastPageBtn = new JButton(t(MessageConstants.Button.LAST_PAGE));
        lastPageBtn.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);
            if (totalPages > 0 && currentPage < totalPages - 1) {
                currentPage = totalPages - 1;
                loadUsers();
            }
        });
        paginationPanel.add(lastPageBtn);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton(t(MessageConstants.Button.REFRESH_LIST));
        refreshBtn.addActionListener(e -> loadUsers());
        leftButtonPanel.add(refreshBtn);

        JButton addBtn = new JButton(t(MessageConstants.Button.ADD_USER));
        addBtn.addActionListener(this::onAddUser);
        leftButtonPanel.add(addBtn);

        JButton editBtn = new JButton(t(MessageConstants.Button.EDIT));
        editBtn.addActionListener(this::onEditUser);
        leftButtonPanel.add(editBtn);

        JButton deleteBtn = new JButton(t(MessageConstants.Button.DELETE));
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

        // 设置登录方式
        if (config.getAuthType() != null) {
            authTypeComboBox.setSelectedItem(config.getAuthType());
        }

        // 根据登录方式显示对应的凭据
        if (config.getAuthType() == AuthType.USERNAME_PASSWORD) {
            usernameField.setText(config.getUsername());
            passwordField.setText(config.getPassword());
        } else {
            // 客户端凭据方式
            clientIdField.setText(config.getClientId());
            clientSecretField.setText(config.getClientSecret());
        }

        // 设置代理配置
        useProxyCheckBox.setSelected(config.isUseProxy());
        if (config.getProxyProtocol() != null) {
            proxyProtocolComboBox.setSelectedItem(config.getProxyProtocol());
        }
        proxyHostField.setText(config.getProxyHost() != null ? config.getProxyHost() : "");
        proxyPortSpinner.setValue(config.getProxyPort());

        // 更新代理字段启用状态
        proxyProtocolComboBox.setEnabled(config.isUseProxy());
        proxyHostField.setEnabled(config.isUseProxy());
        proxyPortSpinner.setEnabled(config.isUseProxy());

        skipSslCheckBox.setSelected(config.isSkipSslVerify());
    }

    private void autoConnect() {
        if (currentConfig == null) {
            statusLabel.setText(t(MessageConstants.Status.CONFIG_INCOMPLETE));
            statusLabel.setForeground(Color.ORANGE);
            return;
        }

        // 验证配置完整性
        if (currentConfig.getServerUrl() == null || currentConfig.getServerUrl().isEmpty() ||
            currentConfig.getRealm() == null || currentConfig.getRealm().isEmpty()) {
            statusLabel.setText(t(MessageConstants.Status.CONFIG_INCOMPLETE));
            statusLabel.setForeground(Color.ORANGE);
            return;
        }

        // 根据登录方式验证凭据
        if (currentConfig.getAuthType() == AuthType.USERNAME_PASSWORD) {
            if (currentConfig.getUsername() == null || currentConfig.getUsername().isEmpty() ||
                currentConfig.getPassword() == null || currentConfig.getPassword().isEmpty()) {
                statusLabel.setText(t(MessageConstants.Status.USERNAME_PASSWORD_INCOMPLETE));
                statusLabel.setForeground(Color.ORANGE);
                return;
            }
        } else {
            if (currentConfig.getClientId() == null || currentConfig.getClientId().isEmpty() ||
                currentConfig.getClientSecret() == null || currentConfig.getClientSecret().isEmpty()) {
                statusLabel.setText(t(MessageConstants.Status.CLIENT_CREDENTIALS_INCOMPLETE));
                statusLabel.setForeground(Color.ORANGE);
                return;
            }
        }

        try {
            keycloakService = KeycloakService.fromConfig(currentConfig);
            statusLabel.setText(t(MessageConstants.Status.CONNECTED_TO) + ": " + currentConfig.getServerUrl());
            statusLabel.setForeground(Color.GREEN.darker());
            loadClients();
            loadUsers();
        } catch (Exception ex) {
            statusLabel.setText(t(MessageConstants.Status.AUTO_CONNECT_FAILED) + ": " + ex.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void onTestConnection(ActionEvent e) {
        // 创建临时配置对象用于测试
        ConfigStorage.ConnectionConfig testConfig = new ConfigStorage.ConnectionConfig();
        testConfig.setServerUrl(serverUrlField.getText().trim());
        testConfig.setRealm(realmField.getText().trim());
        testConfig.setAuthType((AuthType) authTypeComboBox.getSelectedItem());
        testConfig.setUsername(usernameField.getText().trim());
        testConfig.setPassword(new String(passwordField.getPassword()));
        testConfig.setClientId(clientIdField.getText().trim());
        testConfig.setClientSecret(new String(clientSecretField.getPassword()));
        testConfig.setUseProxy(useProxyCheckBox.isSelected());
        testConfig.setProxyProtocol((ConfigStorage.ProxyProtocol) proxyProtocolComboBox.getSelectedItem());
        testConfig.setProxyHost(proxyHostField.getText().trim());
        testConfig.setProxyPort((Integer) proxyPortSpinner.getValue());
        testConfig.setSkipSslVerify(skipSslCheckBox.isSelected());

        // 验证必填项
        if (testConfig.getServerUrl().isEmpty() || testConfig.getRealm().isEmpty()) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.SERVER_URL_AND_REALM_REQUIRED));
            return;
        }

        // 根据登录方式验证凭据
        if (testConfig.getAuthType() == AuthType.USERNAME_PASSWORD) {
            if (testConfig.getUsername().isEmpty() || testConfig.getPassword().isEmpty()) {
                showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.USERNAME_AND_PASSWORD_REQUIRED));
                return;
            }
        } else {
            if (testConfig.getClientId().isEmpty() || testConfig.getClientSecret().isEmpty()) {
                showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.CLIENT_ID_AND_SECRET_REQUIRED));
                return;
            }
        }

        // 验证代理配置
        if (testConfig.isUseProxy() && testConfig.getProxyHost().isEmpty()) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.PROXY_HOST_REQUIRED));
            return;
        }

        // 显示测试中的提示
        statusLabel.setText(t(MessageConstants.Status.TESTING_CONNECTION));
        statusLabel.setForeground(Color.BLUE);

        // 在后台线程中执行连接测试
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String resultMessage;
            private boolean isSuccess;

            @Override
            protected Void doInBackground() {
                try (KeycloakService testService = KeycloakService.fromConfig(testConfig)) {
                    // 尝试获取服务器信息来验证连接
                    testService.getClients();
                    resultMessage = t(MessageConstants.Msg.CONNECTION_TEST_SUCCESS) + "\n\n" +
                            t(MessageConstants.DialogConfig.SERVER_URL) + ": " + testConfig.getServerUrl() + "\n" +
                            t(MessageConstants.DialogConfig.REALM) + ": " + testConfig.getRealm() + "\n" +
                            t(MessageConstants.DialogConfig.AUTH_TYPE) + ": " + testConfig.getAuthType().getDisplayName();
                    if (testConfig.isUseProxy()) {
                        resultMessage += "\n" + t(MessageConstants.DialogConfig.PROXY) + ": " + testConfig.getProxyProtocol().getDisplayName() +
                                "://" + testConfig.getProxyHost() + ":" + testConfig.getProxyPort();
                    }
                    isSuccess = true;
                } catch (Exception ex) {
                    resultMessage = t(MessageConstants.Msg.CONNECTION_TEST_FAILED) + "\n\n" + t(MessageConstants.Msg.ERROR_INFO) + ": " + ex.getMessage();
                    isSuccess = false;
                }
                return null;
            }

            @Override
            protected void done() {
                if (isSuccess) {
                    statusLabel.setText(t(MessageConstants.Status.CONNECTION_TEST_SUCCESS));
                    statusLabel.setForeground(Color.GREEN.darker());
                    showInfo(KeycloakClientManagerUI.this, t(MessageConstants.Dialog.CONNECTION_TEST_RESULT), resultMessage);
                } else {
                    statusLabel.setText(t(MessageConstants.Status.CONNECTION_TEST_FAILED));
                    statusLabel.setForeground(Color.RED);
                    showError(KeycloakClientManagerUI.this, t(MessageConstants.Dialog.CONNECTION_TEST_RESULT), resultMessage);
                }
            }
        };

        worker.execute();
    }

    private void onConnect(ActionEvent e) {
        if (currentConfig == null) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Msg.NO_CONFIG_TO_CONNECT));
            return;
        }

        // 从界面更新当前配置
        currentConfig.setServerUrl(serverUrlField.getText().trim());
        currentConfig.setRealm(realmField.getText().trim());
        currentConfig.setAuthType((AuthType) authTypeComboBox.getSelectedItem());
        currentConfig.setUsername(usernameField.getText().trim());
        currentConfig.setPassword(new String(passwordField.getPassword()));
        currentConfig.setClientId(clientIdField.getText().trim());
        currentConfig.setClientSecret(new String(clientSecretField.getPassword()));
        currentConfig.setUseProxy(useProxyCheckBox.isSelected());
        currentConfig.setProxyProtocol((ConfigStorage.ProxyProtocol) proxyProtocolComboBox.getSelectedItem());
        currentConfig.setProxyHost(proxyHostField.getText().trim());
        currentConfig.setProxyPort((Integer) proxyPortSpinner.getValue());
        currentConfig.setSkipSslVerify(skipSslCheckBox.isSelected());

        // 验证必填项
        if (currentConfig.getServerUrl().isEmpty() || currentConfig.getRealm().isEmpty()) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.SERVER_URL_AND_REALM_REQUIRED));
            return;
        }

        // 根据登录方式验证凭据
        if (currentConfig.getAuthType() == AuthType.USERNAME_PASSWORD) {
            if (currentConfig.getUsername().isEmpty() || currentConfig.getPassword().isEmpty()) {
                showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.USERNAME_AND_PASSWORD_REQUIRED));
                return;
            }
        } else {
            if (currentConfig.getClientId().isEmpty() || currentConfig.getClientSecret().isEmpty()) {
                showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.CLIENT_ID_AND_SECRET_REQUIRED));
                return;
            }
        }

        // 验证代理配置
        if (currentConfig.isUseProxy() && currentConfig.getProxyHost().isEmpty()) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.PROXY_HOST_REQUIRED));
            return;
        }

        try {
            keycloakService = KeycloakService.fromConfig(currentConfig);
            // 验证连接是否真正有效
            keycloakService.testConnection();
            statusLabel.setText(t(MessageConstants.Status.CONNECTED_TO) + ": " + currentConfig.getServerUrl());
            statusLabel.setForeground(Color.GREEN.darker());
            loadClients();
            loadUsers();
            showInfo(this, t(MessageConstants.Msg.CONNECTION_SUCCESS), t(MessageConstants.Msg.CONNECTED_TO_SERVER) + ":\n" + currentConfig.getServerUrl());
        } catch (Exception ex) {
            keycloakService = null;
            showError(this, t(MessageConstants.Msg.CONNECTION_FAILED), ex.getMessage());
            statusLabel.setText(t(MessageConstants.Status.CONNECTION_TEST_FAILED));
            statusLabel.setForeground(Color.RED);
        }
    }

    private void onSaveConfig(ActionEvent e) {
        if (currentConfig == null) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Msg.NO_CONFIG_TO_SAVE));
            return;
        }

        currentConfig.setServerUrl(serverUrlField.getText().trim());
        currentConfig.setRealm(realmField.getText().trim());
        currentConfig.setAuthType((AuthType) authTypeComboBox.getSelectedItem());
        currentConfig.setUsername(usernameField.getText().trim());
        currentConfig.setPassword(new String(passwordField.getPassword()));
        currentConfig.setClientId(clientIdField.getText().trim());
        currentConfig.setClientSecret(new String(clientSecretField.getPassword()));
        currentConfig.setUseProxy(useProxyCheckBox.isSelected());
        currentConfig.setProxyProtocol((ConfigStorage.ProxyProtocol) proxyProtocolComboBox.getSelectedItem());
        currentConfig.setProxyHost(proxyHostField.getText().trim());
        currentConfig.setProxyPort((Integer) proxyPortSpinner.getValue());
        currentConfig.setSkipSslVerify(skipSslCheckBox.isSelected());

        try {
            configStorage.saveConfig(currentConfig);
            showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.CONFIG_SAVED));
        } catch (Exception ex) {
            showError(this, t(MessageConstants.Msg.SAVE_FAILED), ex.getMessage());
        }
    }

    private void onSaveAsNewConfig(ActionEvent e) {
        String name = JOptionPane.showInputDialog(this, t(MessageConstants.Msg.ENTER_NEW_CONFIG_NAME) + ":", t(MessageConstants.Dialog.SAVE_NEW_CONFIG), JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        ConfigStorage.ConnectionConfig newConfig = new ConfigStorage.ConnectionConfig();
        newConfig.setName(name.trim());
        newConfig.setServerUrl(serverUrlField.getText().trim());
        newConfig.setRealm(realmField.getText().trim());
        newConfig.setAuthType((AuthType) authTypeComboBox.getSelectedItem());
        newConfig.setUsername(usernameField.getText().trim());
        newConfig.setPassword(new String(passwordField.getPassword()));
        newConfig.setClientId(clientIdField.getText().trim());
        newConfig.setClientSecret(new String(clientSecretField.getPassword()));
        newConfig.setUseProxy(useProxyCheckBox.isSelected());
        newConfig.setProxyProtocol((ConfigStorage.ProxyProtocol) proxyProtocolComboBox.getSelectedItem());
        newConfig.setProxyHost(proxyHostField.getText().trim());
        newConfig.setProxyPort((Integer) proxyPortSpinner.getValue());
        newConfig.setSkipSslVerify(skipSslCheckBox.isSelected());

        try {
            configStorage.saveConfig(newConfig);
            currentConfig = newConfig;
            setTitle(t(MessageConstants.App.TITLE) + " - " + newConfig.getName());
            showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.NEW_CONFIG_SAVED));
        } catch (Exception ex) {
            showError(this, t(MessageConstants.Msg.SAVE_FAILED), ex.getMessage());
        }
    }

    private void onSwitchConfig(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this,
            t(MessageConstants.Msg.SWITCH_CONFIG_CONFIRM),
            t(MessageConstants.Dialog.CONFIRM_SWITCH),
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
            showWarning(this, t(MessageConstants.Msg.WARNING), t(MessageConstants.Msg.CONNECT_FIRST));
            return;
        }

        clientTableModel.setRowCount(0);
        try {
            List<ClientInfo> clients = keycloakService.getClients();
            for (ClientInfo client : clients) {
                clientTableModel.addRow(new Object[]{
                    client.getClientId(),
                    client.getName(),
                    client.isEnabled() ? t(MessageConstants.Status.ENABLED) : t(MessageConstants.Status.DISABLED),
                    client.getProtocol(),
                    client.getRootUrl()
                });
            }
            statusLabel.setText(t(MessageConstants.Msg.CLIENT_COUNT, clients.size()));
        } catch (Exception ex) {
            showError(this, t(MessageConstants.Msg.LOAD_CLIENT_FAILED), ex.getMessage());
        }
    }

    private void onRefreshClients(ActionEvent e) {
        loadClients();
    }

    private void onAddClient(ActionEvent e) {
        if (keycloakService == null) {
            showWarning(this, t(MessageConstants.Msg.WARNING), t(MessageConstants.Msg.CONNECT_FIRST));
            return;
        }

        ClientDialog dialog = new ClientDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.createClient(dialog.getClientInfo());
                loadClients();
                showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.CLIENT_CREATE_SUCCESS));
            } catch (Exception ex) {
                showError(this, t(MessageConstants.Msg.CREATE_FAILED), ex.getMessage());
            }
        }
    }

    private void onEditClient(ActionEvent e) {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, t(MessageConstants.Msg.WARNING), t(MessageConstants.Msg.SELECT_CLIENT_FIRST));
            return;
        }

        String clientId = (String) clientTableModel.getValueAt(selectedRow, 0);
        ClientInfo clientInfo = keycloakService.getClientByClientId(clientId);
        
        if (clientInfo == null) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Msg.GET_CLIENT_FAILED));
            return;
        }

        ClientDialog dialog = new ClientDialog(this, clientInfo);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.updateClient(clientId, dialog.getClientInfo());
                loadClients();
                showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.CLIENT_UPDATE_SUCCESS));
            } catch (Exception ex) {
                showError(this, t(MessageConstants.Msg.UPDATE_FAILED), ex.getMessage());
            }
        }
    }

    private void onDeleteClient(ActionEvent e) {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.SELECT_CLIENT_FIRST));
            return;
        }

        String clientId = (String) clientTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            t(MessageConstants.Msg.CONFIRM_DELETE_CLIENT) + " '" + clientId + "'?",
            t(MessageConstants.Msg.CONFIRM_DELETE),
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                keycloakService.deleteClient(clientId);
                loadClients();
                showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.DELETE_SUCCESS));
            } catch (Exception ex) {
                showError(this, t(MessageConstants.Msg.DELETE_FAILED), ex.getMessage());
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
            showError(this, t(MessageConstants.Msg.LOAD_USER_FAILED), ex.getMessage());
        }
    }

    private void searchUsers() {
        if (keycloakService == null) {
            showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.CONNECT_FIRST));
            return;
        }

        String searchType = (String) searchTypeCombo.getSelectedItem();
        boolean exactMatch = exactMatchCheckBox.isSelected();

        try {
            PageResult<UserInfo> result;

            if (t(MessageConstants.Search.ATTRIBUTE).equals(searchType)) {
                // 多属性条件搜索（AND 逻辑）
                if (attrNameFields.isEmpty()) {
                    showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.ADD_ATTR_CONDITION));
                    return;
                }

                // 收集所有属性条件
                java.util.List<String> attrNames = new java.util.ArrayList<>();
                java.util.List<String> attrValues = new java.util.ArrayList<>();

                for (int i = 0; i < attrNameFields.size(); i++) {
                    String name = attrNameFields.get(i).getText().trim();
                    String value = attrValueFields.get(i).getText().trim();

                    if (!name.isEmpty()) {
                        attrNames.add(name);
                        attrValues.add(value);
                    }
                }

                if (attrNames.isEmpty()) {
                    showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.ATTR_NAME_REQUIRED));
                    return;
                }

                result = keycloakService.searchUsersByMultipleAttributes(attrNames, attrValues, currentPage, pageSize);
            } else {
                String search = userSearchField.getText().trim();
                if (search.isEmpty()) {
                    currentPage = 0;
                    loadUsers();
                    return;
                }
                String type = t(MessageConstants.Search.ALL).equals(searchType) ? MessageConstants.SearchType.ALL :
                              "ID".equals(searchType) ? MessageConstants.SearchType.ID :
                              t(MessageConstants.User.USERNAME).equals(searchType) ? MessageConstants.SearchType.USERNAME : MessageConstants.SearchType.EMAIL;
                result = keycloakService.searchUsers(search, type, exactMatch, currentPage, pageSize);
            }

            displayUsers(result);
        } catch (Exception ex) {
            showError(this, t(MessageConstants.Msg.SEARCH_USER_FAILED), ex.getMessage());
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
                user.isEnabled() ? t(MessageConstants.Status.ENABLED) : t(MessageConstants.Status.DISABLED),
                user.isEmailVerified() ? t(MessageConstants.Status.YES) : t(MessageConstants.Status.NO)
            });
        }

        // 更新分页信息
        int totalPages = result.getTotalPages();
        int currentPageNum = result.getPage() + 1;
        pageInfoLabel.setText(t(MessageConstants.User.PAGE_INFO, currentPageNum, Math.max(1, totalPages))
            + " (" + t(MessageConstants.User.TOTAL) + " " + totalUsers + ")");

        // 更新按钮状态
        if (firstPageBtn != null) firstPageBtn.setEnabled(result.getPage() > 0);
        prevPageBtn.setEnabled(result.getPage() > 0);
        nextPageBtn.setEnabled(result.getPage() < totalPages - 1);
        if (lastPageBtn != null) lastPageBtn.setEnabled(result.getPage() < totalPages - 1);

        statusLabel.setText(t(MessageConstants.Msg.USER_COUNT, totalUsers) + ", " + t(MessageConstants.User.PAGE_SIZE, users.size()));
    }

    private void onAddUser(ActionEvent e) {
        if (keycloakService == null) {
            showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.CONNECT_FIRST));
            return;
        }

        UserDialog dialog = new UserDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.createUser(dialog.getUserInfo());
                loadUsers();
                showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.USER_CREATE_SUCCESS));
            } catch (Exception ex) {
                showError(this, t(MessageConstants.Msg.CREATE_FAILED), ex.getMessage());
            }
        }
    }

    private void onEditUser(ActionEvent e) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.SELECT_USER_FIRST));
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        UserInfo userInfo = keycloakService.getUserById(userId);
        
        if (userInfo == null) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Msg.GET_USER_FAILED));
            return;
        }

        UserDialog dialog = new UserDialog(this, userInfo);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                keycloakService.updateUser(userId, dialog.getUserInfo());
                loadUsers();
                showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.USER_UPDATE_SUCCESS));
            } catch (Exception ex) {
                showError(this, t(MessageConstants.Msg.UPDATE_FAILED), ex.getMessage());
            }
        }
    }

    private void onDeleteUser(ActionEvent e) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.SELECT_USER_FIRST));
            return;
        }

        String userId = (String) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            t(MessageConstants.Msg.CONFIRM_DELETE_USER) + " '" + username + "'?",
            t(MessageConstants.Msg.CONFIRM_DELETE),
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                keycloakService.deleteUser(userId);
                loadUsers();
                showInfo(this, t(MessageConstants.Msg.SUCCESS), t(MessageConstants.Msg.DELETE_SUCCESS));
            } catch (Exception ex) {
                showError(this, t(MessageConstants.Msg.DELETE_FAILED), ex.getMessage());
            }
        }
    }

    private void showUserDetail(int row) {
        String userId = (String) userTableModel.getValueAt(row, 0);
        UserInfo userInfo = keycloakService.getUserById(userId);

        if (userInfo != null) {
            UserDetailDialog dialog = new UserDetailDialog(this, userInfo, currentConfig.getServerUrl(), currentConfig.getRealm());
            dialog.setVisible(true);
        }
    }

    private JPanel createAttributeSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.Label.ATTR_CONDITIONS)));

        // 条件列表面板 - 不使用滚动条，让面板自然扩展
        attributeConditionsPanel = new JPanel(new GridBagLayout());
        attributeConditionsPanel.setBorder(null);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton(t(MessageConstants.Label.ADD_CONDITION));
        addBtn.addActionListener(e -> addAttributeConditionRow());
        buttonPanel.add(addBtn);

        JButton clearBtn = new JButton(t(MessageConstants.Label.CLEAR_CONDITIONS));
        clearBtn.addActionListener(e -> clearAttributeConditions());
        buttonPanel.add(clearBtn);

        // 将条件面板和按钮面板垂直排列，不使用JSplitPane，让高度自然扩展
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(attributeConditionsPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);

        // 默认添加一行
        addAttributeConditionRow();

        return panel;
    }

    private void addAttributeConditionRow() {
        if (attrNameFields.size() >= 5) {
            showWarning(this, t(MessageConstants.Msg.HINT), t(MessageConstants.Msg.MAX_ATTR_CONDITIONS));
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = attrNameFields.size();

        // 属性名输入
        gbc.gridx = 0;
        gbc.weightx = 0;
        attributeConditionsPanel.add(new JLabel(t(MessageConstants.Label.ATTR_NAME) + (row + 1) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        JTextField nameField = new JTextField(12);
        attributeConditionsPanel.add(nameField, gbc);
        attrNameFields.add(nameField);

        // 属性值输入
        gbc.gridx = 2;
        gbc.weightx = 0;
        attributeConditionsPanel.add(new JLabel(t(MessageConstants.Label.ATTR_VALUE) + ":"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.6;
        JTextField valueField = new JTextField(15);
        attributeConditionsPanel.add(valueField, gbc);
        attrValueFields.add(valueField);

        // 删除按钮
        gbc.gridx = 4;
        gbc.weightx = 0;
        JButton deleteBtn = new JButton(t(MessageConstants.Button.DELETE));
        final int index = row;
        deleteBtn.addActionListener(e -> removeAttributeConditionRow(index));
        attributeConditionsPanel.add(deleteBtn, gbc);

        // 刷新父容器布局，让高度自然调整
        attributeConditionsPanel.revalidate();
        attributeConditionsPanel.repaint();

        // 通知上层 JSplitPane 重新计算分隔位置
        SwingUtilities.invokeLater(() -> {
            if (userPanelSplitPane != null) {
                Component topComponent = userPanelSplitPane.getTopComponent();
                if (topComponent != null) {
                    int preferredHeight = topComponent.getPreferredSize().height;
                    userPanelSplitPane.setDividerLocation(preferredHeight);
                }
            }
        });
    }

    private void removeAttributeConditionRow(int index) {
        if (index < 0 || index >= attrNameFields.size()) return;

        attrNameFields.remove(index);
        attrValueFields.remove(index);

        // 重新构建面板
        attributeConditionsPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < attrNameFields.size(); i++) {
            // 属性名输入
            gbc.gridx = 0;
            gbc.weightx = 0;
            attributeConditionsPanel.add(new JLabel(t(MessageConstants.Label.ATTR_NAME) + (i + 1) + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.4;
            attributeConditionsPanel.add(attrNameFields.get(i), gbc);

            // 属性值输入
            gbc.gridx = 2;
            gbc.weightx = 0;
            attributeConditionsPanel.add(new JLabel(t(MessageConstants.Label.ATTR_VALUE) + ":"), gbc);

            gbc.gridx = 3;
            gbc.weightx = 0.6;
            attributeConditionsPanel.add(attrValueFields.get(i), gbc);

            // 删除按钮
            gbc.gridx = 4;
            gbc.weightx = 0;
            JButton deleteBtn = new JButton(t(MessageConstants.Button.DELETE));
            final int idx = i;
            deleteBtn.addActionListener(e -> removeAttributeConditionRow(idx));
            attributeConditionsPanel.add(deleteBtn, gbc);
        }

        // 刷新父容器布局，让高度自然调整
        attributeConditionsPanel.revalidate();
        attributeConditionsPanel.repaint();

        // 通知上层 JSplitPane 重新计算分隔位置
        SwingUtilities.invokeLater(() -> {
            if (userPanelSplitPane != null) {
                Component topComponent = userPanelSplitPane.getTopComponent();
                if (topComponent != null) {
                    int preferredHeight = topComponent.getPreferredSize().height;
                    userPanelSplitPane.setDividerLocation(preferredHeight);
                }
            }
        });
    }

    private void clearAttributeConditions() {
        attrNameFields.clear();
        attrValueFields.clear();
        attributeConditionsPanel.removeAll();
        addAttributeConditionRow();
        // 高度会在 addAttributeConditionRow 中自动调整
    }
}
