import model.FileHandler;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    @Test //testing whether filenames are properly returned
    void getAvailableFilesReturnsFileNames() throws IOException {
        File testFolder = new File("testData");
        FileHandler fileHandler = new FileHandler(testFolder);

        ArrayList<String> testFiles = fileHandler.getAvailableFiles();

        assertTrue(testFiles.contains("filea.txt"));
        assertTrue(testFiles.contains("fileb.txt"));
        assertTrue(testFiles.contains("empty.txt"));
    }

    @Test //testing filea
    void readFileContentsReturnsText() throws IOException {
        File testFolder = new File("testData");
        FileHandler fileHandler = new FileHandler(testFolder);

        String fileContent = fileHandler.readFileContents("filea.txt");
        assertEquals("This is file A.", fileContent.trim());
    }

    @Test //testing fileb
    void readFileContentsReturnsMultipleLines() throws IOException {
        File testFolder = new File("testData");
        FileHandler fileHandler = new FileHandler(testFolder);

        String contents = fileHandler.readFileContents("fileb.txt");

        assertTrue(contents.contains("Line 1"));
        assertTrue(contents.contains("Line 2"));
        assertTrue(contents.contains("Line 3"));
    }

    @Test //testing empty file
    void readFileContentsReturnsEmptyString() throws IOException {
        File testFolder = new File("testData");
        FileHandler fileHandler = new FileHandler(testFolder);
        String contents = fileHandler.readFileContents("empty.txt");
        assertTrue(contents.isEmpty());
    }

    @Test //testing that an exception is thrown when file doesn't exist
    void readFileContentsThrowsExceptionWhenFileDoesNotExist() {
        File testFolder = new File("testData");
        FileHandler fileHandler = new FileHandler(testFolder);

        assertThrows(IOException.class, () -> {
            fileHandler.readFileContents("blah");
        });
    }

    @Test //testing that exception is thrown when folder doesn't exist
    void getAvailableFilesThrowsExceptionWhenFolderDoesNotExist() {
        File missingFolder = new File("folderThatDoesNotExist");
        FileHandler fileHandler = new FileHandler(missingFolder);

        assertThrows(IOException.class, () -> {
            fileHandler.getAvailableFiles();
        });
    }

    @Test //testing that exception is thrown when path is not a folder
    void getAvailableFilesThrowsExceptionWhenPathIsNotFolder() {
        File notAFolder = new File("testData/filea.txt");
        FileHandler fileHandler = new FileHandler(notAFolder);

        assertThrows(IOException.class, () -> {
            fileHandler.getAvailableFiles();
        });
    }

    @Test //makes sure getAvailableFiles doesn't return a folder (only returns files)
    void getAvailableFilesDoesNotReturnFolders() throws IOException {
        File testFolder = new File("testData");
        FileHandler fileHandler = new FileHandler(testFolder);

        ArrayList<String> testFiles = fileHandler.getAvailableFiles();

        assertFalse(testFiles.contains("fakeFolder"));
    }

}