import model.CredentialManager;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CredentialManagerTest {

    @Test
    void validUsernameOnlyAllowsLowercaseLetters() {
        CredentialManager manager = new CredentialManager(new File("testAuth/credentials.txt"));

        assertTrue(manager.isValidUsername("agent"));
        assertTrue(manager.isValidUsername("kevin"));

        assertTrue(manager.isValidUsername("Agent"));
        assertTrue(manager.isValidUsername("agent1"));
        assertTrue(manager.isValidUsername("agent_name"));
        assertTrue(manager.isValidUsername("agent-name"));
        assertTrue(manager.isValidUsername("can"));

        assertFalse(manager.isValidUsername("agent$"));
        assertFalse(manager.isValidUsername("va"));
        assertFalse(manager.isValidUsername(""));
        assertFalse(manager.isValidUsername(null));
    }//end of lowercase username test

    @Test
    void validPasswordRequiresAtLeastFiveCharactersAndUpperAndLowercase() {
        CredentialManager manager = new CredentialManager(new File("testAuth/credentials.txt"));

        assertTrue(manager.isValidPassword("Password"));
        assertTrue(manager.isValidPassword("Pass1"));
        assertTrue(manager.isValidPassword("aB123"));

        assertFalse(manager.isValidPassword("password")); // no uppercase
        assertFalse(manager.isValidPassword("ABCDE"));    // no lowercase
        assertFalse(manager.isValidPassword("abcde"));    // no uppercase
        assertFalse(manager.isValidPassword("Abcd"));     // less than 5 characters
        assertFalse(manager.isValidPassword(""));
        assertFalse(manager.isValidPassword(null));
    }

    @Test
    void credentialsExistReturnsFalseWhenFileMissing() {
        CredentialManager manager = new CredentialManager(new File("testAuth/missing.txt"));

        assertFalse(manager.credentialsExist());
    }//end of checking credential file

    @Test
    void createCredentialsCreatesCredentialFile() throws IOException {
        File testFile = new File("testAuth/createCredentialsTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);

        manager.createCredentials("agent", "Abcde");

        assertTrue(testFile.exists());
    }//end of creating credential file

    @Test
    void loginReturnsTrueForCorrectCredentials() throws IOException {
        File testFile = new File("testAuth/loginTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");

        assertTrue(manager.login("agent", "Abcde"));
    }//end of testing correct login

    @Test
    void loginReturnsFalseForWrongPassword() throws IOException {
        File testFile = new File("testAuth/wrongPasswordTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");

        assertFalse(manager.login("agent", "wrongpass"));
    }//end of testing wrong login

    @Test
    void createCredentialsRejectsInvalidUsername() {
        CredentialManager manager = new CredentialManager(new File("testAuth/badUsername.txt"));

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createCredentials("va", "Abcde");
        });
    }//end of testing invalid username when creating credentials

    @Test
    void createCredentialsRejectsShortPassword() {
        CredentialManager manager = new CredentialManager(new File("testAuth/badPassword.txt"));

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createCredentials("agent", "abcd");
        });
    }//end of testing invalid password when creating credentials

    @Test
    void changePasswordUpdatesCredentialFile() throws IOException {
        File testFile = new File("testAuth/changePasswordTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");

        manager.changePassword("agent", "Abcde", "Newpass", "Newpass");

        assertTrue(manager.login("agent", "Newpass"));
        assertFalse(manager.login("agent", "Abcde"));
    }//end of testing if changePassword updates the credential file

    @Test
    void changePasswordRejectsMismatchedNewPasswords() throws IOException {
        File testFile = new File("testAuth/mismatchPasswordTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.changePassword("agent", "Abcde", "Newpass", "Different");
        });
    }//end of testing if changePassword rejects mismatching new passwords
    @Test
    void usernameExistsReturnsTrueForExistingUser() throws IOException {
        File testFile = new File("testAuth/usernameExistsTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");

        assertTrue(manager.usernameExists("agent"));
        assertFalse(manager.usernameExists("unknown"));
    }//end of testing usernameExists
    @Test
    void hasNoPasswordReturnsTrueWhenPasswordIsEmpty() throws IOException {
        File testFile = new File("testAuth/hasNoPasswordTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.addUser("agent");

        assertTrue(manager.hasNoPassword("agent"));
    }//end of testing hasNoPassword

    @Test
    void addUserAddsUsernameWithNoPassword() throws IOException {
        File testFile = new File("testAuth/addUserTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.addUser("agent");

        assertTrue(manager.usernameExists("agent"));
        assertTrue(manager.hasNoPassword("agent"));
    }//end of testing addUser

    @Test
    void addUserRejectsInvalidUsername() {
        CredentialManager manager = new CredentialManager(new File("testAuth/addUserInvalidTest.txt"));

        assertThrows(IllegalArgumentException.class, () -> {
            manager.addUser("ag");
        });
    }//end of testing addUser rejects invalid username

    @Test
    void addUserRejectsDuplicateUsername() throws IOException {
        File testFile = new File("testAuth/addUserDuplicateTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.addUser("agent");

        assertThrows(IllegalArgumentException.class, () -> {
            manager.addUser("agent");
        });
    }//end of testing addUser rejects duplicate username

    @Test
    void getAllUsernamesReturnsAllUsers() throws IOException {
        File testFile = new File("testAuth/getAllUsernamesTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");
        manager.createCredentials("john", "Abcde");

        List<String> usernames = manager.getAllUsernames();

        assertTrue(usernames.contains("agent"));
        assertTrue(usernames.contains("john"));
        assertEquals(2, usernames.size());
    }//end of testing getAllUsernames

    @Test
    void multipleUsersCanLoginIndependently() throws IOException {
        File testFile = new File("testAuth/multipleUsersTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");
        manager.createCredentials("john", "Fghij");

        assertTrue(manager.login("agent", "Abcde"));
        assertTrue(manager.login("john", "Fghij"));
        assertFalse(manager.login("agent", "Fghij"));
        assertFalse(manager.login("john", "Abcde"));
    }//end of testing multiple users login

    @Test
    void changePasswordDoesNotAffectOtherUsers() throws IOException {
        File testFile = new File("testAuth/changePasswordMultipleUsersTest.txt");
        testFile.delete();

        CredentialManager manager = new CredentialManager(testFile);
        manager.createCredentials("agent", "Abcde");
        manager.createCredentials("john", "Fghij");

        manager.changePassword("agent", "Abcde", "Newpass", "Newpass");

        assertTrue(manager.login("agent", "Newpass"));
        assertTrue(manager.login("john", "Fghij"));
    }//end of testing changePassword does not affect other users


}