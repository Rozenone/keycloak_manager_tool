package com.rozen.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserInfo {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean emailVerified;
    private Long createdTimestamp;
    private List<String> requiredActions;
    private Map<String, List<String>> attributes = new HashMap<>();

    // Keycloak Console 中默认显示的属性
    private static final Set<String> VISIBLE_ATTRIBUTES = new HashSet<>();
    static {
        VISIBLE_ATTRIBUTES.add("phone");
        VISIBLE_ATTRIBUTES.add("mobile");
        VISIBLE_ATTRIBUTES.add("department");
        VISIBLE_ATTRIBUTES.add("title");
        VISIBLE_ATTRIBUTES.add("description");
        VISIBLE_ATTRIBUTES.add("locale");
        VISIBLE_ATTRIBUTES.add("timezone");
    }

    public UserInfo() {
        this.enabled = true;
    }

    /**
     * 判断属性是否在 Keycloak Console 中可见
     */
    public static boolean isAttributeVisible(String attributeName) {
        return VISIBLE_ATTRIBUTES.contains(attributeName.toLowerCase());
    }

    /**
     * 获取可见属性
     */
    public Map<String, List<String>> getVisibleAttributes() {
        Map<String, List<String>> visible = new HashMap<>();
        if (attributes != null) {
            for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                if (isAttributeVisible(entry.getKey())) {
                    visible.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return visible;
    }

    /**
     * 获取隐藏属性
     */
    public Map<String, List<String>> getHiddenAttributes() {
        Map<String, List<String>> hidden = new HashMap<>();
        if (attributes != null) {
            for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
                if (!isAttributeVisible(entry.getKey())) {
                    hidden.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return hidden;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public Long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(Long createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public List<String> getRequiredActions() { return requiredActions; }
    public void setRequiredActions(List<String> requiredActions) { this.requiredActions = requiredActions; }

    public Map<String, List<String>> getAttributes() { return attributes; }
    public void setAttributes(Map<String, List<String>> attributes) { this.attributes = attributes; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.length() > 0 ? sb.toString() : "(未设置)";
    }

    /**
     * 获取表示名 (displayName)
     * 从属性中获取 displayName，如果没有则返回空字符串
     */
    public String getDisplayName() {
        if (attributes != null && attributes.containsKey("displayName")) {
            List<String> values = attributes.get("displayName");
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return username != null ? username : id;
    }
}
