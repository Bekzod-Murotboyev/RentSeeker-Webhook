package uz.pdp.rentseekerwebhook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.rentseekerwebhook.entity.Home;
import uz.pdp.rentseekerwebhook.entity.Like;
import uz.pdp.rentseekerwebhook.entity.User;
import uz.pdp.rentseekerwebhook.repository.LikeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {


    private final HomeService homeService;

    private final LikeRepository likeRepository;

    public void saveLike(Like like) {
        likeRepository.save(like);
    }

    public Like getLikeByHomeIdAndUserId(Home home, User user) {
        Optional<Like> like = likeRepository.findByHomeIdAndUserId(home.getId(), user.getId());
        return like.orElseGet(() -> likeRepository.save(new Like(home, user)));
    }

    public boolean isLike(String data) {
        UUID uuid;
        try {
            uuid = UUID.fromString(data);
        } catch (Exception e) {
            return false;
        }
        return likeRepository.existsById(uuid);
    }

    public Like getById(UUID id) {
        return likeRepository.findById(id).orElse(null);
    }

    public List<Home> getActiveHomesByUserId(UUID userId) {
        List<Home> homeIdList = new ArrayList<>();
        for (Like like : likeRepository.findByUserIdAndActiveTrue(userId))
            homeIdList.add(like.getHome());
        return homeIdList;
    }

    public Like changeLike(Like like) {
        like.setActive(!like.isActive());
        homeService.changeCountOfLike(like);
        return likeRepository.save(like);
    }
}
