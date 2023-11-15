package com.cs407.cardx;

public class Card {

    private String name;
    private String title;
    private String contactInfo;
    // Additional fields like email, phone number, company, etc. can be added here

    // Constructor to create a new Card object
    public Card(String name, String title, String contactInfo) {
        this.name = name;
        this.title = title;
        this.contactInfo = contactInfo;
    }

    // Getter and setter for the name field
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and setter for the title field
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and setter for the contact information
    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    // Additional getter and setter methods for any other fields you add
    // ...

    // You might also want to override the toString() method for easy printing of Card information
    @Override
    public String toString() {
        return "Card{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                '}';
    }

    // Depending on your requirements, you could also implement methods for sharing the card information,
    // saving it to a database, etc.
}
