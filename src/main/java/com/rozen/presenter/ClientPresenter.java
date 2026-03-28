package com.rozen.presenter;

import com.rozen.constant.MessageConstants;
import com.rozen.model.ClientInfo;
import com.rozen.service.KeycloakService;

import java.util.List;
import java.util.function.Consumer;

import static com.rozen.service.I18nManager.t;

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
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return List.of();
        }

        try {
            List<ClientInfo> clients = keycloakService.getClients();
            notifyStatus(t(MessageConstants.Msg.CLIENT_COUNT, clients.size()));
            return clients;
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.LOAD_CLIENT_FAILED) + ": " + ex.getMessage());
            return List.of();
        }
    }

    /**
     * 根据客户端ID获取客户端信息
     */
    public ClientInfo getClientById(String clientId) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return null;
        }

        try {
            return keycloakService.getClientByClientId(clientId);
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.GET_CLIENT_FAILED) + ": " + ex.getMessage());
            return null;
        }
    }

    /**
     * 创建新客户端
     */
    public boolean createClient(ClientInfo clientInfo) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return false;
        }

        try {
            keycloakService.createClient(clientInfo);
            notifyStatus(t(MessageConstants.Msg.CLIENT_CREATE_SUCCESS));
            return true;
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.CREATE_CLIENT_FAILED) + ": " + ex.getMessage());
            return false;
        }
    }

    /**
     * 更新客户端
     */
    public boolean updateClient(String clientId, ClientInfo clientInfo) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return false;
        }

        try {
            keycloakService.updateClient(clientId, clientInfo);
            notifyStatus(t(MessageConstants.Msg.CLIENT_UPDATE_SUCCESS));
            return true;
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.UPDATE_CLIENT_FAILED) + ": " + ex.getMessage());
            return false;
        }
    }

    /**
     * 删除客户端
     */
    public boolean deleteClient(String clientId) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return false;
        }

        try {
            keycloakService.deleteClient(clientId);
            notifyStatus(t(MessageConstants.Msg.CLIENT_DELETE_SUCCESS));
            return true;
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.DELETE_CLIENT_FAILED) + ": " + ex.getMessage());
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
