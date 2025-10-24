package dataaccess;

import datamodel.User;
import java.util.UUID;

public interface DataAccess {

    void saveUser(User user);

    User getUser(String username);

    void saveAuthToken(UUID authToken);
}
