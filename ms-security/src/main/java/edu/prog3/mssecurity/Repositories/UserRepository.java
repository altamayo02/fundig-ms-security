package edu.prog3.mssecurity.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import edu.prog3.mssecurity.Models.User;

public interface UserRepository extends MongoRepository<User, String> {
    
}
