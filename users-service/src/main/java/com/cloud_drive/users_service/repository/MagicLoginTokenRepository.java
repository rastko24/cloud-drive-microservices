package com.cloud_drive.users_service.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.cloud_drive.users_service.model.MagicLoginToken;

public interface MagicLoginTokenRepository extends MongoRepository<MagicLoginToken, String> {
    Optional<MagicLoginToken> findByTokenAndEmailAndUsedFalse(String token, String email);
}
