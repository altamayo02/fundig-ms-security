package edu.prog3.mssecurity.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import edu.prog3.mssecurity.Models.Role;

public interface RoleRepository extends MongoRepository<Role, String> {}
