package com.rozen.ui;

import com.rozen.constant.MessageConstants;
import com.rozen.model.ClientInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.rozen.service.I18nManager.t;

public class ClientDetailDialog extends JDialog {

    private JRadioButton tableModeRadio;
    private JRadioButton textModeRadio;
    private JPanel contentPanel;
    private ClientInfo clientInfo;

    public ClientDetailDialog(JFrame parent, ClientInfo clientInfo) {
        super(parent, t(MessageConstants.Client.DETAIL) + " - " + clientInfo.getClientId(), true);
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
        modePanel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.Dialog.DISPLAY_MODE)));

        ButtonGroup modeGroup = new ButtonGroup();
        tableModeRadio = new JRadioButton(t(MessageConstants.Dialog.TABLE_MODE), true);
        textModeRadio = new JRadioButton(t(MessageConstants.Dialog.TEXT_MODE));
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
        JButton closeBtn = new JButton(t(MessageConstants.Button.CLOSE));
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
            sb.append(t(MessageConstants.Client.ID)).append(":     ").append(getStringValue(clientInfo.getClientId())).append("\n\n");
            sb.append(t(MessageConstants.Client.NAME)).append(":         ").append(getStringValue(clientInfo.getName())).append("\n\n");
            sb.append(t(MessageConstants.Client.DESCRIPTION)).append(":         ").append(getStringValue(clientInfo.getDescription())).append("\n\n");
            sb.append(t(MessageConstants.Client.ENABLED)).append(":     ").append(clientInfo.isEnabled() ? t(MessageConstants.Status.ENABLED) : t(MessageConstants.Status.DISABLED)).append("\n\n");
            sb.append(t(MessageConstants.Client.PROTOCOL)).append(":         ").append(getStringValue(clientInfo.getProtocol())).append("\n\n");
            sb.append(t(MessageConstants.Client.ROOT_URL)).append(":        ").append(getStringValue(clientInfo.getRootUrl())).append("\n\n");
            sb.append(t(MessageConstants.Client.REDIRECT_URIS)).append(":\n").append(getStringValue(clientInfo.getRedirectUris())).append("\n\n");
            sb.append(t(MessageConstants.Client.WEB_ORIGINS)).append(":\n").append(getStringValue(clientInfo.getWebOrigins()));
            
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
                {t(MessageConstants.Client.ID), getStringValue(clientInfo.getClientId())},
                {t(MessageConstants.Client.NAME), getStringValue(clientInfo.getName())},
                {t(MessageConstants.Client.DESCRIPTION), getStringValue(clientInfo.getDescription())},
                {t(MessageConstants.Client.ENABLED), clientInfo.isEnabled() ? t(MessageConstants.Status.ENABLED) : t(MessageConstants.Status.DISABLED)},
                {t(MessageConstants.Client.PROTOCOL), getStringValue(clientInfo.getProtocol())},
                {t(MessageConstants.Client.ROOT_URL), getStringValue(clientInfo.getRootUrl())},
                {t(MessageConstants.Client.REDIRECT_URIS), getStringValue(clientInfo.getRedirectUris())},
                {t(MessageConstants.Client.WEB_ORIGINS), getStringValue(clientInfo.getWebOrigins())}
            };

            String[] columns = {t(MessageConstants.Dialog.PROPERTY), t(MessageConstants.Dialog.VALUE)};
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
        return value != null && !value.isEmpty() ? value : t(MessageConstants.Dialog.NOT_SET);
    }
}
