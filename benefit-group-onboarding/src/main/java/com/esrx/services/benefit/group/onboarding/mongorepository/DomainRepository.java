package com.esrx.services.benefit.group.onboarding.mongorepository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.esrx.services.benefit.group.onboarding.mongo.Domain;

// No need implementation, just one interface, and you have CRUD, thanks Spring Data
@Repository
public interface DomainRepository extends MongoRepository<Domain, Long> {

    Domain findFirstByDomain(String domain);

    Domain findByDomainAndDisplayAds(String domain, boolean displayAds);

    //Supports native JSON query string
    @Query("{domain:'?0'}")
    Domain findCustomByDomain(String domain);

    @Query("{domain: { $regex: ?0 } })")
    List<Domain> findCustomByRegExDomain(String domain);
    
   // @Query("select a from Domain a where a.domain = ?1")
 //   @Query("{$where : 'this.domain == ?0 && this.effdate < new Date() && this.termDate >= new Date() '}")
//    /@Query("{'arrived': {$gte: ?0, $lte:?1 }}")
    @Query("{'domain':?0 , 'effdate': {$lt: ?1}, 'termDate': {$gte : ?1}}")
    List<Domain> findByDomaincurrent(String domainValue, Date date);

}