package model;

import java.io.*;
public interface LoginService {

    //checks if the credential file exists
    boolean credentialsExist();

    //username can only contain lowercase letters
    boolean isValidUsername(String username);

    //password must be at least 5 characters long
    boolean isValidPassword(String password);

    //creates or overwrites the credential file
    void createCredentials(String username, String password) throws IOException;

    //checks if the username/pw entered by the user matches the stored username/pw
    boolean login(String username, String password) throws IOException;

    //lets the user change their password - they need to know their current password
    void changePassword(
            String username,
            String currentPassword,
            String newPassword,
            String confirmPassword
    ) throws IOException;

    void addUser(String username) throws IOException;

    boolean usernameExists(String username) throws IOException;

    boolean hasNoPassword(String username) throws IOException;

}
