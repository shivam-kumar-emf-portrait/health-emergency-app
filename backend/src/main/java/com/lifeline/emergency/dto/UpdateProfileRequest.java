package com.lifeline.emergency.dto;

public class UpdateProfileRequest {
    private String name;
    private String phone;
    private String knownConditions;
    private Integer age;
    private String gender;

    public UpdateProfileRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getKnownConditions() { return knownConditions; }
    public void setKnownConditions(String knownConditions) { this.knownConditions = knownConditions; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}

