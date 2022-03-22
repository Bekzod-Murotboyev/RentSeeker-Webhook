package uz.pdp.rentseekerwebhook.payload;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import uz.pdp.rentseekerwebhook.util.enums.BotState;
import uz.pdp.rentseekerwebhook.util.enums.Language;
import uz.pdp.rentseekerwebhook.util.enums.Role;


@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LanStateDTO {
    Language language;
    BotState state;
    Role role;
    boolean isAdmin;
}
