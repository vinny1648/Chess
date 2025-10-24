package dataaccess;

import datamodel.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, User> users = new HashMap<>();
    private ArrayList<UUID> loginTokens = new ArrayList<>();

    @Override
    public void saveUser(User user) {
        users.put(user.username(), user);
    }
    @Override
    public User getUser(String username) {
        return users.get(username);
    }
    @Override
    public void saveAuthToken(UUID authToken) {
        loginTokens.add(authToken);
    }
}
