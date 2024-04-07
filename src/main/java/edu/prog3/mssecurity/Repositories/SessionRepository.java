package edu.prog3.mssecurity.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import edu.prog3.mssecurity.Models.Session;

public interface SessionRepository extends MongoRepository<Session, String> {

    @Query("{'Session.$id': ?0}")
    public Session getById(String sessionId);
}
