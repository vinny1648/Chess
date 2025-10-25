package dataaccess;

import datamodel.RegisterUser;

import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, RegisterUser> users = new HashMap<>();
    private HashMap<String, UUID> loginTokens = new HashMap<>();

    @Override
    public void saveUser(RegisterUser user) {
        users.put(user.username(), user);
    }
    @Override
    public RegisterUser getUser(String username) {
        return users.get(username);
    }
    @Override
    public void saveAuthToken(String username, UUID authToken) {
        loginTokens.put(username, authToken);
    }
}
