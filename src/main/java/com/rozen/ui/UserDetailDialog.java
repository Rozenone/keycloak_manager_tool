package com.rozen.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserDetailDialog extends JDialog {

    public UserDetailDialog(JFrame parent, UserInfo userInfo) {
        super(parent, "用户详情 - " + userInfo.getUsername(), true);

        setSize(650, 550);
        setLocationRelativeTo(parent);
        initComponents(userInfo);
    }

    private void initComponents(UserInfo userInfo) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建标签页
        JTabbedPane tabbedPane = new JTabbedPane();

        // 基本信息标签页
        tabbedPane.addTab("基本信息", createBasicInfoPanel(userInfo));

        // 属性标签页
        tabbedPane.addTab("自定义属性 (" + (userInfo.getAttributes() != null ? userInfo.getAttributes().size() : 0) + ")", 
            createAttributesPanel(userInfo));

        panel.add(tabbedPane, BorderLayout.CENTER);

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private JPanel createBasicInfoPanel(UserInfo userInfo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建属性表格
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

        return panel;
    }

    private JPanel createAttributesPanel(UserInfo userInfo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        if (userInfo.getAttributes() == null || userInfo.getAttributes().isEmpty()) {
            JLabel emptyLabel = new JLabel("该用户没有自定义属性", SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font(emptyLabel.getFont().getName(), Font.ITALIC, 14));
            panel.add(emptyLabel, BorderLayout.CENTER);
            return panel;
        }

        // 创建属性表格
        String[] columns = {"属性名", "属性值"};
        String[][] data = new String[userInfo.getAttributes().size()][2];
        
        int i = 0;
        for (Map.Entry<String, List<String>> entry : userInfo.getAttributes().entrySet()) {
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
