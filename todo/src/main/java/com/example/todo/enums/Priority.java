package com.example.todo.enums;

public enum Priority {
    HIGH("高", "priority-high"),
    MEDIUM("中", "priority-medium"),
    LOW("低", "priority-low");

    private final String displayName;
    private final String cssClass;

    Priority(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }
}
