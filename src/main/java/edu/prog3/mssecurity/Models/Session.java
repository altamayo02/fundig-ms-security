package edu.prog3.mssecurity.Models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class Session {
    @Id
    private String _id;
    private String code;
    private boolean use = false;
    private String token;
    @DBRef
    private User user;

    private LocalDateTime createDateTime;
    private LocalDateTime expirationDateTime;

    public Session(String code, User user){
        this.code=code;
        this.user=user;

        this.createDateTime=LocalDateTime.now();
        this.expirationDateTime = LocalDateTime.now().plusMinutes(30);
    }

}
