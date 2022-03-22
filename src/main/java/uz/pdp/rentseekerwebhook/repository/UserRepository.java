package uz.pdp.rentseekerwebhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.rentseekerwebhook.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByChatId(String chatId);

    List<User> findByActive(boolean active);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query(value = "update users set crt_page=:page where chat_id=:chatId",nativeQuery = true)
    void changeUserPageByChatId(@Param("chatId") String chatId,@Param("page")  int page);
}
