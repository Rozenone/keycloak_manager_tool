package com.rozen.service;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 国际化管理器
 * 支持语言：英文(默认)、中文、日语
 */
public class I18nManager {

    private static final String BUNDLE_NAME = "messages";
    private static I18nManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;

    // 支持的语言
    public enum Language {
        ENGLISH("en", "English"),
        CHINESE("zh", "中文"),
        JAPANESE("ja", "日本語");

        private final String code;
        private final String displayName;

        Language(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) {
                    return lang;
                }
            }
            return ENGLISH; // 默认英文
        }
    }

    private I18nManager() {
        // 默认使用英文
        setLocale(Language.ENGLISH);
    }

    public static synchronized I18nManager getInstance() {
        if (instance == null) {
            instance = new I18nManager();
        }
        return instance;
    }

    /**
     * 设置当前语言
     */
    public void setLocale(Language language) {
        this.currentLocale = new Locale(language.getCode());
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
    }

    /**
     * 获取当前语言
     */
    public Language getCurrentLanguage() {
        return Language.fromCode(currentLocale.getLanguage());
    }

    /**
     * 获取翻译文本
     */
    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // 如果找不到，返回 key 本身
        }
    }

    /**
     * 获取带参数的翻译文本
     */
    public String getString(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return String.format(pattern, args);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * 便捷方法：获取翻译文本
     */
    public static String t(String key) {
        return getInstance().getString(key);
    }

    /**
     * 便捷方法：获取带参数的翻译文本
     */
    public static String t(String key, Object... args) {
        return getInstance().getString(key, args);
    }

    /**
     * 使用 MessageConstants 常量获取翻译文本
     * @param constant MessageConstants 中的常量值
     * @return 翻译后的文本
     */
    public static String t(MessageConstant constant) {
        return getInstance().getString(constant.getKey());
    }

    /**
     * 使用 MessageConstants 常量获取带参数的翻译文本
     * @param constant MessageConstants 中的常量值
     * @param args 参数
     * @return 翻译后的文本
     */
    public static String t(MessageConstant constant, Object... args) {
        return getInstance().getString(constant.getKey(), args);
    }

    /**
     * 消息常量接口，用于类型安全地传递常量
     */
    public interface MessageConstant {
        String getKey();
    }
}
