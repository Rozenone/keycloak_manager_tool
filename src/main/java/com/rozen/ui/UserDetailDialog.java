package com.rozen.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserDetailDialog extends JDialog {

    private JRadioButton tableModeRadio;
    private JRadioButton textModeRadio;
    private JPanel contentPanel;
    private UserInfo userInfo;

    public UserDetailDialog(JFrame parent, UserInfo userInfo) {
        super(parent, "用户详情 - " + userInfo.getUsername(), true);
        this.userInfo = userInfo;

        setSize(650, 550);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 模式选择面板
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.setBorder(BorderFactory.createTitledBorder("显示模式"));
        
        ButtonGroup modeGroup = new ButtonGroup();
        tableModeRadio = new JRadioButton("表格模式", true);
        textModeRadio = new JRadioButton("文本模式(可复制)");
        modeGroup.add(tableModeRadio);
        modeGroup.add(textModeRadio);
        
        modePanel.add(tableModeRadio);
        modePanel.add(textModeRadio);
        
        // 添加切换监听
        tableModeRadio.addActionListener(e -> updateContent());
        textModeRadio.addActionListener(e -> updateContent());
        
        panel.add(modePanel, BorderLayout.NORTH);

        // 内容面板
        contentPanel = new JPanel(new BorderLayout());
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // 初始显示
        updateContent();

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }
    
    private void updateContent() {
        contentPanel.removeAll();
        
        // 创建标签页
        JTabbedPane tabbedPane = new JTabbedPane();

        // 基本信息标签页
        tabbedPane.addTab("基本信息", createBasicInfoPanel(userInfo));

        // 所有属性标签页
        int allCount = userInfo.getAttributes() != null ? userInfo.getAttributes().size() : 0;
        tabbedPane.addTab("所有属性 (" + allCount + ")", 
            createAttributesPanel(userInfo.getAttributes(), "该用户没有自定义属性"));

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createBasicInfoPanel(UserInfo userInfo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (textModeRadio.isSelected()) {
            // 文本模式 - 可复制
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            StringBuilder sb = new StringBuilder();
            sb.append("用户ID:       ").append(getStringValue(userInfo.getId())).append("\n\n");
            sb.append("用户名:       ").append(getStringValue(userInfo.getUsername())).append("\n\n");
            sb.append("邮箱:         ").append(getStringValue(userInfo.getEmail())).append("\n\n");
            sb.append("姓:           ").append(getStringValue(userInfo.getFirstName())).append("\n\n");
            sb.append("名:           ").append(getStringValue(userInfo.getLastName())).append("\n\n");
            sb.append("全名:         ").append(userInfo.getFullName()).append("\n\n");
            sb.append("启用状态:     ").append(userInfo.isEnabled() ? "启用" : "禁用").append("\n\n");
            sb.append("邮箱已验证:   ").append(userInfo.isEmailVerified() ? "是" : "否").append("\n\n");
            sb.append("创建时间:     ").append(formatTimestamp(userInfo.getCreatedTimestamp())).append("\n\n");
            sb.append("必需操作:     ").append(getRequiredActions(userInfo.getRequiredActions()));
            
            textArea.setText(sb.toString());
            textArea.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            panel.add(scrollPane, BorderLayout.CENTER);
        } else {
            // 表格模式
            String[][] data = {
                {"用户ID", getStringValue(userInfo.getId())},
                {"用户名", getStringValue(userInfo.getUsername())},
                {"邮箱", getStringValue(userInfo.getEmail())},
                {"姓", getStringValue(userInfo.getFirstName())},
                {"名", getStringValue(userInfo.getLastName())},
                {"全名", userInfo.getFullName()},
                {"启用状态", userInfo.isEnabled() ? "启用" : "禁用"},
                {"邮箱已验证", userInfo.isEmailVerified() ? "是" : "否"},
                {"创建时间", formatTimestamp(userInfo.getCreatedTimestamp())},
                {"必需操作", getRequiredActions(userInfo.getRequiredActions())}
            };

            String[] columns = {"属性", "值"};
            JTable table = new JTable(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table.setRowHeight(28);
            table.getColumnModel().getColumn(0).setPreferredWidth(150);
            table.getColumnModel().getColumn(1).setPreferredWidth(400);
            table.setFont(new Font(table.getFont().getName(), Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createAttributesPanel(Map<String, List<String>> attributes, String emptyMessage) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (attributes == null || attributes.isEmpty()) {
            JTextArea emptyText = new JTextArea(emptyMessage);
            emptyText.setEditable(false);
            emptyText.setLineWrap(true);
            emptyText.setWrapStyleWord(true);
            emptyText.setBackground(panel.getBackground());
            emptyText.setForeground(Color.GRAY);
            emptyText.setFont(new Font(emptyText.getFont().getName(), Font.ITALIC, 14));
            emptyText.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.add(emptyText, BorderLayout.CENTER);
            return panel;
        }

        if (textModeRadio.isSelected()) {
            // 文本模式 - 可复制
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                String value = entry.getValue() != null ? String.join(", ", entry.getValue()) : "";
                sb.append(entry.getKey()).append(": ").append(value).append("\n\n");
            }
            
            textArea.setText(sb.toString());
            textArea.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            panel.add(scrollPane, BorderLayout.CENTER);
        } else {
            // 表格模式
            String[] columns = {"属性名", "属性值"};
            String[][] data = new String[attributes.size()][2];
            
            int i = 0;
            for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                data[i][0] = entry.getKey();
                data[i][1] = entry.getValue() != null ? String.join(", ", entry.getValue()) : "";
                i++;
            }

            JTable table = new JTable(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table.setRowHeight(25);
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setPreferredWidth(350);

            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
        }

        return panel;
    }

    private String getStringValue(String value) {
        return value != null && !value.isEmpty() ? value : "(未设置)";
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "(未设置)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }

    private String getRequiredActions(List<String> actions) {
        if (actions == null || actions.isEmpty()) {
            return "(无)";
        }
        return String.join(", ", actions);
    }
}
