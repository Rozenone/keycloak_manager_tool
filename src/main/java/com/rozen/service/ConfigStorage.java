package com.rozen.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigStorage {

    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".keycloak_manager";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "connections.properties";
    private static final String CONFIG_INDEX_FILE = CONFIG_DIR + File.separator + "config.list";

    // 登录方式枚举
    public enum AuthType {
        USERNAME_PASSWORD("用户名密码"),
        CLIENT_CREDENTIALS("客户端凭据");

        private final String displayName;

        AuthType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 代理协议类型枚举
    public enum ProxyProtocol {
        HTTP("HTTP"),
        HTTPS("HTTPS");

        private final String displayName;

        ProxyProtocol(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class ConnectionConfig {
        private String name;
        private String serverUrl;
        private String realm;
        private AuthType authType;
        // 用户名密码方式
        private String username;
        private String password;
        // 客户端凭据方式
        private String clientId;
        private String clientSecret;
        // 代理配置
        private boolean useProxy;
        private ProxyProtocol proxyProtocol;
        private String proxyHost;
        private int proxyPort;
        private boolean skipSslVerify;

        public ConnectionConfig() {
            this.skipSslVerify = false;
            this.useProxy = false;
            this.authType = AuthType.USERNAME_PASSWORD;
            this.proxyProtocol = ProxyProtocol.HTTP;
            this.proxyPort = 8080;
        }

        public ConnectionConfig(String name, String serverUrl, String realm, String username, String password) {
            this(name, serverUrl, realm, username, password, false);
        }

        public ConnectionConfig(String name, String serverUrl, String realm, String username, String password, boolean skipSslVerify) {
            this.name = name;
            this.serverUrl = serverUrl;
            this.realm = realm;
            this.authType = AuthType.USERNAME_PASSWORD;
            this.username = username;
            this.password = password;
            this.skipSslVerify = skipSslVerify;
            this.useProxy = false;
            this.proxyProtocol = ProxyProtocol.HTTP;
            this.proxyPort = 8080;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getServerUrl() { return serverUrl; }
        public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

        public String getRealm() { return realm; }
        public void setRealm(String realm) { this.realm = realm; }

        public AuthType getAuthType() { return authType; }
        public void setAuthType(AuthType authType) { this.authType = authType; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

        public boolean isUseProxy() { return useProxy; }
        public void setUseProxy(boolean useProxy) { this.useProxy = useProxy; }

        public ProxyProtocol getProxyProtocol() { return proxyProtocol; }
        public void setProxyProtocol(ProxyProtocol proxyProtocol) { this.proxyProtocol = proxyProtocol; }

        public String getProxyHost() { return proxyHost; }
        public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; }

        public int getProxyPort() { return proxyPort; }
        public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }

        public boolean isSkipSslVerify() { return skipSslVerify; }
        public void setSkipSslVerify(boolean skipSslVerify) { this.skipSslVerify = skipSslVerify; }

        @Override
        public String toString() {
            return name != null ? name : (serverUrl + " - " + realm);
        }
    }

    private void ensureConfigDirExists() {
        Path dir = Paths.get(CONFIG_DIR);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("无法创建配置目录: " + e.getMessage(), e);
            }
        }
    }

    public List<String> getConfigNames() {
        List<String> names = new ArrayList<>();
        File indexFile = new File(CONFIG_INDEX_FILE);
        if (indexFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        names.add(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return names;
    }

    public ConnectionConfig loadConfig(String name) {
        ensureConfigDirExists();
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return null;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String prefix = "config." + name + ".";
        ConnectionConfig config = new ConnectionConfig();
        config.setName(name);
        config.setServerUrl(props.getProperty(prefix + "serverUrl", ""));
        config.setRealm(props.getProperty(prefix + "realm", ""));

        // 登录方式
        String authTypeStr = props.getProperty(prefix + "authType", "USERNAME_PASSWORD");
        try {
            config.setAuthType(AuthType.valueOf(authTypeStr));
        } catch (IllegalArgumentException e) {
            config.setAuthType(AuthType.USERNAME_PASSWORD);
        }

        // 用户名密码方式
        config.setUsername(props.getProperty(prefix + "username", ""));
        config.setPassword(decrypt(props.getProperty(prefix + "password", "")));

        // 客户端凭据方式
        config.setClientId(props.getProperty(prefix + "clientId", ""));
        config.setClientSecret(decrypt(props.getProperty(prefix + "clientSecret", "")));

        // 代理配置
        config.setUseProxy(Boolean.parseBoolean(props.getProperty(prefix + "useProxy", "false")));
        String proxyProtocolStr = props.getProperty(prefix + "proxyProtocol", "HTTP");
        try {
            config.setProxyProtocol(ProxyProtocol.valueOf(proxyProtocolStr));
        } catch (IllegalArgumentException e) {
            config.setProxyProtocol(ProxyProtocol.HTTP);
        }
        config.setProxyHost(props.getProperty(prefix + "proxyHost", ""));
        config.setProxyPort(Integer.parseInt(props.getProperty(prefix + "proxyPort", "8080")));

        config.setSkipSslVerify(Boolean.parseBoolean(props.getProperty(prefix + "skipSslVerify", "false")));

        return config;
    }

    public void saveConfig(ConnectionConfig config) {
        ensureConfigDirExists();
        String name = config.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("配置名称不能为空");
        }

        // 更新索引文件
        List<String> names = getConfigNames();
        if (!names.contains(name)) {
            names.add(name);
            saveConfigNames(names);
        }

        // 保存配置
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String prefix = "config." + name + ".";
        props.setProperty(prefix + "serverUrl", config.getServerUrl());
        props.setProperty(prefix + "realm", config.getRealm());

        // 登录方式
        props.setProperty(prefix + "authType", config.getAuthType() != null ? config.getAuthType().name() : AuthType.USERNAME_PASSWORD.name());

        // 用户名密码方式
        props.setProperty(prefix + "username", config.getUsername() != null ? config.getUsername() : "");
        props.setProperty(prefix + "password", encrypt(config.getPassword()));

        // 客户端凭据方式
        props.setProperty(prefix + "clientId", config.getClientId() != null ? config.getClientId() : "");
        props.setProperty(prefix + "clientSecret", encrypt(config.getClientSecret()));

        // 代理配置
        props.setProperty(prefix + "useProxy", String.valueOf(config.isUseProxy()));
        props.setProperty(prefix + "proxyProtocol", config.getProxyProtocol() != null ? config.getProxyProtocol().name() : ProxyProtocol.HTTP.name());
        props.setProperty(prefix + "proxyHost", config.getProxyHost() != null ? config.getProxyHost() : "");
        props.setProperty(prefix + "proxyPort", String.valueOf(config.getProxyPort()));

        props.setProperty(prefix + "skipSslVerify", String.valueOf(config.isSkipSslVerify()));

        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "Keycloak Manager Connection Configurations");
        } catch (IOException e) {
            throw new RuntimeException("保存配置失败: " + e.getMessage(), e);
        }
    }

    public void deleteConfig(String name) {
        List<String> names = getConfigNames();
        names.remove(name);
        saveConfigNames(names);

        // 从属性文件中删除
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            String prefix = "config." + name + ".";
            props.remove(prefix + "serverUrl");
            props.remove(prefix + "realm");
            props.remove(prefix + "authType");
            props.remove(prefix + "username");
            props.remove(prefix + "password");
            props.remove(prefix + "clientId");
            props.remove(prefix + "clientSecret");
            props.remove(prefix + "useProxy");
            props.remove(prefix + "proxyProtocol");
            props.remove(prefix + "proxyHost");
            props.remove(prefix + "proxyPort");
            props.remove(prefix + "skipSslVerify");

            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "Keycloak Manager Connection Configurations");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveConfigNames(List<String> names) {
        ensureConfigDirExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_INDEX_FILE))) {
            for (String name : names) {
                writer.write(name);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 简单的加密/解密（Base64）
    private String encrypt(String text) {
        if (text == null) return "";
        return java.util.Base64.getEncoder().encodeToString(text.getBytes());
    }

    private String decrypt(String text) {
        if (text == null || text.isEmpty()) return "";
        try {
            return new String(java.util.Base64.getDecoder().decode(text));
        } catch (Exception e) {
            return text; // 如果解密失败，返回原文
        }
    }
}
