package model;

import java.util.*;
import java.io.*;

public class FileHandler {
    private final File dataFolder;

    public FileHandler(){
        this.dataFolder = new File("./data");
    }//end of default constructor - folder in question is "data" for the assignment

    public FileHandler(File dataFolder){
        this.dataFolder = dataFolder;
    }//end of 1-arg (folder in question is anything that you want to test, i.e. "testData")


    /**
     * @param fileName
     * Reads and returns the contents of one file
     */
    public String readFileContents(String fileName) throws IOException{
        File targetFile = new File(dataFolder, fileName);

        if (!targetFile.exists()) { //if targetFile doesn't exist
            throw new FileNotFoundException("File not found: " + fileName);
        }
        if (!targetFile.isFile()) { //if target is not a file (could be a folder)
            throw new IOException("Requested path is not a file: " + fileName);
        }

        String contentsInFile = ""; //build the contents of the file as a String
        Scanner fileScanner = new Scanner(targetFile);

        while (fileScanner.hasNextLine()) {
            contentsInFile += fileScanner.nextLine();

            if(fileScanner.hasNextLine()){
                contentsInFile += "\n";
            }
        }
        fileScanner.close();
        return contentsInFile;
    }//end of readFileContents

    /**
     * Returns the names of all regular files in the data folder
     */
    public ArrayList<String> getAvailableFiles() throws IOException{
        ArrayList<String> fileNames = new ArrayList<>();

        if(!dataFolder.exists()){ //if folder doesn't exist
            throw new IOException("Folder doesn't exist.");
        }

        if(!dataFolder.isDirectory()){ //if path isn't a directory
            throw new IOException("Path is not a directory.");
        }

        File[] filesInsideFolder = dataFolder.listFiles();
        if(filesInsideFolder == null){ //if can't read folder
            throw new IOException("Couldn't read files from the folder.");
        }

        for(File file : filesInsideFolder){
            if(file.isFile()) //only add files (not folders or anything else)
                fileNames.add(file.getName());
        }

        Collections.sort(fileNames); //sort fileNames into alphabetical order
        return fileNames;
    }//end of getAvailableFiles

}//end of FileHandler
