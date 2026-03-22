package com.rozen.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientDetailDialog extends JDialog {

    private JRadioButton tableModeRadio;
    private JRadioButton textModeRadio;
    private JPanel contentPanel;
    private ClientInfo clientInfo;

    public ClientDetailDialog(JFrame parent, ClientInfo clientInfo) {
        super(parent, "客户端详情 - " + clientInfo.getClientId(), true);
        this.clientInfo = clientInfo;

        setSize(600, 500);
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

        if (textModeRadio.isSelected()) {
            // 文本模式 - 可复制
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            StringBuilder sb = new StringBuilder();
            sb.append("客户端ID:     ").append(getStringValue(clientInfo.getClientId())).append("\n\n");
            sb.append("名称:         ").append(getStringValue(clientInfo.getName())).append("\n\n");
            sb.append("描述:         ").append(getStringValue(clientInfo.getDescription())).append("\n\n");
            sb.append("启用状态:     ").append(clientInfo.isEnabled() ? "启用" : "禁用").append("\n\n");
            sb.append("协议:         ").append(getStringValue(clientInfo.getProtocol())).append("\n\n");
            sb.append("根URL:        ").append(getStringValue(clientInfo.getRootUrl())).append("\n\n");
            sb.append("重定向URI:\n").append(getStringValue(clientInfo.getRedirectUris())).append("\n\n");
            sb.append("Web Origins:\n").append(getStringValue(clientInfo.getWebOrigins()));
            
            textArea.setText(sb.toString());
            textArea.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            contentPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            // 表格模式
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
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
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            contentPanel.add(tablePanel, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String getStringValue(String value) {
        return value != null && !value.isEmpty() ? value : "(未设置)";
    }
}
