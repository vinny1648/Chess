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
        String authToken = UUID.randomUUID().toString();
        dataAccess.saveAuthToken(authToken, user.username());
        return new RequestResult(user.username(), authToken);
    }
    public RequestResult login(LoginUser user) {
        if (user.username() == null || user.password() == null){
            throw new BadRequestException("Username and Password must contain characters");
        }
        var existingUser = dataAccess.getUser(user.username());
        if (existingUser == null) {
            throw new IncorrectPasswordException("Username or Password is incorrect");
        }
        if (!Objects.equals(existingUser.password(), user.password())) {
            throw new IncorrectPasswordException("Username or Password is incorrect");
        }
        String authToken = UUID.randomUUID().toString();
        dataAccess.saveAuthToken(authToken, user.username());
        return new RequestResult(user.username(), authToken);
    }
    public void logout(LogoutUser user) {
        if (user.authToken() == null) {
            throw new UnauthorizedException("No auth token");
        }
        if (dataAccess.checkAuthToken(user.authToken()) == null) {
            throw new UnauthorizedException("Session not valid");
        }
        dataAccess.deleteAuthToken(user.authToken());
    }
}
