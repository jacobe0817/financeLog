import java.io.File;
import java.nio.file.*;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class FinanceLog {
    static String currentDirectory;
    static File dataFile;
    static String[] dataDirectory;
    static Scanner in;

    public static void main(String[] args) {
        //setup
        currentDirectory = System.getProperty("user.dir");;
        dataFile = new File(Paths.get(currentDirectory, "data").toString());
        boolean dataFolderMade = dataFile.mkdir();
        if (dataFolderMade)
            System.out.println("\nA directory has been created to store this application's data");
        dataDirectory = dataFile.list();
        assert dataDirectory != null;
        in = new Scanner(System.in);

        //access or delete
        System.out.println("\nWelcome to your finance log!");
        while (true) {
            printAccounts();
            System.out.print("\nWould you like to (a)ccess or (d)elete a user's profile?\nEnter (q) to quit\n\n>>");
            String input = in.nextLine();
            if (input.equalsIgnoreCase("a")) {
                accessAccount();
            } else if (input.equalsIgnoreCase("d")) {
                deleteAccount();
            } else if (input.equalsIgnoreCase("q")) {
                break;
            } else {
                System.out.print("\n**invalid input**\n\n");
            }
        }
    }

    public static void printAccounts() {
        System.out.println("\nThe following users have data:\n");
        for (String item : dataDirectory) {
            File itemFile = Paths.get(currentDirectory, "data", item).toFile();
            if (itemFile.isDirectory()) {
                System.out.println("\t-> " + item);
            }
        }
        System.out.println();
    }

    public static void deleteAccount() {
        //get name from user
        System.out.println();
        printAccounts();
        System.out.print("\nWhat account would you like to delete?\nEnter (q) to quit\n\n>>");
        File accountFile = null;

        deleteLoop:
        while (true) {
            String name = in.nextLine().toUpperCase(Locale.ROOT).strip();
            for (String content : dataDirectory) {
                if (content.equals(name)) {
                    accountFile = Paths.get(currentDirectory, "data", name).toFile();
                    deleteFolder(accountFile);
                    break deleteLoop;
                } else if (content.equals("q"))
                    break deleteLoop;
            }
            System.out.println();
            printAccounts();
            System.out.print("\n**invalid input**\n\nto delete the account 'JACOB' enter 'JACOB' or 'jacob'\nEnter (q) to quit\n\n>>");
        }
    }

    public static void deleteFolder(File folder) {
        File[] folderContents = folder.listFiles();
        System.out.println();
        if (folderContents != null) {
            for (File file : folderContents) {
                if (file.isDirectory())
                    deleteFolder(file);
                else if (file.delete())
                    System.out.println("File deleted: " + file.toString());
            }
        }
        if (folder.delete())
            System.out.println("Folder delete: " + folder.toString());
        System.out.println();
    }

    public static void printAccountDirectory(String[] accountDirectory, String name) {
        //print existing entry months
        if (accountDirectory.length == 0)
            System.out.println("**No existing logs for " + name + "**");
        else {
            System.out.println("\nExisting logs for " + name);
            System.out.println();
            for (String item : accountDirectory)
                System.out.println("\t-> " + item);
        }
        System.out.println();
    }

    public static void accessAccount() {
        //get name from user
        System.out.print("\nWhat is your name?\n>>");
        String name = in.nextLine().toUpperCase(Locale.ROOT).strip();
        var accountFile = new File(Paths.get(currentDirectory, "data", name).toString());
        String[] accountDirectory = Objects.requireNonNullElse(accountFile.list(), new String[0]);
        boolean newUser = accountFile.mkdirs();

        System.out.println();
        if (newUser) {
            System.out.println("A directory has been created for your data\n");
            dataDirectory = dataFile.list();
        }

        //create, view, modify, or delete
        while(true) {
            printAccountDirectory(accountDirectory, name);
            System.out.print("\nWould you like to (c)reate, (v)iew, (m)odify, or (d)elete a log?\nEnter (q) to quit\n\n>>");
            String input = in.nextLine();
            if (input.equalsIgnoreCase("c")) {
                //create
                var accountEntry = createAccountEntry(name, accountFile);
                accountEntry.createDeposits();
                accountEntry.printDeposits(false);
                accountEntry.writeCSV();
                accountDirectory = accountFile.list();
            } else if (input.equalsIgnoreCase("v")) {
                //view
                if (accountDirectory.length == 0)
                    System.out.println("\n**invalid command**");
                else {
                    var accountEntry = getAccountEntry(name, accountDirectory);
                    accountEntry.printDeposits(false);
                }
            } else if (input.equalsIgnoreCase("m")) {
                //modify
                if (accountDirectory.length == 0)
                    System.out.println("\n**invalid command**");
                else {
                    var accountEntry = getAccountEntry(name, accountDirectory);
                    modifyAccountEntry(accountEntry);
                }
            } else if (input.equalsIgnoreCase("d")) {
                //delete
                deleteLog(accountDirectory, name);
                accountDirectory = accountFile.list();
            } else if (input.equalsIgnoreCase("q"))
                break;
            else {
                System.out.println("**invalid command**");
            }
        }
    }

    public static void modifyAccountEntry(AccountEntry accountEntry) {
        while (true) {
            System.out.print("\nWould you like to (a)dd or (r)emove entries from this log?\nEnter (q) to quit\n\n>>");
            String userInput = in.nextLine().trim();
            if (userInput.equalsIgnoreCase("a")) {
                accountEntry.createDeposits();
                accountEntry.writeCSV();
                break;
            } else if (userInput.equalsIgnoreCase("r")) {
                accountEntry.deleteDeposits();
                accountEntry.writeCSV();
                break;
            } else if (userInput.equalsIgnoreCase("q"))
                break;
            System.out.println("\n**invalid command**");
        }
    }

    public static void deleteLog(String[] accountDirectory, String name) {
        if (accountDirectory.length == 0)
            System.out.println("\n**no logs exist for this user**\nselect a different function");
        else {
            deleteLoop:
            while (true) {
                System.out.println();
                printAccountDirectory(accountDirectory, name);
                System.out.println();
                System.out.print("Type the name of the log file you would like to delete\nTo delete '2021_11.csv' enter '2021_11'\n\nEnter (q) to quit\n\n>>");
                String deleteTarget = in.nextLine().toLowerCase(Locale.ROOT).trim();
                if (deleteTarget.equalsIgnoreCase("q"))
                    break;
                deleteTarget = deleteTarget.endsWith(".csv") ? deleteTarget : deleteTarget + ".csv";
                for (String item : accountDirectory) {
                    if (item.equals(deleteTarget)) {
                        String filePath = Paths.get(currentDirectory, "data", name, item).toString();
                        var fileToDelete = new File(filePath);
                        boolean successfulDelete = fileToDelete.delete();
                        if (successfulDelete)
                            System.out.println("\nFile deleted: " + filePath);
                        break deleteLoop;
                    }
                }
                System.out.println("\n**invalid file name**\n");
            }
        }
    }

    public static AccountEntry getAccountEntry(String name, String[] accountDirectory) {
        AccountEntry accountEntry;
        System.out.println();
        while (true) {
            for (int i = 0; i < accountDirectory.length; i++)
                System.out.println("\t" + (i + 1) + ") " + accountDirectory[i]);
            System.out.print("\n\nEnter the number corresponding to the desired file\n>>");
            if (in.hasNextInt()) {
                int input = in.nextInt();
                if (input - 1 >= 0 && input - 1 < accountDirectory.length) {
                    in.nextLine();
                    accountEntry = new AccountEntry(name, accountDirectory[input - 1]);
                    break;
                }
            }
            in.nextLine();
            System.out.println("\n**invalid input for file index**\nenter a number that is in front of a ')'\n");
        }
        accountEntry.readCSV();
        return accountEntry;
    }

    public static AccountEntry createAccountEntry(String name, File accountFile) {
        //get date from user
        System.out.print("\nWhat year would you like to log?\n>>");
        int year = 0;
        while (true) {
            if (in.hasNextInt()) {
                year = in.nextInt();
                in.nextLine();
                if (year > 0)
                    break;
            }
            System.out.print("\n**invalid input for year**\nfor 2021 input '2021'\n\n>>");
            in.nextLine();
        }
        System.out.print("\nWhat month would you like to log, as a number?\nfor February input '2'\n>>");
        int month = 0;
        while (true) {
            if (in.hasNextInt()) {
                month = in.nextInt();
                in.nextLine();
                if (month >= 1 && month <= 12)
                    break;
            } else
                in.nextLine();
            System.out.print("\n**invalid input for month**\nfor February input '2'\n\n>>");
        }

        //create account entry
        return new AccountEntry(name, year, month);
    }
}
