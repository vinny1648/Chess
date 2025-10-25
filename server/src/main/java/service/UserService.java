package service;
import dataaccess.*;
import datamodel.*;

import java.util.Objects;
import java.util.UUID;


public class UserService {

    private DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public RequestResult register(RegisterUser user) {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new BadRequestException("Username, Password, and Email must contain characters");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new AlreadyTakenException(user.username() + " is already taken.");
        }
        dataAccess.saveUser(user);
        UUID authToken = UUID.randomUUID();
        dataAccess.saveAuthToken(user.username(), authToken);
        return new RequestResult(user.username(), authToken.toString());
    }
    public RequestResult login(LoginUser user) {
        if (user.username() == null || user.password() == null) {
            throw new BadRequestException("User not registered");
        }
        if (!Objects.equals(dataAccess.getUser(user.username()).password(), user.password())) {
            throw new IncorrectPasswordException("Incorrect Password");
        }
        UUID authToken = UUID.randomUUID();
        dataAccess.saveAuthToken(user.username(), authToken);
        return new RequestResult(user.username(), authToken.toString());
    }
}
