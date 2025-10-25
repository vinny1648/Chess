package dataaccess;

import datamodel.RegisterUser;
import java.util.UUID;

public interface DataAccess {

    void saveUser(RegisterUser user);

    RegisterUser getUser(String username);

    void saveAuthToken(String username, UUID authToken);
}
