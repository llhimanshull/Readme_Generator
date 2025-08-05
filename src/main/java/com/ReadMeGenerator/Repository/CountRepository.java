package com.ReadMeGenerator.Repository;

import com.ReadMeGenerator.Model.Count;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountRepository extends MongoRepository<Count , String>{
}
