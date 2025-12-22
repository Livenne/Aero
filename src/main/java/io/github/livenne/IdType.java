package io.github.livenne;

public enum IdType {
    AUTO("AUTO_INCREMENT"),UUID("DEFAULT (UUID())");
    private final String value;
    IdType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
