package com.karaoke.manager;

import java.util.Date;

public record ManagerResponse(String email, String id, ManagerType type, Date premiumLastPayment) {
}