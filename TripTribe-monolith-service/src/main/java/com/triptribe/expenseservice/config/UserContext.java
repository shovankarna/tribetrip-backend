package com.triptribe.expenseservice.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class UserContext {

    private static final ThreadLocal<UserDetails> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(UserDetails user) {
        currentUser.set(user);
    }

    public static void clear() {
        currentUser.remove();
    }

    public static UserDetails getCurrentUser() {
        return currentUser.get();
    }

    public static String getUserId() {
        UserDetails user = currentUser.get();
        return user != null ? user.getUserId() : null;
    }

    public static String getAuthToken() {
        UserDetails user = currentUser.get();
        return user != null ? user.getAccessToken() : null;
    }

    @Getter
    @Setter
    @ToString
    @lombok.AllArgsConstructor
    public static class UserDetails {
        private String userId;
        private String email;
        private String firstName;
        private String lastName;
        private String username;
        private String accessToken; // Added to store raw token
    }
}
