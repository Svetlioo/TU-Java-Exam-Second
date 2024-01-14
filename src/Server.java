import model.Regex;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Server {
    private static final String FILE_NAME = "regex.bin";
    private final Object regexLock;
    private ServerSocket server;

    public Server() {
        ensureFileExists();
        this.regexLock = new Object();
    }

    public void start() {
        try {
            System.out.println("Server listening.");
            this.server = new ServerSocket(8080);

            while (true) {

                Socket client = server.accept();

                Thread clientThread = new Thread(() -> {
                    System.out.println("Accepted client.");
                    try (Scanner in = new Scanner(client.getInputStream()); PrintStream out = new PrintStream(client.getOutputStream())) {
                        userMenu(in, out);
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }

                });
                clientThread.start();
            }

        } catch (IOException e) {
            throw new RuntimeException();
        }


    }

    private void userMenu(Scanner in, PrintStream out) {
        while (true) {
            Command[] commands = Command.values();
            for (int i = 0; i < commands.length; i++) {
                out.printf("Press %d to %s regex.\n", i + 1, commands[i].toString().toLowerCase());
            }
            out.println("Press 0 to exit.");
            try {
                int choice = Integer.parseInt(in.nextLine());
                if (choice == 0) return;
                if (choice - 1 > commands.length) {
                    out.println("Invalid choice.");
                    continue;
                }
                Command chosenCommand = commands[choice];
                switch (chosenCommand) {
                    case ADD: {
                        addMenu(in, out);
                        break;
                    }
                    case SEARCH: {
                        searchMenu(in, out);
                        break;
                    }
                    default: {
                        throw new InvalidCommandException("No such command.");
                    }
                }
            } catch (NumberFormatException e) {
                out.println("Enter a valid integer.");
            }

        }

    }

    private void addMenu(Scanner in, PrintStream out) {
        out.println("Enter description: ");
        String description = in.nextLine();
        out.println("Enter pattern: ");
        String pattern = in.nextLine();
        try {
            ensurePatternDoesNotExist(pattern);
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }
        Regex regex = new Regex(pattern, description);
        String[] testStrings = getTestStrings(in, out);
        List<Boolean> testResults = RegexTester.test(regex, testStrings);
        displayTestResults(out, testStrings, testResults);

        out.println("Press Y to save the regex.");
        if (!in.nextLine().equalsIgnoreCase("Y")) return;
        synchronized (regexLock) {
            Map<Integer, Regex> regexMap = loadRegexMap();
            regexMap.putIfAbsent(regex.getId(), regex);
            saveRegexMap(regexMap);
        }

    }

    private static String[] getTestStrings(Scanner in, PrintStream out) {
        out.println("Now you will enter 3 test strings.");
        String[] testStrings = new String[3];
        for (int i = 0; i < testStrings.length; i++) {
            out.printf("Enter %d test string: \n", i + 1);
            testStrings[i] = in.nextLine();
        }
        return testStrings;
    }

    private void searchMenu(Scanner in, PrintStream out) {
        out.println("Enter keyword to search.");
        String keyword = in.nextLine();
        Map<Integer, Regex> regexMap;
        synchronized (regexLock) {
            regexMap = loadRegexMap();
        }

        List<Regex> filteredRegex = regexMap.values()
                .stream()
                .filter(regex -> regex.getDescription().contains(keyword))
                .sorted(Comparator.comparing(Regex::getRating).reversed())
                .toList();

        if (filteredRegex.isEmpty()) {
            out.println("Keyword not found.");
            return;

        }
        for (Regex regex : filteredRegex) {
            out.printf("Type %d to get %s.\n", regex.getId(), regex);
        }
        while (true) {
            try {
                Integer id = Integer.parseInt(in.nextLine());
                if (!regexMap.containsKey(id)) {
                    out.println("No such id.");
                    continue;
                }
                Regex regex = regexMap.get(id);
                String[] testStrings = getTestStrings(in, out);
                List<Boolean> testResults = RegexTester.test(regex, testStrings);
                displayTestResults(out, testStrings, testResults);
                out.println("Press Y to give one rating or Press N to remove one rating.");
                String rating = in.nextLine();
                if (rating.equalsIgnoreCase("Y")) {
                    regex.setRating(regex.getRating() + 1);
                    out.println("New rating of regex: " + regex.getRating());
                } else if (rating.equalsIgnoreCase("N")) {
                    if (regex.getRating() >= 1) {
                        regex.setRating(regex.getRating() - 1);
                    }
                    out.println("New rating of regex: " + regex.getRating());
                } else {
                    out.println("No rating given.");
                    return;
                }
                synchronized (regexLock) {
                    saveRegexMap(regexMap);
                }
                break;
            } catch (NumberFormatException e) {
                out.println("Only integers allowed.");
            }
        }

    }

    private static void displayTestResults(PrintStream out, String[] testStrings, List<Boolean> testResults) {
        for (int i = 0; i < testStrings.length; i++) {
            out.printf("String %s : %s\n", testStrings[i], testResults.get(i) ? "match" : "no match");
        }
    }


    private void ensureFileExists() {
        try {
            if (!new File(FILE_NAME).exists()) {
                Files.createFile(Path.of(FILE_NAME));
                System.out.println("File created successfully.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error creating the file.");
        }

    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Regex> loadRegexMap() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (Map<Integer, Regex>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    private void saveRegexMap(Map<Integer, Regex> regexMap) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(regexMap);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void ensurePatternDoesNotExist(String pattern) throws IllegalArgumentException {
        synchronized (regexLock) {
            boolean patternExists = loadRegexMap().values()
                    .stream()
                    .anyMatch(existingRegex -> existingRegex.getPattern().equals(pattern));

            if (patternExists) {
                throw new IllegalArgumentException("Pattern already exists: " + pattern);
            }
        }
    }


}
