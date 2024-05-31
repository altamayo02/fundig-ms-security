package edu.prog3.mssecurity.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Loyalty {
    @Id
    private String _id;
    private int points;
    @DBRef
    private User user;

    public void exchangePoints() throws Exception {
        if (this.points < 10) {
            throw new Exception("Not enough points to exchange.");
        }
        this.points -= 10;
    }
}
