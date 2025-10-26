package dataaccess;

import datamodel.RegisterUser;

import java.util.HashMap;


public class MemoryDataAccess implements DataAccess{
    private HashMap<String, RegisterUser> users = new HashMap<>();
    private HashMap<String, String> loginTokens = new HashMap<>();

    @Override
    public void saveUser(RegisterUser user) {
        users.put(user.username(), user);
    }
    @Override
    public RegisterUser getUser(String username) {
        return users.get(username);
    }
    @Override
    public void saveAuthToken(String authToken, String username) {
        loginTokens.put(authToken, username);
    }
    @Override
    public String checkAuthToken(String authToken) {
        return loginTokens.get(authToken);
    }
    @Override
    public void deleteAuthToken(String authToken) {
        loginTokens.remove(authToken);
    }
}
