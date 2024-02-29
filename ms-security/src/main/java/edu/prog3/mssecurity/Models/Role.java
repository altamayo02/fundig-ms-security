package edu.prog3.mssecurity.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Role {
    @Id
    private String _id;
    private String name;
    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String get_id() {
        return this._id;
    }

    public void setId(String id) {
         this._id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
         this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
         this.description = description;
    }
}
