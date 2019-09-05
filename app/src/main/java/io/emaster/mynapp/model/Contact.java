package io.emaster.mynapp.model;

public class Contact {
    public String username, status, image;

    public Contact() {}

    public Contact(String username, String status, String image) {
        this.username = username;
        this.status = status;
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
