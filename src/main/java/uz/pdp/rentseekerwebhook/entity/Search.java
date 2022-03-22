package uz.pdp.rentseekerwebhook.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import uz.pdp.rentseekerwebhook.entity.base.BaseModel;
import uz.pdp.rentseekerwebhook.util.enums.District;
import uz.pdp.rentseekerwebhook.util.enums.HomeStatus;
import uz.pdp.rentseekerwebhook.util.enums.HomeType;
import uz.pdp.rentseekerwebhook.util.enums.Region;

import javax.persistence.*;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Search extends BaseModel {
    @OneToOne
    User user;

    @Enumerated(value = EnumType.STRING)
    Region region;

    @Enumerated(value = EnumType.STRING)
    District district;

    @Enumerated(value = EnumType.STRING)
    HomeStatus status;

    @Enumerated(value = EnumType.STRING)
    HomeType homeType;

    int numberOfRooms = -1;
    double minPrice = -1;
    double maxPrice = -1;

    public Search(User user) {
        this.user = user;
    }
}
