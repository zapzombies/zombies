package io.github.zap.zombies.command.mapeditor;

public final class Regexes {
    public static final String OBJECT_NAME = "^([a-zA-Z0-9_ ]+)$";
    public static final String BOOLEAN = "^((true)|(false))$";
    public static final String NON_NEGATIVE_INTEGER = "^(\\d+)$";
    public static final String INTEGER = "^(-?\\d+)$";
    public static final String DOUBLE = "^(-?\\d+(\\.\\d+)?)$";
    public static final String STRING_LIST = "^([a-zA-Z0-9_ ]+,?)+([a-zA-Z0-9_ ]+)$";
}
