package hu.prog3.offlinechatprog3.model;

/**
 * Centralized permission keys for group operations.
 */
public final class Permissions {
    private Permissions() {}

    public static final String ALL = "ALL";

    public static final String GROUP_SEND_MESSAGE = "GROUP_SEND_MESSAGE";
    public static final String GROUP_ADD_MEMBER = "GROUP_ADD_MEMBER";
    public static final String GROUP_REMOVE_MEMBER = "GROUP_REMOVE_MEMBER";
    public static final String GROUP_DELETE_MESSAGES = "GROUP_DELETE_MESSAGES";
    public static final String GROUP_DELETE_GROUP = "GROUP_DELETE_GROUP";
    public static final String GROUP_READ = "GROUP_READ";
}
