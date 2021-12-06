import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Scanner;

public class AccountEntry {
    private final int year;
    private final int month;
    private final ArrayList<Deposit> deposits;
    private final String path;

    public AccountEntry(String name, int year, int month) {
        this.year = year;
        this.month = month;
        String monthString = month >= 10 ? "" + month : "0" + month;
        String entryFileName = "" + year + "_" + monthString + ".csv";
        path = Paths.get(FinanceLog.currentDirectory, "data", name, entryFileName).toString();
        deposits = new ArrayList<>();
    }

    public AccountEntry(String name, String path) {
        this.path = Paths.get(FinanceLog.currentDirectory, "data", name, path).toString();
        String[] parts = path.split("_");
        parts[1] = parts[1].substring(0, 2);
        year = Integer.parseInt(parts[0]);
        month = Integer.parseInt(parts[1]);
        deposits = new ArrayList<>();
    }

    public void addDeposit(Deposit deposit) {
        deposits.add(deposit);
    }

    public void readCSV() {
        try {
            var in = new Scanner(Path.of(path), StandardCharsets.UTF_8);
            ArrayList<String> rows = new ArrayList<>();
            while (in.hasNextLine()) {
                rows.add(in.nextLine());
            }
            rows.remove(0);
            if (rows.size() > 0) {
                for (String row : rows) {
                    String[] parts = row.split(",");
                    Deposit deposit;
                    if (parts.length == 3) {
                        deposit = new Deposit(Integer.parseInt(parts[0]), Double.parseDouble(parts[1]), parts[2]);
                    } else {
                        deposit = new Deposit(Integer.parseInt(parts[0]), Double.parseDouble(parts[1]), "");
                    }
                    deposits.add(deposit);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeCSV() {
        try {
            var file = new File(path);
            boolean success = file.createNewFile();
            var out = new PrintWriter(path, StandardCharsets.UTF_8);
            out.println("day,amount deposited ($),description");
            if (deposits != null && deposits.size() > 0) {
                for (Deposit deposit : deposits) {
                    out.println(deposit.getCSVstring());
                }
            }
            out.flush();
            out.close();
            if (success)
                System.out.println("Data successfully written to " + path + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printDeposits(boolean numericalIndex) {
        String monthName = new DateFormatSymbols().getMonths()[month - 1];
        String spacing = " ".repeat(50);
        String itemSpacing = " ".repeat(48);
        String indexSpacing = " ".repeat(46);
        String totalSpacing = " ".repeat(42);
        String header = monthName + " " + year;
        System.out.println("\n" + spacing + header);
        System.out.println(spacing + "-".repeat(header.length()));

        if (numericalIndex) {
            int i = 1;
            for (Deposit deposit : deposits) {
                System.out.print(indexSpacing + i + ")");
                deposit.print("", itemSpacing);
                i++;
            }
        } else {
            double total = 0;
            for (Deposit deposit : deposits) {
                deposit.print(itemSpacing, itemSpacing);
                total += deposit.getAmount();
            }
            if (total < 0)
                System.out.printf(totalSpacing + "Total:   - $%,.2f%n", Math.abs(total));
            else
                System.out.printf(totalSpacing + "Total:     $%,.2f%n", Math.abs(total));
            System.out.println();
        }

        if (numericalIndex && deposits.size() == 0)
            System.out.println("**this log has no deposits/withdrawals**\n");
    }

    public void createDeposits() {
        depositLoop:
        while (true){
            //get day
            System.out.print("\nEnter the day of a deposit/withdrawal or enter (q) to quit\n>>");
            int day = 0;
            while (true) {
                if (FinanceLog.in.hasNextInt()) {
                    day = FinanceLog.in.nextInt();
                    FinanceLog.in.nextLine();
                    if (day >= 1 && day <= 31)
                        break;
                } else {
                    String input = FinanceLog.in.nextLine();
                    if (input.equalsIgnoreCase("q"))
                        break depositLoop;
                }
                System.out.print("\n**invalid input for day of deposit**\nfor the 21st enter '21'\n\n>>");
            }
            //get amount
            System.out.print("\nEnter a positive number in dollars to log a deposit, enter a negative number to log a withdrawal\n>>");
            double depositAmount = 0;
            while (true) {
                if (FinanceLog.in.hasNextDouble()) {
                    depositAmount = FinanceLog.in.nextDouble();
                    FinanceLog.in.nextLine();
                    break;
                }
                FinanceLog.in.nextLine();
                System.out.print("\n**invalid input for deposit amount**\nfor a withdrawal of $21.60 input '-21.6'\n\n>>");
            }

            //get description
            String depositDescription = null;
            System.out.print("\nOptional: enter a description for the deposit/withdrawal\ndo NOT include a comma character\n\n>>");
            while (true) {
                depositDescription = FinanceLog.in.nextLine();
                if (!depositDescription.contains(","))
                    break;
                System.out.print("\n**invalid input**\ncommas are NOT allowed in the description\n\n>>");
            }
            var deposit = new Deposit(day, depositAmount, depositDescription);
            addDeposit(deposit);
        }
    }

    public void deleteDeposits() {
        while(true) {
            printDeposits(true);
            System.out.print("\nEnter the number corresponding to the deposit/withdrawal you would like to delete\nEnter (q) to quit\n\n>>");
            if (FinanceLog.in.hasNextInt()) {
                int input = FinanceLog.in.nextInt();
                if (deposits.size() > 0 && input - 1 < deposits.size()) {
                    deposits.remove(input - 1);
                    FinanceLog.in.nextLine();
                    continue;
                }
            }
            String input = FinanceLog.in.nextLine();
            if (input.trim().equalsIgnoreCase("q"))
                break;
            System.out.println("\n**invalid input for deposit/withdrawal index**");
        }
    }
}
