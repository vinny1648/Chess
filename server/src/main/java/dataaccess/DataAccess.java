package dataaccess;

import datamodel.RegisterUser;

public interface DataAccess {

    void saveUser(RegisterUser user);

    RegisterUser getUser(String username);

    void saveAuthToken(String authToken, String username);

    String checkAuthToken(String authToken);

    void deleteAuthToken(String authToken);
}
