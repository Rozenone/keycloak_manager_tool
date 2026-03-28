package com.rozen;

import com.rozen.service.ConfigStorage;
import com.rozen.ui.ConfigSelectionDialog;
import com.rozen.ui.KeycloakClientManagerUI;

import javax.swing.*;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        // 从资源文件加载日志配置
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (Exception e) {
            // 忽略配置加载错误
        }

        // 确保 RESTEasy 警告被禁用
        Logger.getLogger("org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl").setLevel(Level.SEVERE);
        Logger.getLogger("org.jboss.resteasy").setLevel(Level.SEVERE);

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