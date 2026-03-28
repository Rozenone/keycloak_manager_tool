package com.rozen.ui;

import com.rozen.constant.MessageConstants;
import com.rozen.model.ClientInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

import static com.rozen.ui.DialogUtil.*;
import static com.rozen.service.I18nManager.t;

public class ClientDialog extends JDialog {

    private boolean confirmed = false;
    private ClientInfo clientInfo;

    private JTextField clientIdField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JCheckBox enabledCheckBox;
    private JTextField rootUrlField;
    private JTextArea redirectUrisArea;
    private JTextArea webOriginsArea;

    public ClientDialog(JFrame parent, ClientInfo existingClient) {
        super(parent, existingClient == null ? t(MessageConstants.Client.ADD) : t(MessageConstants.Client.EDIT), true);
        this.clientInfo = existingClient != null ? existingClient : new ClientInfo();

        setSize(500, 500);
        setLocationRelativeTo(parent);
        initComponents();

        if (existingClient != null) {
            populateFields();
        }
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // 客户端ID
        addFormRow(formPanel, gbc, row++, t(MessageConstants.Client.ID) + " *:", clientIdField = new JTextField(20));

        // 名称
        addFormRow(formPanel, gbc, row++, t(MessageConstants.Client.NAME) + ":", nameField = new JTextField(20));

        // 启用状态
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel(t(MessageConstants.Client.ENABLED) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setSelected(true);
        formPanel.add(enabledCheckBox, gbc);
        row++;

        // 根URL
        addFormRow(formPanel, gbc, row++, t(MessageConstants.Client.ROOT_URL) + ":", rootUrlField = new JTextField(20));

        // 描述
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel(t(MessageConstants.Client.DESCRIPTION) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        row++;

        // 重定向URI
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        formPanel.add(new JLabel(t(MessageConstants.Client.REDIRECT_URIS) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        redirectUrisArea = new JTextArea(3, 20);
        redirectUrisArea.setLineWrap(true);
        formPanel.add(new JScrollPane(redirectUrisArea), gbc);
        row++;

        // Web Origins
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        formPanel.add(new JLabel(t(MessageConstants.Client.WEB_ORIGINS) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        webOriginsArea = new JTextArea(2, 20);
        webOriginsArea.setLineWrap(true);
        formPanel.add(new JScrollPane(webOriginsArea), gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okBtn = new JButton(t(MessageConstants.Button.OK));
        okBtn.addActionListener(this::onOk);
        buttonPanel.add(okBtn);

        JButton cancelBtn = new JButton(t(MessageConstants.Button.CANCEL));
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void populateFields() {
        clientIdField.setText(clientInfo.getClientId());
        clientIdField.setEditable(false);
        nameField.setText(clientInfo.getName());
        enabledCheckBox.setSelected(clientInfo.isEnabled());
        rootUrlField.setText(clientInfo.getRootUrl());
        descriptionArea.setText(clientInfo.getDescription());
        redirectUrisArea.setText(clientInfo.getRedirectUris());
        webOriginsArea.setText(clientInfo.getWebOrigins());
    }

    private void onOk(ActionEvent e) {
        String clientId = clientIdField.getText().trim();
        if (clientId.isEmpty()) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.CLIENT_ID_REQUIRED));
            return;
        }

        clientInfo.setClientId(clientId);
        clientInfo.setName(nameField.getText().trim());
        clientInfo.setEnabled(enabledCheckBox.isSelected());
        clientInfo.setRootUrl(rootUrlField.getText().trim());
        clientInfo.setDescription(descriptionArea.getText().trim());
        clientInfo.setRedirectUris(redirectUrisArea.getText().trim());
        clientInfo.setWebOrigins(webOriginsArea.getText().trim());

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
}
