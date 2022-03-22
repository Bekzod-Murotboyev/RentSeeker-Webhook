package uz.pdp.rentseekerwebhook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.rentseekerwebhook.entity.Home;
import uz.pdp.rentseekerwebhook.entity.Interest;
import uz.pdp.rentseekerwebhook.entity.User;
import uz.pdp.rentseekerwebhook.repository.InterestRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterestService {


    private final HomeService homeService;

    private final InterestRepository interestRepository;

    public boolean changeVisible(Home home, User user) {
        Interest interest;
        Optional<Interest> optionalInterest = interestRepository.findByHomeIdAndUserId(home.getId(), user.getId());
        if (optionalInterest.isPresent()) {
            interest = optionalInterest.get();
            interest.setVisible(!interest.isVisible());
            if (!interest.isActive()) {
                interest.setActive(true);
                homeService.changeCountOfInterest(interest);
            }
        } else
            interest = interestRepository.save(new Interest(home, user, false));
        return interest.isVisible();
    }

    public boolean getVisible(Home home, User user) {
        Interest interest;
        Optional<Interest> optionalInterest = interestRepository.findByHomeIdAndUserId(home.getId(), user.getId());
        if (optionalInterest.isPresent()) {
            interest = optionalInterest.get();
            if (!interest.isActive()) {
                interest.setActive(true);
                homeService.changeCountOfInterest(interest);
            }
        } else
            interest =interestRepository.save(new Interest(home, user, false));
        return interest.isVisible();
    }

}
