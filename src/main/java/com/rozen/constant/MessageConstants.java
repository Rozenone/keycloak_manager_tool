package com.rozen.constant;

import com.rozen.service.I18nManager;

/**
 * 国际化消息常量类
 * 集中管理所有翻译键，避免硬编码字符串
 * 所有内部类实现 {@link I18nManager.MessageConstant} 接口，支持类型安全的翻译调用
 * 
 * @author Rozenone
 * @version 1.0
 */
public final class MessageConstants {

    private MessageConstants() {
        // 工具类，禁止实例化
    }

    /**
     * 基础常量接口实现
     */
    private static final class Constant implements I18nManager.MessageConstant {
        private final String key;
        
        Constant(String key) {
            this.key = key;
        }
        
        @Override
        public String getKey() {
            return key;
        }
    }

    // ==================== 应用信息 ====================
    public static final class App {
        public static final String TITLE = "app.title";
        public static final String VERSION = "app.version";
    }

    // ==================== 按钮文本 ====================
    public static final class Button {
        public static final String OK = "button.ok";
        public static final String CANCEL = "button.cancel";
        public static final String SAVE = "button.save";
        public static final String DELETE = "button.delete";
        public static final String EDIT = "button.edit";
        public static final String SEARCH = "button.search";
        public static final String CONNECT = "button.connect";
        public static final String TEST = "button.test";
        public static final String EXIT = "button.exit";
        public static final String CLOSE = "button.close";
        public static final String SAVE_CURRENT_CONFIG = "button.saveCurrentConfig";
        public static final String SAVE_AS_NEW_CONFIG = "button.saveAsNewConfig";
        public static final String SWITCH_CONFIG = "button.switchConfig";
        public static final String NEXT_PAGE = "button.nextPage";
        public static final String LAST_PAGE = "button.lastPage";
        public static final String REFRESH_LIST = "button.refreshList";
        public static final String ADD_USER = "button.addUser";
        public static final String ADD_CLIENT = "button.addClient";
        public static final String CLEAR = "button.clear";
        public static final String ADD_ATTR = "button.addAttr";
        public static final String DELETE_SELECTED = "button.deleteSelected";
    }

    // ==================== 菜单文本 ====================
    public static final class Menu {
        public static final String FILE = "menu.file";
        public static final String FILE_EXIT = "menu.file.exit";
        public static final String HELP = "menu.help";
        public static final String HELP_GITHUB = "menu.help.github";
        public static final String HELP_ABOUT = "menu.help.about";
        public static final String LANGUAGE = "menu.language";
    }

    // ==================== Tab 标签 ====================
    public static final class Tab {
        public static final String CLIENT = "tab.client";
        public static final String USER = "tab.user";
        public static final String CONFIG = "tab.config";
    }

    // ==================== 配置对话框 ====================
    public static final class DialogConfig {
        public static final String TITLE = "dialog.config.title";
        public static final String SELECT = "dialog.config.select";
        public static final String NEW_CONFIG = "dialog.config.new";
        public static final String BASIC = "dialog.config.basic";
        public static final String AUTH = "dialog.config.auth";
        public static final String PROXY = "dialog.config.proxy";
        public static final String SSL = "dialog.config.ssl";
        public static final String USERNAME_PASSWORD = "dialog.config.usernamePassword";
        public static final String CLIENT_CREDENTIALS = "dialog.config.clientCredentials";
        public static final String USE_PROXY = "dialog.config.useProxy";
        public static final String ENABLE_PROXY = "dialog.config.enableProxy";
        public static final String SKIP_SSL = "dialog.config.skipSsl";
        public static final String SKIP_SSL_HINT = "dialog.config.skipSslHint";
        public static final String NAME = "dialog.config.name";
        public static final String SERVER_URL = "dialog.config.serverUrl";
        public static final String REALM = "dialog.config.realm";
        public static final String AUTH_TYPE = "dialog.config.authType";
        public static final String USERNAME = "dialog.config.username";
        public static final String PASSWORD = "dialog.config.password";
        public static final String CLIENT_ID = "dialog.config.clientId";
        public static final String CLIENT_SECRET = "dialog.config.clientSecret";
        public static final String PROXY_PROTOCOL = "dialog.config.proxyProtocol";
        public static final String PROXY_HOST = "dialog.config.proxyHost";
        public static final String PROXY_PORT = "dialog.config.proxyPort";
        public static final String EDIT = "dialog.config.edit";
        public static final String DELETE = "dialog.config.delete";
        public static final String EDIT_CONFIG = "dialog.config.editConfig";
        public static final String NAME_TOOLTIP = "dialog.config.nameTooltip";
        public static final String SERVER = "dialog.config.server";
        public static final String USERNAME_LABEL = "dialog.config.usernameLabel";
        public static final String PASSWORD_LABEL = "dialog.config.passwordLabel";
        public static final String CLIENT_ID_LABEL = "dialog.config.clientIdLabel";
        public static final String CLIENT_SECRET_LABEL = "dialog.config.clientSecretLabel";
    }

