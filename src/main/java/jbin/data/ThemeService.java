package jbin.data;

import jbin.dao.ThemeDAO;
import jbin.entity.ThemeEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ThemeService {
    private final ThemeDAO themeDAO;
    private final UserService userService;

    public ThemeService(ThemeDAO themeDAO, UserService userService) {
        this.themeDAO = themeDAO;
        this.userService = userService;
    }

    public Optional<UUID> create(String userName) {
        var user = userService.findByName(userName);
        if (user.isEmpty()) return Optional.empty();
        return themeDAO.insert(ThemeEntity.builder()
                .name("Edit me")
                .foregroundColor("#ffffff")
                .backgroundColor("#000000")
                .owner(user.get().id())
                .build());
    }

    public List<ThemeEntity> getAll() {
        return themeDAO.getAll();
    }

    public boolean update(ThemeEntity theme, String user) {
       if (isAllowedToEdit(theme, user)) {
           themeDAO.update(theme);
           return true;
       } else {
           return false;
       }
    }

    public boolean delete(UUID id, String user) {
        if (user == null) return false;
        return themeDAO.delete(id);
    }

    public Optional<ThemeEntity> getById(UUID id) {
        return themeDAO.getById(id);
    }

    private boolean isAllowedToEdit(ThemeEntity theme, String user) {
        if (user == null) return false;
        var dbUser = userService.findByName(user);
        return dbUser.isPresent() && dbUser.get().id().equals(theme.owner());
    }
}
