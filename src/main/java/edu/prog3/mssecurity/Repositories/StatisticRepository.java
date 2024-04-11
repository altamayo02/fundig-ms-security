package edu.prog3.mssecurity.Repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import edu.prog3.mssecurity.Models.Statistic;

@Repository
public interface StatisticRepository extends MongoRepository<Statistic, ObjectId> {
    @Query(value = "{$sort: {numberErrorvalidations: -1}, $limit: 1}")
    Statistic findTopByOrderBynumberErrorvalidations();

    @Query(value = "{$sort: {numberErrorAuthentication: -1}, $limit: 1}")
    Statistic findTopByOrderBynumberErrorAuthentication();

    @Query("'user.$id': ObjectId(?0)")
    Statistic findByUser(String id);
}