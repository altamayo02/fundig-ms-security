package edu.prog3.mssecurity.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.prog3.mssecurity.Models.Permission;

public interface PermissionRepository extends MongoRepository<Permission, String> {
    
}
