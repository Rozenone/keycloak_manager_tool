package com.rozen.presenter;

import com.rozen.model.ClientInfo;
import com.rozen.service.KeycloakService;

import java.util.List;
import java.util.function.Consumer;

/**
 * 客户端管理的 Presenter 层
 * 负责处理客户端相关的业务逻辑，连接 View 和 Service
 */

public class ClientPresenter {

    private final KeycloakService keycloakService;
    private Consumer<String> onError;
    private Consumer<String> onStatusUpdate;

    public ClientPresenter(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    public void setOnError(Consumer<String> onError) {
        this.onError = onError;
    }

    public void setOnStatusUpdate(Consumer<String> onStatusUpdate) {
        this.onStatusUpdate = onStatusUpdate;
    }

    /**
     * 加载所有客户端
     */
    public List<ClientInfo> loadClients() {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return List.of();
        }

        try {
            List<ClientInfo> clients = keycloakService.getClients();
            notifyStatus("共 " + clients.size() + " 个客户端");
            return clients;
        } catch (Exception ex) {
            notifyError("加载客户端失败: " + ex.getMessage());
            return List.of();
        }
    }

    /**
     * 根据客户端ID获取客户端信息
     */
    public ClientInfo getClientById(String clientId) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return null;
        }

        try {
            return keycloakService.getClientByClientId(clientId);
        } catch (Exception ex) {
            notifyError("获取客户端信息失败: " + ex.getMessage());
            return null;
        }
    }

    /**
     * 创建新客户端
     */
    public boolean createClient(ClientInfo clientInfo) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return false;
        }

        try {
            keycloakService.createClient(clientInfo);
            notifyStatus("客户端创建成功");
            return true;
        } catch (Exception ex) {
            notifyError("创建客户端失败: " + ex.getMessage());
            return false;
        }
    }

    /**
     * 更新客户端
     */
    public boolean updateClient(String clientId, ClientInfo clientInfo) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return false;
        }

        try {
            keycloakService.updateClient(clientId, clientInfo);
            notifyStatus("客户端更新成功");
            return true;
        } catch (Exception ex) {
            notifyError("更新客户端失败: " + ex.getMessage());
            return false;
        }
    }

    /**
     * 删除客户端
     */
    public boolean deleteClient(String clientId) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return false;
        }

        try {
            keycloakService.deleteClient(clientId);
            notifyStatus("客户端删除成功");
            return true;
        } catch (Exception ex) {
            notifyError("删除客户端失败: " + ex.getMessage());
            return false;
        }
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
}
