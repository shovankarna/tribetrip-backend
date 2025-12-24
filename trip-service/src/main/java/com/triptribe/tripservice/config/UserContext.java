package com.triptribe.tripservice.config;

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

    public static String getEmail() {
        UserDetails user = currentUser.get();
        return user != null ? user.getEmail() : null;
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
    }
}
