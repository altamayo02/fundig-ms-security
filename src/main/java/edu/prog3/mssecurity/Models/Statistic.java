package edu.prog3.mssecurity.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Statistic {
    
    @Id
    private String _id;
    private int numberErrorvalidations;
    private int numberErrorAuthentication;
    @DBRef
    private User user;

    public Statistic(){
        this.numberErrorAuthentication=0;
        this.numberErrorvalidations=0;
    }
    
    public void setErrorValidation(){
        this.numberErrorvalidations +=1;
    }

    public void setErrorAuthentication(){
        this.numberErrorAuthentication +=1;
    }
}
