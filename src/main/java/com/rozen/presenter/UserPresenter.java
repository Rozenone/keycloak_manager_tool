package com.rozen.presenter;

import com.rozen.constant.MessageConstants;
import com.rozen.model.UserInfo;
import com.rozen.service.KeycloakService;
import com.rozen.service.KeycloakService.PageResult;

import java.util.List;
import java.util.function.Consumer;

import static com.rozen.service.I18nManager.t;

/**
 * 用户管理的 Presenter 层
 * 负责处理用户相关的业务逻辑，连接 View 和 Service
 */
public class UserPresenter {

    private final KeycloakService keycloakService;
    private Consumer<String> onError;
    private Consumer<String> onStatusUpdate;

    public UserPresenter(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    public void setOnError(Consumer<String> onError) {
        this.onError = onError;
    }

    public void setOnStatusUpdate(Consumer<String> onStatusUpdate) {
        this.onStatusUpdate = onStatusUpdate;
    }

    /**
     * 加载用户列表（分页）
     */
    public PageResult<UserInfo> loadUsers(int page, int pageSize) {
        if (keycloakService == null) {
            return new PageResult<>(List.of(), 0, page, pageSize);
        }

        try {
            return keycloakService.getUsers(page, pageSize);
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.LOAD_USER_FAILED) + ": " + ex.getMessage());
            return new PageResult<>(List.of(), 0, page, pageSize);
        }
    }

    /**
     * 搜索用户
     */
    public PageResult<UserInfo> searchUsers(String search, String searchType, boolean exactMatch, int page, int pageSize) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return new PageResult<>(List.of(), 0, page, pageSize);
        }

        try {
            String type = t(MessageConstants.Search.ALL).equals(searchType) ? MessageConstants.SearchType.ALL :
                          "ID".equals(searchType) ? MessageConstants.SearchType.ID :
                          t(MessageConstants.User.USERNAME).equals(searchType) ? MessageConstants.SearchType.USERNAME : MessageConstants.SearchType.EMAIL;
            return keycloakService.searchUsers(search, type, exactMatch, page, pageSize);
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.SEARCH_USER_FAILED) + ": " + ex.getMessage());
            return new PageResult<>(List.of(), 0, page, pageSize);
        }
    }

    /**
     * 根据多个属性搜索用户
     */
    public PageResult<UserInfo> searchUsersByAttributes(List<String> attrNames, List<String> attrValues, int page, int pageSize) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return new PageResult<>(List.of(), 0, page, pageSize);
        }

        try {
            return keycloakService.searchUsersByMultipleAttributes(attrNames, attrValues, page, pageSize);
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.SEARCH_USER_FAILED) + ": " + ex.getMessage());
            return new PageResult<>(List.of(), 0, page, pageSize);
        }
    }

    /**
     * 根据ID获取用户信息
     */
    public UserInfo getUserById(String userId) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return null;
        }

        try {
            return keycloakService.getUserById(userId);
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.GET_USER_FAILED) + ": " + ex.getMessage());
            return null;
        }
    }

    /**
     * 创建新用户
     */
    public boolean createUser(UserInfo userInfo) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return false;
        }

        try {
            keycloakService.createUser(userInfo);
            notifyStatus(t(MessageConstants.Msg.USER_CREATE_SUCCESS));
            return true;
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.CREATE_USER_FAILED) + ": " + ex.getMessage());
            return false;
        }
    }

    /**
     * 更新用户
     */
    public boolean updateUser(String userId, UserInfo userInfo) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return false;
        }

        try {
            keycloakService.updateUser(userId, userInfo);
            notifyStatus(t(MessageConstants.Msg.USER_UPDATE_SUCCESS));
            return true;
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.UPDATE_USER_FAILED) + ": " + ex.getMessage());
            return false;
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String userId) {
        if (keycloakService == null) {
            notifyError(t(MessageConstants.Msg.CONNECT_FIRST));
            return false;
        }

        try {
            keycloakService.deleteUser(userId);
            notifyStatus(t(MessageConstants.Msg.USER_DELETE_SUCCESS));
            return true;
        } catch (Exception ex) {
            notifyError(t(MessageConstants.Msg.DELETE_USER_FAILED) + ": " + ex.getMessage());
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
