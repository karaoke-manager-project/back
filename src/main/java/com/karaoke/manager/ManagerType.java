package com.karaoke.manager;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Calendar;
import java.util.Date;

public enum ManagerType {
    FREE,
    PREMIUM_1DAY,
    PREMIUM_1MONTH,
    PREMIUM_1YEAR;
    @JsonValue
    public String getValue() {
        return this.name();
    }

    public Date getFinishDate(Date lastPaymentDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastPaymentDate);
        if (this == ManagerType.PREMIUM_1DAY)
            cal.add(Calendar.DAY_OF_MONTH, 1);
        if (this == ManagerType.PREMIUM_1MONTH)
            cal.add(Calendar.DAY_OF_MONTH, 30);
        if (this == ManagerType.PREMIUM_1YEAR)
            cal.add(Calendar.DAY_OF_MONTH, 365);
        return cal.getTime();
    }
}
