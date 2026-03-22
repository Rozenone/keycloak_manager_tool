package com.rozen.service;

import com.rozen.ui.ClientInfo;
import com.rozen.ui.UserInfo;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
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

public class KeycloakService {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakService(String serverUrl, String realm, String username, String password, boolean skipSslVerify) {
        this.realm = realm;

        try {
            KeycloakBuilder builder = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .username(username)
                    .password(password)
                    .clientId("admin-cli");

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

                // 创建自定义的 ResteasyClient，配置 SSL
                ResteasyClient client = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder())
                        .sslContext(sslContext)
                        .hostnameVerifier((hostname, session) -> true)
                        .build();

                builder.resteasyClient(client);
            }

            this.keycloak = builder.build();
        } catch (Exception e) {
            throw new RuntimeException("初始化 Keycloak 客户端失败: " + e.getMessage(), e);
        }
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

    public List<UserInfo> getUsers() {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users = usersResource.list();
        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }

    public List<UserInfo> searchUsers(String search, String searchType, boolean exactMatch) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        List<UserRepresentation> users;

        if (exactMatch) {
            // 精确匹配搜索
            if ("id".equalsIgnoreCase(searchType)) {
                // 按 ID 精确查找
                try {
                    UserRepresentation user = usersResource.get(search).toRepresentation();
                    users = user != null ? Collections.singletonList(user) : Collections.emptyList();
                } catch (Exception e) {
                    users = Collections.emptyList();
                }
            } else if ("username".equalsIgnoreCase(searchType)) {
                // 按用户名精确查找
                users = usersResource.searchByUsername(search, true);
            } else if ("email".equalsIgnoreCase(searchType)) {
                // 按邮箱精确查找
                users = usersResource.searchByEmail(search, true);
            } else {
                // 默认使用通用搜索（精确匹配）
                users = usersResource.search(search, true);
            }
        } else {
            // 模糊匹配搜索
            if ("username".equalsIgnoreCase(searchType)) {
                users = usersResource.searchByUsername(search, false);
            } else if ("email".equalsIgnoreCase(searchType)) {
                users = usersResource.searchByEmail(search, false);
            } else {
                // 通用模糊搜索（搜索用户名、邮箱、姓、名）
                users = usersResource.search(search, false);
            }
        }

        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }

    public List<UserInfo> searchUsersByAttribute(String attributeName, String attributeValue) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // 按属性搜索
        List<UserRepresentation> users = usersResource.searchByAttributes(attributeName + ":" + attributeValue);
        return users.stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
    }

    public UserInfo getUserById(String userId) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        try {
            UserRepresentation user = usersResource.get(userId).toRepresentation();
            return convertToUserInfo(user);
        } catch (Exception e) {
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
        
        if (userInfo.getAttributes() != null && !userInfo.getAttributes().isEmpty()) {
            user.setAttributes(userInfo.getAttributes());
        }

        try {
            usersResource.create(user);
        } catch (Exception e) {
            throw new RuntimeException("创建用户失败: " + e.getMessage(), e);
        }
    }

    public void updateUser(String userId, UserInfo userInfo) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserResource userResource = usersResource.get(userId);
        UserRepresentation user = userResource.toRepresentation();

        user.setUsername(userInfo.getUsername());
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setEnabled(userInfo.isEnabled());
        user.setEmailVerified(userInfo.isEmailVerified());
        
        if (userInfo.getAttributes() != null) {
            user.setAttributes(userInfo.getAttributes());
        }

        userResource.update(user);
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
        
        if (user.getAttributes() != null) {
            info.setAttributes(new HashMap<>(user.getAttributes()));
        }

        return info;
    }

    public void close() {
        if (keycloak != null) {
            keycloak.close();
        }
    }
}
