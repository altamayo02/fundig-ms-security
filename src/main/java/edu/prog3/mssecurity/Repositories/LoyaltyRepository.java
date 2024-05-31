package edu.prog3.mssecurity.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import edu.prog3.mssecurity.Models.Loyalty;

public interface LoyaltyRepository extends MongoRepository<Loyalty, String> {
    @Query("{'user.$id': ObjectId(?0)}")
    public Loyalty getLoyaltyByUser(String userId);
}
