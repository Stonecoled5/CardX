package com.cs407.cardx;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Card implements Serializable {

    private String company;
    private String occupation;
    private String email;
    private String phone;
    private String school;
    private String name;
    private String bio;
    private String avatar;

    // Constructor to create a new Card object
    public Card(String company, String occupation, String email, String phone, String school,
                String name, String bio, String avatar) {
        this.company = company;
        this.occupation = occupation;
        this.email = email;
        this.phone = phone;
        this.school = school;
        this.name = name;
        this.bio = bio;
        this.avatar = avatar;

    }
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() { return avatar; }

    public void setAvatar() { this.avatar = avatar;}

    // A static method to parse JSON data and return a list of Card objects
    public static List<Card> parseJsonData(String jsonData) {
        List<Card> cards = new ArrayList<>();

        try {
            JSONArray cardsArray = new JSONArray(jsonData);

            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject cardJson = cardsArray.getJSONObject(i);

                String company = cardJson.optString("company");
                String occupation = cardJson.optString("occupation");
                String email = cardJson.optString("email");
                String phone = cardJson.optString("phone");
                String school = cardJson.optString("school");
                String name = cardJson.optString("name");
                String bio = cardJson.optString("bio");
                String avatar = cardJson.optString("avatar");

                Card card = new Card(company, occupation, email, phone, school, name, bio, avatar);
                cards.add(card);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cards;
    }
}

