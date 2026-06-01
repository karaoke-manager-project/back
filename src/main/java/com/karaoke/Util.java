package com.karaoke;

public class Util {
  public static String extractToken(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Token inválido");
    }
    return authHeader.substring(7);
  }
}
