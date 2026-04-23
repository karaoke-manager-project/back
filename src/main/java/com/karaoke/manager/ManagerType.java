package com.karaoke.manager;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ManagerType {
    FREE,
    PREMIUM_1DAY,
    PREMIUM_1MONTH;
    @JsonValue
    public String getValue() {
        return this.name();
    }
}
