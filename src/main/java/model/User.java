package model;

public class User {
    private int userId;
    private String fullname;
    private String email;
    private String password;
    private String address;
    private String phone;

    public User() {
    }

    public User(int userId, String fullname, String email, String password, String address, String phone) {
        this.userId = userId;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phone = phone;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
