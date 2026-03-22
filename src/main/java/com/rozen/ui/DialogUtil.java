package com.rozen.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogUtil {

    /**
     * 显示可复制的错误对话框
     */
    public static void showError(Component parent, String title, String message) {
        showCopyableDialog(parent, title, message, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 显示可复制的警告对话框
     */
    public static void showWarning(Component parent, String title, String message) {
        showCopyableDialog(parent, title, message, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 显示可复制的信息对话框
     */
    public static void showInfo(Component parent, String title, String message) {
        showCopyableDialog(parent, title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示可复制的对话框（使用可编辑的JTextArea）
     */
    private static void showCopyableDialog(Component parent, String title, String message, int messageType) {
        // 创建可编辑的文本区域
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        textArea.setCaretPosition(0);

        // 设置首选大小，确保对话框不会太大
        textArea.setColumns(50);
        textArea.setRows(Math.min(10, message.split("\n").length + 2));

        // 将文本区域放入滚动面板
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(400, 150));

        // 创建消息面板
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 根据类型添加图标
        Icon icon = UIManager.getIcon(messageType == JOptionPane.ERROR_MESSAGE ? "OptionPane.errorIcon" :
                messageType == JOptionPane.WARNING_MESSAGE ? "OptionPane.warningIcon" : "OptionPane.informationIcon");

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            panel.add(iconLabel, BorderLayout.WEST);
        }

        panel.add(scrollPane, BorderLayout.CENTER);

        // 添加提示标签
        JLabel hintLabel = new JLabel("提示：文本已选中，可以直接按 Ctrl+C 复制");
        hintLabel.setFont(new Font(hintLabel.getFont().getName(), Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, BorderLayout.SOUTH);

        // 显示对话框
        JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * 显示异常错误对话框（包含堆栈跟踪）
     */
    public static void showExceptionError(Component parent, String title, Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("错误信息: ").append(exception.getMessage()).append("\n\n");
        sb.append("堆栈跟踪:\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("    at ").append(element.toString()).append("\n");
        }
        showError(parent, title, sb.toString());
    }
}
