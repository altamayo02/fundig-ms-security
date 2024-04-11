package edu.prog3.mssecurity.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import edu.prog3.mssecurity.Models.ErrorStatistic;

public interface ErrorStatisticRepository extends MongoRepository<ErrorStatistic, String> {
    @Query("{'user.$id': ObjectId(?0)}")
    public ErrorStatistic getErrorStatisticByUser(String userId);
}