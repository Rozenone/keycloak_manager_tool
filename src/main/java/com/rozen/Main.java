package com.rozen;

import com.rozen.service.ConfigStorage;
import com.rozen.ui.ConfigSelectionDialog;
import com.rozen.ui.KeycloakClientManagerUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 设置外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // 显示配置选择对话框
            ConfigSelectionDialog dialog = new ConfigSelectionDialog(null);
            dialog.setVisible(true);

            if (!dialog.isConfirmed()) {
                System.exit(0);
            }

            ConfigStorage.ConnectionConfig config = dialog.getSelectedConfig();
            if (config == null) {
                JOptionPane.showMessageDialog(null, "未选择有效的配置", "错误", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // 启动主界面，传入配置
            KeycloakClientManagerUI ui = new KeycloakClientManagerUI(config);
            ui.setVisible(true);
        });
    }
}