    // ==================== 认证类型 ====================
    public static final class Auth {
        public static final String USERNAME_PASSWORD = "auth.username_password";
        public static final String CLIENT_CREDENTIALS = "auth.client_credentials";
    }

    // ==================== 客户端管理 ====================
    public static final class Client {
        public static final String ID = "client.id";
        public static final String NAME = "client.name";
        public static final String DESCRIPTION = "client.description";
        public static final String ENABLED = "client.enabled";
        public static final String PROTOCOL = "client.protocol";
        public static final String ROOT_URL = "client.rootUrl";
        public static final String REDIRECT_URIS = "client.redirectUris";
        public static final String WEB_ORIGINS = "client.webOrigins";
        public static final String ADD = "client.add";
        public static final String EDIT = "client.edit";
        public static final String DELETE = "client.delete";
        public static final String DETAIL = "client.detail";
    }

    // ==================== 用户管理 ====================
    public static final class User {
        public static final String ID = "user.id";
        public static final String USERNAME = "user.username";
        public static final String EMAIL = "user.email";
        public static final String FIRST_NAME = "user.firstName";
        public static final String LAST_NAME = "user.lastName";
        public static final String ENABLED = "user.enabled";
        public static final String EMAIL_VERIFIED = "user.emailVerified";
        public static final String CREATED = "user.created";
        public static final String REQUIRED_ACTIONS = "user.requiredActions";
        public static final String ATTRIBUTES = "user.attributes";
        public static final String BASIC_INFO = "user.basicInfo";
        public static final String ATTR_NAME = "user.attrName";
        public static final String ATTR_VALUE = "user.attrValue";
        public static final String ALL_ATTRIBUTES = "user.allAttributes";
        public static final String NO_ATTRIBUTES = "user.noAttributes";
        public static final String FULL_NAME = "user.fullName";
        public static final String EXACT_MATCH = "user.exactMatch";
        public static final String PAGE = "user.page";
        public static final String PAGE_SIZE = "user.pageSize";
        public static final String TOTAL = "user.total";
        public static final String DISPLAY_NAME = "user.displayName";
        public static final String KEYWORD = "user.keyword";
        public static final String PAGE_INFO = "user.pageInfo";
        public static final String ADD = "user.add";
        public static final String EDIT = "user.edit";
        public static final String DELETE = "user.delete";
        public static final String DETAIL = "user.detail";
    }

    // ==================== 搜索类型 ====================
    public static final class Search {
        public static final String ALL = "search.all";
        public static final String USERNAME = "search.username";
        public static final String EMAIL = "search.email";
        public static final String ID = "search.id";
        public static final String ATTRIBUTE = "search.attribute";
    }

    // ==================== UI 标签 ====================
    public static final class Label {
        public static final String SEARCH_TYPE = "label.searchType";
        public static final String EXACT_MATCH = "label.exactMatch";
        public static final String PER_PAGE = "label.perPage";
        public static final String KEYWORD = "label.keyword";
        public static final String GOTO_PAGE = "label.gotoPage";
        public static final String PAGE = "label.page";
        public static final String ATTR_CONDITIONS = "label.attrConditions";
        public static final String ATTR_NAME = "label.attrName";
        public static final String ATTR_VALUE = "label.attrValue";
        public static final String ADD_CONDITION = "label.addCondition";
        public static final String CLEAR_CONDITIONS = "label.clearConditions";
        public static final String GO = "label.go";
    }

    // ==================== 状态文本 ====================
    public static final class Status {
        public static final String READY = "status.ready";
        public static final String CONNECTED = "status.connected";
        public static final String DISCONNECTED = "status.disconnected";
        public static final String CONNECTING = "status.connecting";
        public static final String ERROR = "status.error";
        public static final String ENABLED = "status.enabled";
        public static final String DISABLED = "status.disabled";
        public static final String YES = "status.yes";
        public static final String NO = "status.no";
        public static final String CONFIG_INCOMPLETE = "status.configIncomplete";
        public static final String USERNAME_PASSWORD_INCOMPLETE = "status.usernamePasswordIncomplete";
        public static final String CLIENT_CREDENTIALS_INCOMPLETE = "status.clientCredentialsIncomplete";
        public static final String CONNECTED_TO = "status.connectedTo";
        public static final String AUTO_CONNECT_FAILED = "status.autoConnectFailed";
        public static final String TESTING_CONNECTION = "status.testingConnection";
        public static final String CONNECTION_TEST_SUCCESS = "status.connectionTestSuccess";
        public static final String CONNECTION_TEST_FAILED = "status.connectionTestFailed";
    }

