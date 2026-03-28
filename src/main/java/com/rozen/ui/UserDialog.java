package com.rozen.ui;

import com.rozen.constant.MessageConstants;
import com.rozen.model.UserInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import static com.rozen.ui.DialogUtil.*;
import static com.rozen.service.I18nManager.t;

public class UserDialog extends JDialog {

    private boolean confirmed = false;
    private final UserInfo userInfo;

    private JTextField usernameField;
    private JTextField emailField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JCheckBox enabledCheckBox;
    private JCheckBox emailVerifiedCheckBox;
    private JTable attributesTable;
    private DefaultTableModel attributesTableModel;

    public UserDialog(JFrame parent, UserInfo existingUser) {
        super(parent, existingUser == null ? t(MessageConstants.User.ADD) : t(MessageConstants.User.EDIT), true);
        this.userInfo = existingUser != null ? existingUser : new UserInfo();
        boolean isEditMode = existingUser != null;

        setSize(600, 550);
        setLocationRelativeTo(parent);
        initComponents();

        if (isEditMode) {
            populateFields();
        }
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.User.BASIC_INFO)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // 用户名
        addFormRow(formPanel, gbc, row++, t(MessageConstants.User.USERNAME) + " *:", usernameField = new JTextField(20));

        // 邮箱
        addFormRow(formPanel, gbc, row++, t(MessageConstants.User.EMAIL) + ":", emailField = new JTextField(20));

        // 姓
        addFormRow(formPanel, gbc, row++, t(MessageConstants.User.FIRST_NAME) + ":", firstNameField = new JTextField(20));

        // 名
        addFormRow(formPanel, gbc, row++, t(MessageConstants.User.LAST_NAME) + ":", lastNameField = new JTextField(20));

        // 启用状态
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel(t(MessageConstants.User.ENABLED) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setSelected(true);
        formPanel.add(enabledCheckBox, gbc);
        row++;

        // 邮箱已验证
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        formPanel.add(new JLabel(t(MessageConstants.User.EMAIL_VERIFIED) + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailVerifiedCheckBox = new JCheckBox();
        formPanel.add(emailVerifiedCheckBox, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // 属性面板
        JPanel attributesPanel = createAttributesPanel();
        panel.add(attributesPanel, BorderLayout.CENTER);

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

    private JPanel createAttributesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(t(MessageConstants.User.ATTRIBUTES)));

        // 属性表格
        String[] columns = {t(MessageConstants.User.ATTR_NAME), t(MessageConstants.User.ATTR_VALUE)};
        attributesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        attributesTable = new JTable(attributesTableModel);
        attributesTable.setRowHeight(25);
        
        // 确保编辑后的值被保存
        attributesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JScrollPane scrollPane = new JScrollPane(attributesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn = new JButton(t(MessageConstants.Button.ADD_ATTR));
        addBtn.addActionListener(e -> attributesTableModel.addRow(new Object[]{"", ""}));
        buttonPanel.add(addBtn);

        JButton deleteBtn = new JButton(t(MessageConstants.Button.DELETE_SELECTED));
        deleteBtn.addActionListener(e -> {
            int selectedRow = attributesTable.getSelectedRow();
            if (selectedRow >= 0) {
                attributesTableModel.removeRow(selectedRow);
            }
        });
        buttonPanel.add(deleteBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

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

    private void populateFields() {
        usernameField.setText(userInfo.getUsername());
        usernameField.setEditable(false); // 编辑模式下用户名不可修改
        emailField.setText(userInfo.getEmail());
        firstNameField.setText(userInfo.getFirstName());
        lastNameField.setText(userInfo.getLastName());
        enabledCheckBox.setSelected(userInfo.isEnabled());
        emailVerifiedCheckBox.setSelected(userInfo.isEmailVerified());

        // 填充属性表格
        if (userInfo.getAttributes() != null) {
            for (Map.Entry<String, List<String>> entry : userInfo.getAttributes().entrySet()) {
                String value = entry.getValue() != null && !entry.getValue().isEmpty() 
                    ? String.join(", ", entry.getValue()) : "";
                attributesTableModel.addRow(new Object[]{entry.getKey(), value});
            }
        }
    }

    private void onOk(ActionEvent e) {
        // 停止表格编辑，确保值被保存
        if (attributesTable.isEditing()) {
            attributesTable.getCellEditor().stopCellEditing();
        }
        
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError(this, t(MessageConstants.Msg.ERROR), t(MessageConstants.Validation.USERNAME_REQUIRED));
            return;
        }

        userInfo.setUsername(username);
        userInfo.setEmail(emailField.getText().trim());
        userInfo.setFirstName(firstNameField.getText().trim());
        userInfo.setLastName(lastNameField.getText().trim());
        userInfo.setEnabled(enabledCheckBox.isSelected());
        userInfo.setEmailVerified(emailVerifiedCheckBox.isSelected());

        // 收集属性
        Map<String, List<String>> attributes = new HashMap<>();
        for (int i = 0; i < attributesTableModel.getRowCount(); i++) {
            Object keyObj = attributesTableModel.getValueAt(i, 0);
            Object valueObj = attributesTableModel.getValueAt(i, 1);
            String key = keyObj != null ? keyObj.toString().trim() : "";
            String value = valueObj != null ? valueObj.toString().trim() : "";
            
            System.out.println("[DEBUG] UserDialog row " + i + ": key=" + key + ", value=" + value);
            
            if (!key.isEmpty()) {
                // 支持逗号分隔的多个值
                List<String> values;
                if (value.isEmpty()) {
                    values = Collections.singletonList("");
                } else {
                    values = Arrays.asList(value.split(","));
                    values = values.stream().map(String::trim).collect(java.util.stream.Collectors.toList());
                }
                attributes.put(key, values);
                System.out.println("[DEBUG] UserDialog added attribute: " + key + "=" + values);
            }
        }
        System.out.println("[DEBUG] UserDialog final attributes: " + attributes);
        userInfo.setAttributes(attributes);

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
