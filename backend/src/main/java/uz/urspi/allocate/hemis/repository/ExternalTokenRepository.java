package uz.urspi.allocate.hemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.urspi.allocate.hemis.entity.ExternalToken;

import java.util.Optional;

public interface ExternalTokenRepository extends JpaRepository<ExternalToken, Long> {

    Optional<ExternalToken> findByProvider(String provider);

    boolean existsByProvider(String provider);
}
