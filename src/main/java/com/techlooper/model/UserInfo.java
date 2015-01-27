package com.techlooper.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Created by phuonghqh on 12/12/14.
 */
public class UserInfo {

    private String id;

    @NotNull
    @Email
    private String emailAddress;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    private SocialProvider loginSource;

    private String key;

    private String username;

    private Set<SocialProvider> profileNames;

    @NotNull
    private Integer salary;

    private String profileImageUrl;

    private boolean registerVietnamworks;

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public Set<SocialProvider> getProfileNames() {
        return profileNames;
    }

    public void setProfileNames(Set<SocialProvider> profileNames) {
        this.profileNames = profileNames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public SocialProvider getLoginSource() {
        return loginSource;
    }

    public void setLoginSource(SocialProvider loginSource) {
        this.loginSource = loginSource;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isRegisterVietnamworks() {
        return registerVietnamworks;
    }

    public void setRegisterVietnamworks(boolean registerVietnamworks) {
        this.registerVietnamworks = registerVietnamworks;
    }
}