    // ==================== 消息提示 ====================
    public static final class Msg {
        public static final String ERROR = "msg.error";
        public static final String WARNING = "msg.warning";
        public static final String CONFIRM = "msg.confirm";
        public static final String SUCCESS = "msg.success";
        public static final String CONFIRM_DELETE = "msg.confirmDelete";
        public static final String NO_SELECTION = "msg.noSelection";
        public static final String CONNECTION_FAILED = "msg.connectionFailed";
        public static final String CONNECTION_SUCCESS = "msg.connectionSuccess";
        public static final String RESTART_CONFIRM = "msg.restartConfirm";
        public static final String RESTART_FAILED = "msg.restartFailed";
        public static final String BROWSER_OPEN_FAILED = "msg.browserOpenFailed";
        public static final String ERROR_INFO = "msg.errorInfo";
        public static final String STACK_TRACE = "msg.stackTrace";
        public static final String CONNECT_FIRST = "msg.connectFirst";
        public static final String CLIENT_COUNT = "msg.clientCount";
        public static final String USER_COUNT = "msg.userCount";
        public static final String LOAD_CLIENT_FAILED = "msg.loadClientFailed";
        public static final String GET_CLIENT_FAILED = "msg.getClientFailed";
        public static final String CLIENT_CREATE_SUCCESS = "msg.clientCreateSuccess";
        public static final String CREATE_CLIENT_FAILED = "msg.createClientFailed";
        public static final String CLIENT_UPDATE_SUCCESS = "msg.clientUpdateSuccess";
        public static final String UPDATE_CLIENT_FAILED = "msg.updateClientFailed";
        public static final String CLIENT_DELETE_SUCCESS = "msg.clientDeleteSuccess";
        public static final String DELETE_CLIENT_FAILED = "msg.deleteClientFailed";
        public static final String LOAD_USER_FAILED = "msg.loadUserFailed";
        public static final String SEARCH_USER_FAILED = "msg.searchUserFailed";
        public static final String GET_USER_FAILED = "msg.getUserFailed";
        public static final String USER_CREATE_SUCCESS = "msg.userCreateSuccess";
        public static final String CREATE_USER_FAILED = "msg.createUserFailed";
        public static final String USER_UPDATE_SUCCESS = "msg.userUpdateSuccess";
        public static final String UPDATE_USER_FAILED = "msg.updateUserFailed";
        public static final String USER_DELETE_SUCCESS = "msg.userDeleteSuccess";
        public static final String DELETE_USER_FAILED = "msg.deleteUserFailed";
        public static final String NO_CONFIG_TO_CONNECT = "msg.noConfigToConnect";
        public static final String NO_CONFIG_TO_SAVE = "msg.noConfigToSave";
        public static final String CONFIG_SAVED = "msg.configSaved";
        public static final String NEW_CONFIG_SAVED = "msg.newConfigSaved";
        public static final String ENTER_NEW_CONFIG_NAME = "msg.enterNewConfigName";
        public static final String SWITCH_CONFIG_CONFIRM = "msg.switchConfigConfirm";
        public static final String SELECT_CLIENT_FIRST = "msg.selectClientFirst";
        public static final String CONNECTED_TO_SERVER = "msg.connectedToServer";
        public static final String CONNECTION_TEST_SUCCESS = "msg.connectionTestSuccess";
        public static final String CONNECTION_TEST_FAILED = "msg.connectionTestFailed";
        public static final String SAVE_FAILED = "msg.saveFailed";
        public static final String DELETE_FAILED = "msg.deleteFailed";
        public static final String SAVE_SUCCESS = "msg.saveSuccess";
        public static final String DELETE_SUCCESS = "msg.deleteSuccess";
        public static final String LOAD_FAILED = "msg.loadFailed";
        public static final String UPDATE_FAILED = "msg.updateFailed";
        public static final String CREATE_FAILED = "msg.createFailed";
        public static final String HINT = "msg.hint";
        public static final String SELECT_CONFIG = "msg.selectConfig";
        public static final String CONFIRM_DELETE_CONFIG = "msg.confirmDeleteConfig";
        public static final String LOAD_CONFIG_FAILED = "msg.loadConfigFailed";
        public static final String CREATE_DIR_FAILED = "msg.createDirFailed";
        public static final String SELECT_USER_FIRST = "msg.selectUserFirst";
        public static final String CONFIRM_DELETE_USER = "msg.confirmDeleteUser";
        public static final String CONFIRM_DELETE_CLIENT = "msg.confirmDeleteClient";
        public static final String ADD_ATTR_CONDITION = "msg.addAttrCondition";
        public static final String ATTR_NAME_REQUIRED = "msg.attrNameRequired";
        public static final String MAX_ATTR_CONDITIONS = "msg.maxAttrConditions";
    }

