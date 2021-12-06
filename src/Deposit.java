public class Deposit {
    private final int day;
    private final double amount;
    private final String description;

    public Deposit(int day, double amount, String description) {
        this.day = day;
        this.amount = amount;
        this.description = description;
    }

    public int getDay() {
        return day;
    }

    public static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + suffixes[i % 10];
        };
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getCSVstring() {
        return new String(day + "," + amount + "," + description);
    }

    public void print(String amountSpacing, String descriptionSpacing) {
        //create a map mapping day to arraylist of deposits??
        if (amount < 0)
            System.out.printf(amountSpacing + "   - $%,.2f%n", Math.abs(amount));
        else
            System.out.printf(amountSpacing + "     $%,.2f%n", Math.abs(amount));
        if (description.equals(""))
            System.out.println(descriptionSpacing + "\t\t on the " + ordinal(day));
        else
            System.out.println(descriptionSpacing + "\t\t " + description + " on the " + ordinal(day));
        System.out.println();
    }
}