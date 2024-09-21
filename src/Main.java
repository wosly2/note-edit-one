/*
     _   _ _____ ___  _   ___
    | \ | | ____/ _ \/ | / _ \
    |  \| |  _|| | | | || | | |
    | |\  | |__| |_| | || |_| |
    |_| \_|_____\___/|_(_)___/
     NOTE EDIT ONE VERSION 1.0
*/
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class GuiResult {
    boolean success;
    String message;

    public GuiResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

public class Main {
    final private static Scanner scannerObj = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        boolean mainLoopRunning = true;
        String path;
        GuiResult result;
        do {
            // run gui and get its sweet juicy data
            result = gui();
            mainLoopRunning = result.success;
            path = result.message;

            // run the real note editor
            editNote(path);

        } while (mainLoopRunning);
    }

    // gui process
    private static GuiResult gui() {
        final String noteDirectory = "../notes-app-1/notes/";
        String path = noteDirectory;
        boolean success = true;
        String userInput;


        File folder = new File(noteDirectory);
        File[] directoryList = folder.listFiles();

        boolean guiLoopRunning = true;

        do {
            try {
                // print file list
                System.out.println("\n<NEO1.0>\nSelect a note by typing its name or number.\n\nNote Files @ " + noteDirectory);
                int indexCounter = 1;
                if (directoryList.length > 0) {
                    for (File file : directoryList) {
                        if (file.isFile()) {
                            System.out.println(" " + indexCounter + " " + file.getName().substring(0, file.getName().length() - 5));
                        } else {
                            System.out.println(" * (hidden directory)");
                        }
                        indexCounter++;
                    }
                    System.out.print(" or type '~new' to create a blank note!\n\nload> ");
                } else {
                    System.out.println(" No existing notes! type '~new' to make a blank note.");
                }

                // get user input
                userInput = scannerObj.nextLine();

                // deal with user input
                if (userInput.equals("~q") || userInput.equals("~quit")) {
                    System.exit(0);
                } else if (!userInput.equals("~new")) {
                    // load by number
                    try {
                        if (isInteger(userInput)) {
                            path = directoryList[Integer.parseInt(userInput) - 1].getPath();

                        // load by name
                        } else {
                            for (File file : directoryList) {
                                if (file.getName().equals(userInput + ".json")) {
                                    path = file.getPath();
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Note is corrupted or missing!");
                    }
                } else {
                    // write a new file
                    try {
                        System.out.print("Title: ");
                        userInput = scannerObj.nextLine();
                        path = noteDirectory + userInput + ".json";
                        System.out.println("path: " + path);
                        File newFile = new File(path);

                        if (newFile.createNewFile()) {
                            // successfully made new file
                            System.out.println("File created: " + userInput);

                            // write defaults to that file
                            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(newFile))) {
                                bufferedWriter.write("{\"title\":\"" + userInput + "\", \"checks\":{}}");
                            }
                        } else {
                            System.out.println("File already existed.");
                        }
                    } catch (IOException e) {
                        System.out.println("An error occurred while creating the file.");
                        e.printStackTrace();
                    }
                }

                // end loop since we got this far with no problems
                guiLoopRunning = false;
            } catch (Exception e) {
                System.out.println("Invalid input.");
            }

        } while (guiLoopRunning);

        return new GuiResult(success, path);
    }

    // editNote() Process
    private static void editNote(final String path) throws Exception {
        // get note
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> note = objectMapper.readValue(new File(path), Map.class);
        String title = (String) note.get("title");
        Map<String, String> checks = (Map<String, String>) note.get("checks");

        String userInput = "";
        Dictionary<String, String> checkSymbols = new Hashtable<>();
        checkSymbols.put("unchecked", "✘");
        checkSymbols.put("checked", "✔");
        checkSymbols.put("unknown", "?");

        int line = 0;
        String edit = "";
        String lineText = "";
        String checkVal;
        boolean editLoopRunning = true;
        // edit loop
        do {
            // Print Note
            clearConsole();
            System.out.println("\n<NEO1.0>\n" + title);
            AtomicInteger indexCounter = new AtomicInteger(1);
            checks.forEach((task, status) -> System.out.println(" " + indexCounter.getAndIncrement() + " " + checkSymbols.get(status) + " " + task));

            // get user input
            System.out.print("\nedit> ");
            userInput = scannerObj.nextLine();
            // CHECK/UNCHECK LINE
            if (userInput.matches("^check \\d+$")) {
                try {
                    line = Integer.parseInt(userInput.split(" ")[1]);
                    lineText = (new ArrayList<>(checks.keySet())).get(line - 1);
                    if (checks.get(lineText).equals("checked")) {
                        checkVal = "unchecked";
                    } else if (checks.get(lineText).equals("unchecked")) {
                        checkVal = "checked";
                    } else {
                        checkVal = "unknown";
                    }
                    checks.put(lineText, checkVal);
                } catch (Exception e) {
                    System.out.println("Cannot check/uncheck line, does not exist!");
                }
                // DELETE LINE
            } else if (userInput.matches("^del \\d+$")) {
                try {
                    line = Integer.parseInt(userInput.split(" ")[1]);
                    lineText = (new ArrayList<>(checks.keySet())).get(line - 1);
                    checks.remove(lineText);
                } catch (Exception e) {
                    System.out.println("Cannot delete line, does not exist!");
                }
                // UPDATE LINE
            } else if (userInput.matches("^\\d+ .+$")) {
                try {
                    String[] parts = userInput.split(" ", 2);
                    line = Integer.parseInt(parts[0]);
                    edit = parts[1];
                    lineText = (new ArrayList<>(checks.keySet())).get(line - 1);
                    changeKeyAtIndex(checks, lineText, edit, line - 1);
                } catch (Exception e) {
                    System.out.println("Cannot edit line, does not exist!");
                }
            } else {
                switch (userInput) {
                    case "~quit", "~q":
                        editLoopRunning = false;
                        break;
                    case "~saq":
                        editLoopRunning = false;
                        // don't break, fall to save
                    case "~save":
                        // SAVE
                        try {
                            note.put("checks", checks);
                            objectMapper.writeValue(new File(path), note);
                            System.out.println("Saved.");
                        } catch (Exception e) {
                            System.out.println("Error saving changes: " + e.getMessage());
                        }
                        break;
                    case "help", "~help":
                        System.out.println("""
                                EDIT FUNCS
                                    check <LINE> ... check or uncheck the line at <LINE>
                                    del <LINE> ..... delete the line at <LINE>
                                    <LINE> <TEXT> .. edit the line at  <LINE>
                                    <TEXT> ......... add a new unchecked line using <TEXT>
                                
                                FILE FUNCS
                                    ~quit, ~q ...... quit without saving
                                    ~saq ........... save and quit
                                    help, ~help .... return to this menu
                                """);
                    default:
                        checks.put(userInput, "unchecked");
                }

            }

        } while (editLoopRunning);
    }

    // ChatGPT generated map edit func
    private static void changeKeyAtIndex(Map<String, String> map, String oldKey, String newKey, int index) {
        if (!map.containsKey(oldKey) || index < 0 || index > map.size()) {
            return; // exit if old key doesn't exist or index is out of bounds
        }

        // Convert map to LinkedHashMap to maintain order
        LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
        int currentIndex = 0;
        String value = map.get(oldKey);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (currentIndex == index) {
                newMap.put(newKey, value); // insert new key at the specific index
            }
            if (!entry.getKey().equals(oldKey)) {
                newMap.put(entry.getKey(), entry.getValue()); // add other entries
            }
            currentIndex++;
        }

        // If index is at the end, insert new key after looping
        if (currentIndex == index) {
            newMap.put(newKey, value);
        }

        // Clear original map and put everything from newMap
        map.clear();
        map.putAll(newMap);
    }

    // clearing the terminal
    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // check for ints
    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}