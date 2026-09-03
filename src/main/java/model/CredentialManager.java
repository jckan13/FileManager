package model;

import java.io.*;
import java.util.*;

public class CredentialManager implements LoginService {

    private final File credentialFile;

    public CredentialManager() {
        this.credentialFile = new File("auth/credentials.txt");
    }//end of default constructor

    public CredentialManager(File credentialFile){
        this.credentialFile = credentialFile;
    } //end of 1-arg

    @Override
    public boolean credentialsExist() {
        return credentialFile.exists() && credentialFile.isFile() && credentialFile.length() > 0;
    }

    @Override
    public boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return username.matches("[a-zA-Z0-9_\\-]{3,}");
    }//end of username checking

    @Override
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 5) {
            return false;
        }
        boolean Up = password.chars().anyMatch(Character::isUpperCase);
        boolean Low = password.chars().anyMatch(Character::isLowerCase);
        return Up && Low;
    }//end of pw checking

    private Map<String, String> loadAllCredentials() throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        if (!credentialsExist()) return map;

        try (BufferedReader reader = new BufferedReader(new FileReader(credentialFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        }
        return map;
    }//end of loadAllCredentials

    private void saveAllCredentials(Map<String, String> credentials) throws IOException {
        File parentFolder = credentialFile.getParentFile();
        if (parentFolder != null && !parentFolder.exists()) {
            parentFolder.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(credentialFile, false))) {
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
        }
    }//end of saveAllCredentials

    @Override
    public void createCredentials(String username, String password) throws IOException {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Username must contain at least 3 characters. It can contain letters, numbers, hypens,or underscores.");
        }

        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 5 characters long and contain uppercase and lowercase letters.");
        }

        Map<String, String> credentials = loadAllCredentials();
        credentials.put(username, password);
        saveAllCredentials(credentials);
    }//end of create credentials (making a profile of user/pw)

    @Override
    public boolean login(String username, String password) throws IOException {
        if (!credentialsExist()) {
            throw new FileNotFoundException("Credential file not found.");
        }

        Map<String, String> credentials = loadAllCredentials();
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }//end of login

    @Override
    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) throws IOException {
        if (!login(username, currentPassword)) {
            throw new SecurityException("Current username or password is incorrect.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Password must be at least 5 characters long and contain uppercase and lowercase letters.");
        }

        createCredentials(username, newPassword);
    }//end of changePassword

    public boolean usernameExists(String username) throws IOException {
        return loadAllCredentials().containsKey(username);
    }//end of usernameExists

    public boolean hasNoPassword(String username) throws IOException {
        Map<String, String> credentials = loadAllCredentials();
        return credentials.containsKey(username) &&
                credentials.get(username).isEmpty();
    }//end of hasNoPassword

    public void addUser(String username) throws IOException {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be at least 3 characters and can only contain letters, numbers, hyphens, and underscores.");
        }
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Map<String, String> credentials = loadAllCredentials();
        credentials.put(username, "");
        saveAllCredentials(credentials);
    }//end of addUser

    public List<String> getAllUsernames() throws IOException {
        return new ArrayList<>(loadAllCredentials().keySet());
    }//end of getAllUsernames

}//end of CredentialManager