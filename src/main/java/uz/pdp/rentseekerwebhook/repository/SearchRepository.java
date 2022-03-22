package uz.pdp.rentseekerwebhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.rentseekerwebhook.entity.Search;

import java.util.Optional;
import java.util.UUID;

public interface SearchRepository extends JpaRepository<Search, UUID> {
    Optional<Search> findByUserId(UUID user_id);
}
