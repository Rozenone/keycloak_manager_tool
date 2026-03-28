package com.rozen.presenter;

import com.rozen.model.UserInfo;
import com.rozen.service.KeycloakService;
import com.rozen.service.KeycloakService.PageResult;

import java.util.List;
import java.util.function.Consumer;

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
            notifyError("加载用户失败: " + ex.getMessage());
            return new PageResult<>(List.of(), 0, page, pageSize);
        }
    }

    /**
     * 搜索用户
     */
    public PageResult<UserInfo> searchUsers(String search, String searchType, boolean exactMatch, int page, int pageSize) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return new PageResult<>(List.of(), 0, page, pageSize);
        }

        try {
            String type = "全部".equals(searchType) ? "all" :
                          "ID".equals(searchType) ? "id" :
                          "用户名".equals(searchType) ? "username" : "email";
            return keycloakService.searchUsers(search, type, exactMatch, page, pageSize);
        } catch (Exception ex) {
            notifyError("搜索用户失败: " + ex.getMessage());
            return new PageResult<>(List.of(), 0, page, pageSize);
        }
    }

    /**
     * 根据多个属性搜索用户
     */
    public PageResult<UserInfo> searchUsersByAttributes(List<String> attrNames, List<String> attrValues, int page, int pageSize) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return new PageResult<>(List.of(), 0, page, pageSize);
        }

        try {
            return keycloakService.searchUsersByMultipleAttributes(attrNames, attrValues, page, pageSize);
        } catch (Exception ex) {
            notifyError("搜索用户失败: " + ex.getMessage());
            return new PageResult<>(List.of(), 0, page, pageSize);
        }
    }

    /**
     * 根据ID获取用户信息
     */
    public UserInfo getUserById(String userId) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return null;
        }

        try {
            return keycloakService.getUserById(userId);
        } catch (Exception ex) {
            notifyError("获取用户信息失败: " + ex.getMessage());
            return null;
        }
    }

    /**
     * 创建新用户
     */
    public boolean createUser(UserInfo userInfo) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return false;
        }

        try {
            keycloakService.createUser(userInfo);
            notifyStatus("用户创建成功");
            return true;
        } catch (Exception ex) {
            notifyError("创建用户失败: " + ex.getMessage());
            return false;
        }
    }

    /**
     * 更新用户
     */
    public boolean updateUser(String userId, UserInfo userInfo) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return false;
        }

        try {
            keycloakService.updateUser(userId, userInfo);
            notifyStatus("用户更新成功");
            return true;
        } catch (Exception ex) {
            notifyError("更新用户失败: " + ex.getMessage());
            return false;
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(String userId) {
        if (keycloakService == null) {
            notifyError("请先连接到 Keycloak");
            return false;
        }

        try {
            keycloakService.deleteUser(userId);
            notifyStatus("用户删除成功");
            return true;
        } catch (Exception ex) {
            notifyError("删除用户失败: " + ex.getMessage());
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
