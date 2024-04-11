package edu.prog3.mssecurity.Repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import edu.prog3.mssecurity.Models.Session;

@Repository
public interface SessionRepository extends MongoRepository<Session, ObjectId> {
    Session findBy_id(ObjectId _id);
}