    // ==================== 验证提示 ====================
    public static final class Validation {
        public static final String REQUIRED = "validation.required";
        public static final String INVALID_URL = "validation.invalidUrl";
        public static final String INVALID_PORT = "validation.invalidPort";
        public static final String CLIENT_ID_REQUIRED = "validation.clientIdRequired";
        public static final String USERNAME_REQUIRED = "validation.usernameRequired";
        public static final String INVALID_PAGE = "validation.invalidPage";
        public static final String SERVER_URL_AND_REALM_REQUIRED = "validation.serverUrlAndRealmRequired";
        public static final String USERNAME_AND_PASSWORD_REQUIRED = "validation.usernameAndPasswordRequired";
        public static final String CLIENT_ID_AND_SECRET_REQUIRED = "validation.clientIdAndSecretRequired";
        public static final String PROXY_HOST_REQUIRED = "validation.proxyHostRequired";
        public static final String NAME_REQUIRED = "validation.nameRequired";
    }

    // ==================== 配置属性键名（用于配置文件）====================
    public static final class ConfigKey {
        public static final String SERVER_URL = "serverUrl";
        public static final String REALM = "realm";
        public static final String AUTH_TYPE = "authType";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String USE_PROXY = "useProxy";
        public static final String PROXY_PROTOCOL = "proxyProtocol";
        public static final String PROXY_HOST = "proxyHost";
        public static final String PROXY_PORT = "proxyPort";
        public static final String SKIP_SSL_VERIFY = "skipSslVerify";
    }

    // ==================== 搜索类型键名 ====================
    public static final class SearchType {
        public static final String ID = "id";
        public static final String USERNAME = "username";
        public static final String EMAIL = "email";
        public static final String ALL = "all";
    }

    // ==================== 对话框通用 ====================
    public static final class Dialog {
        public static final String DISPLAY_MODE = "dialog.displayMode";
        public static final String TABLE_MODE = "dialog.tableMode";
        public static final String TEXT_MODE = "dialog.textMode";
        public static final String PROPERTY = "dialog.property";
        public static final String VALUE = "dialog.value";
        public static final String NOT_SET = "dialog.notSet";
        public static final String CONSOLE_URL = "dialog.consoleUrl";
        public static final String URL_TOOLTIP = "dialog.urlTooltip";
        public static final String NONE = "dialog.none";
        public static final String COPY_HINT = "dialog.copyHint";
        public static final String CONNECTION_TEST_RESULT = "dialog.connectionTestResult";
        public static final String SAVE_NEW_CONFIG = "dialog.saveNewConfig";
        public static final String CONFIRM_SWITCH = "dialog.confirmSwitch";
    }

    // ==================== 关于对话框 ====================
    public static final class About {
        public static final String TITLE = "about.title";
        public static final String DESCRIPTION = "about.description";
        public static final String FEATURES = "about.features";
        public static final String FEATURE1 = "about.feature1";
        public static final String FEATURE2 = "about.feature2";
        public static final String FEATURE3 = "about.feature3";
        public static final String FEATURE4 = "about.feature4";
        public static final String AUTHOR = "about.author";
        public static final String COPYRIGHT = "about.copyright";
    }

    // ==================== 连接结果信息 ====================
    public static final class Connection {
        public static final String SUCCESS_TITLE = "connection.successTitle";
        public static final String SUCCESS_SERVER = "connection.successServer";
        public static final String SUCCESS_REALM = "connection.successRealm";
        public static final String SUCCESS_AUTH_TYPE = "connection.successAuthType";
        public static final String SUCCESS_PROXY = "connection.successProxy";
        public static final String FAILED_TITLE = "connection.failedTitle";
        public static final String FAILED_ERROR_INFO = "connection.failedErrorInfo";
        public static final String TEST_SUCCESS_TITLE = "connection.testSuccessTitle";
        public static final String TEST_FAILED_TITLE = "connection.testFailedTitle";
    }
}
