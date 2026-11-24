package com.cloud_drive.users_service.repository;

import com.cloud_drive.users_service.model.ZahtevZaKvotu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ZahtevZaKvotuRepository extends MongoRepository<ZahtevZaKvotu, String> {

    List<ZahtevZaKvotu> findByStatusOrderByKreiranAsc(String status);
    
    Optional<ZahtevZaKvotu> findByKorisnikIdAndStatus(String korisnikId, String status);
}