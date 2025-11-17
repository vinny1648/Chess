package service;
import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;


public class UserService {

    private DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void checkAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new UnauthorizedException("No auth token");
        }
        if (dataAccess.checkAuthToken(authToken) == null) {
            throw new UnauthorizedException("Session not valid");
        }
    }
    void storeUserPassword(UserData user, String clearTextPassword) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());

        // write the hashed password in database along with the user's other information
        UserData hashedUser = new UserData(user.username(), hashedPassword, user.email());
        dataAccess.saveUser(hashedUser);
    }

    public AuthData register(model.UserData user) throws DataAccessException {
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new BadRequestException("Username, Password, and Email must contain characters");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new AlreadyTakenException(user.username() + " is already taken.");
        }
        storeUserPassword(user, user.password());
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        dataAccess.saveAuthToken(authData);
        return authData;
    }
    boolean verifyUser(String username, String providedClearTextPassword, UserData existingUser) {
        // read the previously hashed password from the database
        var hashedPassword = existingUser.password();

        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }
    public AuthData login(LoginUser user) throws DataAccessException {
        if (user.username() == null || user.password() == null){
            throw new BadRequestException("Username and Password must contain characters");
        }
        var existingUser = dataAccess.getUser(user.username());
        if (existingUser == null) {
            throw new IncorrectPasswordException("Username or Password is incorrect");
        }
        if (!verifyUser(user.username(), user.password(), existingUser)) {
            throw new IncorrectPasswordException("Username or Password is incorrect");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        dataAccess.saveAuthToken(authData);
        return authData;
    }
    public void logout(String authToken) throws DataAccessException {
        checkAuth(authToken);
        dataAccess.deleteAuthToken(authToken);
    }
}
