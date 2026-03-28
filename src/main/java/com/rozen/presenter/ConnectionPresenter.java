package com.rozen.presenter;

import com.rozen.service.ConfigStorage;
import com.rozen.service.KeycloakService;

import java.util.List;
import java.util.function.Consumer;

/**
 * 连接管理的 Presenter 层
 * 负责处理连接配置和 Keycloak 连接相关的业务逻辑
 */
public class ConnectionPresenter {

    private final ConfigStorage configStorage;
    private KeycloakService keycloakService;
    private Consumer<String> onError;
    private Consumer<String> onStatusUpdate;
    private Consumer<Boolean> onConnectionStateChanged;

    public ConnectionPresenter() {
        this.configStorage = new ConfigStorage();
    }

    public void setOnError(Consumer<String> onError) {
        this.onError = onError;
    }

    public void setOnStatusUpdate(Consumer<String> onStatusUpdate) {
        this.onStatusUpdate = onStatusUpdate;
    }

    public void setOnConnectionStateChanged(Consumer<Boolean> onConnectionStateChanged) {
        this.onConnectionStateChanged = onConnectionStateChanged;
    }

    /**
     * 获取所有配置名称
     */
    public List<String> getConfigNames() {
        return configStorage.getConfigNames();
    }

    /**
     * 加载指定名称的配置
     */
    public ConfigStorage.ConnectionConfig loadConfig(String name) {
        return configStorage.loadConfig(name);
    }

    /**
     * 保存配置
     */
    public boolean saveConfig(ConfigStorage.ConnectionConfig config) {
        try {
            configStorage.saveConfig(config);
            return true;
        } catch (Exception ex) {
            notifyError("保存配置失败: " + ex.getMessage());
            return false;
        }
    }

    /**
     * 删除配置
     */
    public boolean deleteConfig(String name) {
        try {
            configStorage.deleteConfig(name);
            return true;
        } catch (Exception ex) {
            notifyError("删除配置失败: " + ex.getMessage());
            return false;
        }
    }

    /**
     * 测试连接
     */
    public void testConnection(ConfigStorage.ConnectionConfig config, Consumer<TestResult> callback) {
        new Thread(() -> {
            try (KeycloakService testService = KeycloakService.fromConfig(config)) {
                testService.testConnection();
                callback.accept(new TestResult(true, "连接测试成功", config));
            } catch (Exception ex) {
                callback.accept(new TestResult(false, "连接测试失败: " + ex.getMessage(), config));
            }
        }).start();
    }

    /**
     * 建立连接
     */
    public boolean connect(ConfigStorage.ConnectionConfig config) {
        try {
            if (keycloakService != null) {
                keycloakService.close();
            }
            keycloakService = KeycloakService.fromConfig(config);
            keycloakService.testConnection();
            notifyStatus("已连接到: " + config.getServerUrl());
            notifyConnectionStateChanged(true);
            return true;
        } catch (Exception ex) {
            notifyError("连接失败: " + ex.getMessage());
            keycloakService = null;
            notifyConnectionStateChanged(false);
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (keycloakService != null) {
            keycloakService.close();
            keycloakService = null;
        }
        notifyConnectionStateChanged(false);
    }

    /**
     * 获取当前的 KeycloakService 实例
     */
    public KeycloakService getKeycloakService() {
        return keycloakService;
    }

    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return keycloakService != null;
    }

    private void notifyError(String message) {
        if (onError != null) {
            onError.accept(message);
        }
    }

    private void notifyStatus(String message) {
        if (onStatusUpdate != null) {
            onStatusUpdate.accept(message);
        }
    }

    private void notifyConnectionStateChanged(boolean connected) {
        if (onConnectionStateChanged != null) {
            onConnectionStateChanged.accept(connected);
        }
    }

    /**
     * 连接测试结果
     */
    public static class TestResult {
        private final boolean success;
        private final String message;
        private final ConfigStorage.ConnectionConfig config;

        public TestResult(boolean success, String message, ConfigStorage.ConnectionConfig config) {
            this.success = success;
            this.message = message;
            this.config = config;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public ConfigStorage.ConnectionConfig getConfig() {
            return config;
        }
    }
}
