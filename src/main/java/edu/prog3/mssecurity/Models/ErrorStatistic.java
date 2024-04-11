package edu.prog3.mssecurity.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class ErrorStatistic {
    @Id
    private String _id;
    private int numValidationErrors;
    private int numAuthErrors;
    @DBRef
    private User user;


    public ErrorStatistic() {
        this.numValidationErrors = 0;
        this.numAuthErrors = 0;
        this.user = null;
    }

    public ErrorStatistic(int numValidationErrors, int numAuthErrors, User user) {
        this.numValidationErrors = numValidationErrors;
        this.numAuthErrors = numAuthErrors;
        this.user = user;
    }

    public ErrorStatistic(User user) {
        this.numValidationErrors = 0;
        this.numAuthErrors = 0;
        this.user = user;
    }

    public int getNumValidationErrors() {
        return this.numValidationErrors;
    }

    public void setNumValidationErrors(int numValidationErrors) {
        this.numValidationErrors = numValidationErrors;
    }

    public int getNumAuthErrors() {
        return this.numAuthErrors;
    }

    public void setNumAuthErrors(int numAuthErrors) {
        this.numAuthErrors = numAuthErrors;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getNumSecurityErrors() {
        return this.numValidationErrors + this.numAuthErrors;
    }
}