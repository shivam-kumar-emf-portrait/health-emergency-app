package com.lifeline.emergency.dto;

public class UserRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private Integer age;
    private String gender;
    private String knownConditions;

    public UserRegisterRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getKnownConditions() { return knownConditions; }
    public void setKnownConditions(String knownConditions) { this.knownConditions = knownConditions; }
}

