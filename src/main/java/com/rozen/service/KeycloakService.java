package com.rozen.service;

import com.rozen.model.ClientInfo;
import com.rozen.model.UserInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

public class KeycloakService implements AutoCloseable {

    private final Keycloak keycloak;
    private final String realm;

    /**
     * 使用用户名密码方式连接
     */
    public KeycloakService(String serverUrl, String realm, String username, String password,
                           boolean useProxy, String proxyProtocol, String proxyHost, int proxyPort, boolean skipSslVerify) {
        this.realm = realm;

        try {
            KeycloakBuilder builder = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .username(username)
                    .password(password)
                    .clientId("admin-cli");

            // 创建 HTTP 客户端
            Client client = createClient(useProxy, proxyProtocol, proxyHost, proxyPort, skipSslVerify);
            builder.resteasyClient(client);

            this.keycloak = builder.build();
        } catch (Exception e) {
            throw new RuntimeException("初始化 Keycloak 客户端失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用客户端凭据方式连接
     */
    public KeycloakService(String serverUrl, String realm, String clientId, String clientSecret,
                           boolean useProxy, String proxyProtocol, String proxyHost, int proxyPort, boolean skipSslVerify,
                           boolean isClientCredentials) {
        this.realm = realm;

        try {
            KeycloakBuilder builder = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .grantType(org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS);

            // 创建 HTTP 客户端
            Client client = createClient(useProxy, proxyProtocol, proxyHost, proxyPort, skipSslVerify);
            builder.resteasyClient(client);

            this.keycloak = builder.build();
        } catch (Exception e) {
            throw new RuntimeException("初始化 Keycloak 客户端失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 ConnectionConfig 创建服务
     */
    public static KeycloakService fromConfig(ConfigStorage.ConnectionConfig config) {
        String proxyProtocol = config.getProxyProtocol() != null ? config.getProxyProtocol().name() : "HTTP";
        if (config.getAuthType() == ConfigStorage.AuthType.CLIENT_CREDENTIALS) {
            return new KeycloakService(
                    config.getServerUrl(),
                    config.getRealm(),
                    config.getClientId(),
                    config.getClientSecret(),
                    config.isUseProxy(),
                    proxyProtocol,
                    config.getProxyHost(),
                    config.getProxyPort(),
                    config.isSkipSslVerify(),
                    true
            );
        } else {
            return new KeycloakService(
                    config.getServerUrl(),
                    config.getRealm(),
                    config.getUsername(),
                    config.getPassword(),
                    config.isUseProxy(),
                    proxyProtocol,
                    config.getProxyHost(),
                    config.getProxyPort(),
                    config.isSkipSslVerify()
            );
        }
    }

    private Client createClient(boolean useProxy, String proxyProtocol, String proxyHost, int proxyPort, boolean skipSslVerify) throws Exception {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder()
                .register(ResteasyJackson2Provider.class)
                .register(FormUrlEncodedProvider.class);

        // 配置代理
        if (useProxy && proxyHost != null && !proxyHost.trim().isEmpty()) {
            // 设置代理协议（HTTP 或 HTTPS）
            String proxyScheme = "HTTPS".equalsIgnoreCase(proxyProtocol) ? "https" : "http";
            clientBuilder.property("org.jboss.resteasy.jaxrs.client.proxy.host", proxyHost)
                    .property("org.jboss.resteasy.jaxrs.client.proxy.port", proxyPort)
                    .property("org.jboss.resteasy.jaxrs.client.proxy.scheme", proxyScheme);
        }

        // 配置 SSL
        if (skipSslVerify) {
            // 创建允许所有 SSL 证书的 TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            clientBuilder.sslContext(sslContext)
                    .hostnameVerifier((hostname, session) -> true);
        }

        return clientBuilder.build();
    }

    // ==================== 客户端相关方法 ====================

    public List<ClientInfo> getClients() {
        RealmResource realmResource = keycloak.realm(realm);
        ClientsResource clientsResource = realmResource.clients();

        List<ClientRepresentation> clients = clientsResource.findAll();
        return clients.stream()
                .map(this::convertToClientInfo)
                .collect(Collectors.toList());
    }

    public ClientInfo getClientByClientId(String clientId) {
        RealmResource realmResource = keycloak.realm(realm);
        ClientsResource clientsResource = realmResource.clients();

        List<ClientRepresentation> clients = clientsResource.findByClientId(clientId);
        if (clients.isEmpty()) {
            return null;
        }
        return convertToClientInfo(clients.get(0));
    }

    public void createClient(ClientInfo clientInfo) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientInfo.getClientId());
        client.setName(clientInfo.getName());
        client.setDescription(clientInfo.getDescription());
        client.setEnabled(clientInfo.isEnabled());
        client.setProtocol(clientInfo.getProtocol());
        client.setRootUrl(clientInfo.getRootUrl());

        // 处理重定向URI
        if (clientInfo.getRedirectUris() != null && !clientInfo.getRedirectUris().isEmpty()) {
            List<String> redirectUris = Arrays.stream(clientInfo.getRedirectUris().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            client.setRedirectUris(redirectUris);
        }

        // 处理Web Origins
        if (clientInfo.getWebOrigins() != null && !clientInfo.getWebOrigins().isEmpty()) {
            List<String> webOrigins = Arrays.stream(clientInfo.getWebOrigins().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            client.setWebOrigins(webOrigins);
        }

        // 设置默认的客户端协议配置
        client.setStandardFlowEnabled(true);
        client.setImplicitFlowEnabled(false);
        client.setDirectAccessGrantsEnabled(true);
        client.setServiceAccountsEnabled(false);

        RealmResource realmResource = keycloak.realm(realm);

        try {
            realmResource.clients().create(client);
        } catch (Exception e) {
            throw new RuntimeException("创建客户端失败: " + e.getMessage(), e);
        }
    }

    public void deleteClient(String clientId) {
        RealmResource realmResource = keycloak.realm(realm);
        ClientsResource clientsResource = realmResource.clients();

        // 先查找客户端的内部ID
        List<ClientRepresentation> clients = clientsResource.findByClientId(clientId);
        if (clients.isEmpty()) {
            throw new RuntimeException("客户端不存在: " + clientId);
        }

        String id = clients.get(0).getId();
        clientsResource.get(id).remove();
    }

    public void updateClient(String clientId, ClientInfo clientInfo) {
        RealmResource realmResource = keycloak.realm(realm);
        ClientsResource clientsResource = realmResource.clients();

        List<ClientRepresentation> clients = clientsResource.findByClientId(clientId);
        if (clients.isEmpty()) {
            throw new RuntimeException("客户端不存在: " + clientId);
        }

        String id = clients.get(0).getId();
        ClientRepresentation client = clientsResource.get(id).toRepresentation();

        client.setName(clientInfo.getName());
        client.setDescription(clientInfo.getDescription());
        client.setEnabled(clientInfo.isEnabled());
        client.setRootUrl(clientInfo.getRootUrl());

        // 处理重定向URI
        if (clientInfo.getRedirectUris() != null && !clientInfo.getRedirectUris().isEmpty()) {
            List<String> redirectUris = Arrays.stream(clientInfo.getRedirectUris().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            client.setRedirectUris(redirectUris);
        }

        // 处理Web Origins
        if (clientInfo.getWebOrigins() != null && !clientInfo.getWebOrigins().isEmpty()) {
            List<String> webOrigins = Arrays.stream(clientInfo.getWebOrigins().split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            client.setWebOrigins(webOrigins);
        }

        clientsResource.get(id).update(client);
    }

    private ClientInfo convertToClientInfo(ClientRepresentation client) {
        ClientInfo info = new ClientInfo();
        info.setId(client.getId());
        info.setClientId(client.getClientId());
        info.setName(client.getName());
        info.setDescription(client.getDescription());
        info.setEnabled(client.isEnabled());
        info.setProtocol(client.getProtocol());
        info.setRootUrl(client.getRootUrl());

        // 将列表转换为多行字符串
        if (client.getRedirectUris() != null) {
            info.setRedirectUris(String.join("\n", client.getRedirectUris()));
        }
        if (client.getWebOrigins() != null) {
            info.setWebOrigins(String.join("\n", client.getWebOrigins()));
        }

        return info;
    }

    // ==================== 用户相关方法 ====================

    public static class PageResult<T> {
        private List<T> content;
        private int totalCount;
        private int page;
        private int pageSize;

        public PageResult(List<T> content, int totalCount, int page, int pageSize) {
            this.content = content;
            this.totalCount = totalCount;
            this.page = page;
            this.pageSize = pageSize;
        }

        public List<T> getContent() { return content; }
        public int getTotalCount() { return totalCount; }
        public int getPage() { return page; }
        public int getPageSize() { return pageSize; }
        public int getTotalPages() {
            return (int) Math.ceil((double) totalCount / pageSize);
        }
    }

    public PageResult<UserInfo> getUsers(int page, int pageSize) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 获取总数
        int totalCount = usersResource.count();

        // 分页查询
        List<UserRepresentation> users = usersResource.list(page * pageSize, pageSize);
        List<UserInfo> userInfos = users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());

        return new PageResult<>(userInfos, totalCount, page, pageSize);
    }

    public PageResult<UserInfo> searchUsers(String search, String searchType, boolean exactMatch, int page, int pageSize) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users;
        int totalCount = 0;

        if (exactMatch) {
            // 精确匹配搜索
            if ("id".equalsIgnoreCase(searchType)) {
                // 按 ID 精确查找
                try {
                    UserRepresentation user = usersResource.get(search).toRepresentation();
                    users = user != null ? Collections.singletonList(user) : Collections.emptyList();
                    totalCount = users.size();
                } catch (Exception e) {
                    users = Collections.emptyList();
                    totalCount = 0;
                }
            } else if ("username".equalsIgnoreCase(searchType)) {
                // 按用户名精确查找
                users = usersResource.searchByUsername(search, true);
                totalCount = users.size();
            } else if ("email".equalsIgnoreCase(searchType)) {
                // 按邮箱精确查找
                users = usersResource.searchByEmail(search, true);
                totalCount = users.size();
            } else {
                // 默认使用通用搜索（精确匹配）
                users = usersResource.search(search, true);
                totalCount = users.size();
            }
        } else {
            // 模糊匹配搜索
            if ("id".equalsIgnoreCase(searchType)) {
                // 按 ID 模糊查找（实际上 ID 是精确匹配的）
                try {
                    UserRepresentation user = usersResource.get(search).toRepresentation();
                    users = user != null ? Collections.singletonList(user) : Collections.emptyList();
                    totalCount = users.size();
                } catch (Exception e) {
                    users = Collections.emptyList();
                    totalCount = 0;
                }
            } else if ("username".equalsIgnoreCase(searchType)) {
                users = usersResource.searchByUsername(search, false);
                totalCount = users.size();
            } else if ("email".equalsIgnoreCase(searchType)) {
                users = usersResource.searchByEmail(search, false);
                totalCount = users.size();
            } else {
                // 通用模糊搜索（搜索用户名、邮箱、姓、名）
                users = usersResource.search(search, false);
                
                // 同时尝试按 ID 查找并合并结果
                try {
                    UserRepresentation userById = usersResource.get(search).toRepresentation();
                    if (userById != null) {
                        // 检查是否已经在列表中
                        boolean exists = users.stream().anyMatch(u -> search.equals(u.getId()));
                        if (!exists) {
                            List<UserRepresentation> merged = new ArrayList<>(users);
                            merged.add(userById);
                            users = merged;
                        }
                    }
                } catch (Exception e) {
                    // ID 查找失败，忽略
                }
                
                totalCount = users.size();
            }
        }

        // 手动分页（因为 Keycloak API 不支持所有搜索的分页）
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, users.size());
        List<UserRepresentation> pagedUsers = fromIndex < users.size() 
            ? users.subList(fromIndex, toIndex) 
            : Collections.emptyList();

        List<UserInfo> userInfos = pagedUsers.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());

        return new PageResult<>(userInfos, totalCount, page, pageSize);
    }

    public List<UserInfo> searchUsersByAttribute(String attributeName, String attributeValue) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 按属性搜索（精确匹配）
        List<UserRepresentation> users = usersResource.searchByAttributes(attributeName + ":" + attributeValue);
        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }

    public PageResult<UserInfo> searchUsersByAttributeFuzzy(String attributeKeyword, String valueKeyword, int page, int pageSize) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 获取所有用户，然后在内存中过滤（因为 Keycloak API 不支持属性值的模糊搜索）
        List<UserRepresentation> allUsers = usersResource.list(0, Integer.MAX_VALUE);

        // 过滤包含匹配属性的用户
        List<UserInfo> filteredUsers = allUsers.stream()
                .filter(user -> {
                    Map<String, List<String>> attrs = user.getAttributes();
                    if (attrs == null || attrs.isEmpty()) {
                        return false;
                    }
                    // 检查是否有属性名包含关键词，且属性值包含值关键词
                    for (Map.Entry<String, List<String>> entry : attrs.entrySet()) {
                        String attrName = entry.getKey().toLowerCase();
                        List<String> attrValues = entry.getValue();

                        // 属性名模糊匹配
                        boolean nameMatch = attrName.contains(attributeKeyword.toLowerCase());

                        // 属性值模糊匹配（如果指定了值关键词）
                        boolean valueMatch = true;
                        if (valueKeyword != null && !valueKeyword.isEmpty()) {
                            valueMatch = attrValues != null && attrValues.stream()
                                    .anyMatch(v -> v.toLowerCase().contains(valueKeyword.toLowerCase()));
                        }

                        if (nameMatch && valueMatch) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());

        // 手动分页
        int totalCount = filteredUsers.size();
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalCount);

        List<UserInfo> pagedUsers = fromIndex < totalCount
                ? filteredUsers.subList(fromIndex, toIndex)
                : java.util.Collections.emptyList();

        return new PageResult<>(pagedUsers, totalCount, page, pageSize);
    }

    public PageResult<UserInfo> searchUsersByMultipleAttributes(List<String> attrNames, List<String> attrValues, int page, int pageSize) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 获取所有用户，然后在内存中过滤（AND 逻辑）
        List<UserRepresentation> allUsers = usersResource.list(0, Integer.MAX_VALUE);

        // 过滤满足所有属性条件的用户（AND 逻辑）
        List<UserInfo> filteredUsers = allUsers.stream()
                .filter(user -> {
                    Map<String, List<String>> attrs = user.getAttributes();
                    if (attrs == null || attrs.isEmpty()) {
                        return false;
                    }

                    // 检查是否满足所有条件（AND 逻辑）
                    for (int i = 0; i < attrNames.size(); i++) {
                        String searchName = attrNames.get(i).toLowerCase();
                        String searchValue = attrValues.get(i).toLowerCase();

                        // 查找匹配的属性
                        boolean conditionMet = false;
                        for (Map.Entry<String, List<String>> entry : attrs.entrySet()) {
                            String attrName = entry.getKey().toLowerCase();
                            List<String> values = entry.getValue();

                            // 属性名模糊匹配，属性值模糊匹配
                            if (attrName.contains(searchName)) {
                                if (searchValue.isEmpty()) {
                                    // 如果只指定了属性名，匹配即可
                                    conditionMet = true;
                                    break;
                                } else if (values != null && values.stream()
                                        .anyMatch(v -> v.toLowerCase().contains(searchValue))) {
                                    // 属性值也匹配
                                    conditionMet = true;
                                    break;
                                }
                            }
                        }

                        if (!conditionMet) {
                            // 有一个条件不满足，直接返回 false（AND 逻辑）
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());

        // 手动分页
        int totalCount = filteredUsers.size();
        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalCount);

        List<UserInfo> pagedUsers = fromIndex < totalCount
                ? filteredUsers.subList(fromIndex, toIndex)
                : java.util.Collections.emptyList();

        return new PageResult<>(pagedUsers, totalCount, page, pageSize);
    }

    public UserInfo getUserById(String userId) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        try {
            UserRepresentation user = usersResource.get(userId).toRepresentation();
            System.out.println("[DEBUG] getUserById found user: " + user.getUsername() + ", attrs: " + user.getAttributes());
            return convertToUserInfo(user);
        } catch (Exception e) {
            System.out.println("[DEBUG] getUserById error: " + e.getMessage());
            return null;
        }
    }

    public void createUser(UserInfo userInfo) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userInfo.getUsername());
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setEnabled(userInfo.isEnabled());
        user.setEmailVerified(userInfo.isEmailVerified());
        
            // 设置属性
        Map<String, List<String>> attrs = userInfo.getAttributes();
        if (attrs != null && !attrs.isEmpty()) {
            // 过滤掉空值
            Map<String, List<String>> filteredAttrs = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : attrs.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty() 
                    && !entry.getValue().get(0).trim().isEmpty()) {
                    filteredAttrs.put(entry.getKey(), entry.getValue());
                }
            }
            if (!filteredAttrs.isEmpty()) {
                user.setAttributes(filteredAttrs);
            }
        }

        try {
            Response response = usersResource.create(user);
            int status = response.getStatus();
            if (status != 201) {
                String errorBody = response.readEntity(String.class);
                throw new RuntimeException("创建用户失败: HTTP " + status + " - " + errorBody);
            }
            response.close();
        } catch (Exception e) {
            throw new RuntimeException("创建用户失败: " + e.getMessage(), e);
        }
    }

    public void updateUser(String userId, UserInfo userInfo) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserResource userResource = usersResource.get(userId);
        
        // 获取现有用户并更新
        UserRepresentation user = userResource.toRepresentation();
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setEnabled(userInfo.isEnabled());
        user.setEmailVerified(userInfo.isEmailVerified());
        
        // 设置属性
        Map<String, List<String>> attrs = userInfo.getAttributes();
        System.out.println("[DEBUG] updateUser attributes from UI: " + attrs);
        
        if (attrs != null && !attrs.isEmpty()) {
            // 过滤掉空值
            Map<String, List<String>> filteredAttrs = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : attrs.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty() 
                    && !entry.getValue().get(0).trim().isEmpty()) {
                    filteredAttrs.put(entry.getKey(), entry.getValue());
                }
            }
            
            if (!filteredAttrs.isEmpty()) {
                user.setAttributes(filteredAttrs);
                System.out.println("[DEBUG] Setting attributes to Keycloak: " + filteredAttrs);
            }
        }

        try {
            userResource.update(user);
            System.out.println("[DEBUG] User updated successfully");
        } catch (Exception e) {
            throw new RuntimeException("更新用户失败: " + e.getMessage(), e);
        }
    }

    public void deleteUser(String userId) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        usersResource.get(userId).remove();
    }

    private UserInfo convertToUserInfo(UserRepresentation user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setEmail(user.getEmail());
        info.setFirstName(user.getFirstName());
        info.setLastName(user.getLastName());
        info.setEnabled(user.isEnabled());
        info.setEmailVerified(user.isEmailVerified());
        info.setCreatedTimestamp(user.getCreatedTimestamp());
        info.setRequiredActions(user.getRequiredActions());
        
        // 获取用户属性
        Map<String, List<String>> attrs = user.getAttributes();
        System.out.println("[DEBUG] User " + user.getUsername() + " attributes from Keycloak: " + attrs);
        
        if (attrs != null && !attrs.isEmpty()) {
            info.setAttributes(new HashMap<>(attrs));
            System.out.println("[DEBUG] Set attributes to UserInfo: " + info.getAttributes());
        } else {
            System.out.println("[DEBUG] No attributes found for user " + user.getUsername());
        }

        return info;
    }

    /**
     * 测试连接是否有效
     */
    public void testConnection() {
        try {
            // 尝试获取 realm 信息来验证连接
            keycloak.realm(realm).toRepresentation();
        } catch (Exception e) {
            throw new RuntimeException("连接测试失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (keycloak != null) {
            keycloak.close();
        }
    }
}

