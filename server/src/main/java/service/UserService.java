package service;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import datamodel.RegistrationResult;
import datamodel.User;

import java.util.UUID;


public class UserService {

    private DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public RegistrationResult register(User user){
        if (dataAccess.getUser(user.username()) != null) {
            throw new AlreadyTakenException(user.username() + " is already taken.");
        }
        dataAccess.saveUser(user);
        UUID authToken = UUID.randomUUID();
        dataAccess.saveAuthToken(authToken);
        return new RegistrationResult(user.username(), authToken.toString());
    }
}
