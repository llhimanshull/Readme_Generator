package com.ReadMeGenerator.Repository;

import com.ReadMeGenerator.Model.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<Users , String > {

}
