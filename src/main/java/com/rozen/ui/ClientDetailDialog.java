package com.rozen.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientDetailDialog extends JDialog {

    public ClientDetailDialog(JFrame parent, ClientInfo clientInfo) {
        super(parent, "客户端详情 - " + clientInfo.getClientId(), true);

        setSize(600, 500);
        setLocationRelativeTo(parent);
        initComponents(clientInfo);
    }

    private void initComponents(ClientInfo clientInfo) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建属性表格
        String[][] data = {
            {"客户端ID", getStringValue(clientInfo.getClientId())},
            {"名称", getStringValue(clientInfo.getName())},
            {"描述", getStringValue(clientInfo.getDescription())},
            {"启用状态", clientInfo.isEnabled() ? "启用" : "禁用"},
            {"协议", getStringValue(clientInfo.getProtocol())},
            {"根URL", getStringValue(clientInfo.getRootUrl())},
            {"重定向URI", getStringValue(clientInfo.getRedirectUris())},
            {"Web Origins", getStringValue(clientInfo.getWebOrigins())}
        };

        String[] columns = {"属性", "值"};
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private String getStringValue(String value) {
        return value != null && !value.isEmpty() ? value : "(未设置)";
    }
}
