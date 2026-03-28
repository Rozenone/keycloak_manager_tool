package com.rozen;

import com.rozen.constant.MessageConstants;
import com.rozen.service.ConfigStorage;
import com.rozen.service.I18nManager;
import com.rozen.ui.ConfigSelectionDialog;
import com.rozen.ui.KeycloakClientManagerUI;

import javax.swing.*;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static final String DEFAULT_LANGUAGE = "en"; // 默认英文

    public static void main(String[] args) {
        // 强制设置默认Locale为英文（在加载任何资源之前）
        Locale.setDefault(Locale.ENGLISH);

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

        // 初始化国际化（默认英文）
        initI18n(args);

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
                JOptionPane.showMessageDialog(null,
                    I18nManager.t(MessageConstants.Msg.NO_SELECTION),
                    I18nManager.t(MessageConstants.Msg.ERROR),
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // 启动主界面，传入配置
            KeycloakClientManagerUI ui = new KeycloakClientManagerUI(config);
            ui.setVisible(true);
        });
    }

    /**
     * 初始化国际化设置
     * 支持通过命令行参数指定语言: --lang=zh 或 --lang=ja 或 --lang=en
     */
    private static void initI18n(String[] args) {
        String langCode = DEFAULT_LANGUAGE;

        // 从命令行参数解析语言设置
        for (String arg : args) {
            if (arg.startsWith("--lang=")) {
                langCode = arg.substring(7);
                break;
            }
        }

        // 设置语言
        I18nManager.Language language = I18nManager.Language.fromCode(langCode);
        I18nManager.getInstance().setLocale(language);

        // 设置系统默认locale（影响JOptionPane等系统对话框）
        Locale.setDefault(I18nManager.getInstance().getCurrentLanguage() == I18nManager.Language.ENGLISH
            ? Locale.ENGLISH
            : new Locale(language.getCode()));
    }
}