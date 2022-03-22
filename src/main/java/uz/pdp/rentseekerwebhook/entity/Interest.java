package uz.pdp.rentseekerwebhook.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.pdp.rentseekerwebhook.entity.base.BaseModel;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
public class Interest extends BaseModel {

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    Home home;

    @ManyToOne
    User user;

    boolean visible;

    {
        visible = false;
        active = false;
    }
}
