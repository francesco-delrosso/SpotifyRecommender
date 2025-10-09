package org.example.client;

import javafx.beans.property.*;

public class User {
    private final StringProperty userId;
    private final StringProperty email;
    private final StringProperty status;

    public User(String userId, String email, String status) {
        this.userId = new SimpleStringProperty(userId);
        this.email = new SimpleStringProperty(email);
        this.status = new SimpleStringProperty(status);
    }

    public String getUserId() { return userId.get(); }
    public StringProperty userIdProperty() { return userId; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }
}