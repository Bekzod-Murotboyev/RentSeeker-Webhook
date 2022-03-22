package uz.pdp.rentseekerwebhook.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.rentseekerwebhook.entity.Home;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HomeRepository extends JpaRepository<Home, UUID> {

    List<Home> findAllByUserId(UUID user_id);

    Optional<Home> findByUserIdAndActiveFalse(UUID user_id);

    Page<Home> findAllByActiveTrue(Pageable pageable);
}
