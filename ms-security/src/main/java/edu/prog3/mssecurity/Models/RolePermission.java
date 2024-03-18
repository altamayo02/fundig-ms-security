package edu.prog3.mssecurity.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class RolePermission {
    @Id
    private String _id;
    @DBRef
    private Role role;
    @DBRef
    private Permission permission;

    public RolePermission() {}

    public String get_id() {
        return this._id;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
         this.role = role;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public void setPermission(Permission permission) {
         this.permission = permission;
    }
}
