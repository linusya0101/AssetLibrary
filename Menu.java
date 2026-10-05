import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Menu {

    private final Library library;
    private final Scanner scanner;

    public Menu(Library library) {
        this.library = library;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("===== Main Menu =====");
            System.out.println("1. Student Management Menu");
            System.out.println("2. Asset Management Menu");
            System.out.println("0. Exit");
            String choice = prompt("Select an option: ");

            switch (choice) {
                case "1":
                    studentManagementMenu();
                    break;
                case "2":
                    assetManagementMenu();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }

    // ---- Student Management Menu ----

    private void studentManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Student Management Menu =====");
            System.out.println("1. Add Student");
            System.out.println("2. Remove Student");
            System.out.println("3. Search Student");
            System.out.println("0. Back");
            String choice = prompt("Select an option: ");

            switch (choice) {
                case "1":
                    addStudentFlow();
                    break;
                case "2":
                    removeStudentFlow();
                    break;
                case "3":
                    searchStudentFlow();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void addStudentFlow() {
        String name = prompt("Enter student name: ");
        Integer year = promptInt("Enter student year: ");
        if (year == null) {
            System.out.println("Invalid year. Add Student cancelled.");
            return;
        }
        String major = prompt("Enter student major: ");

        Student student = library.addStudent(name, year, major);
        System.out.println("Student added with ID " + student.getId() + ".");
    }

    private void removeStudentFlow() {
        Integer id = promptInt("Enter student ID to remove: ");
        if (id == null) {
            System.out.println("Invalid ID.");
            return;
        }

        Student student = library.findStudentById(id);
        if (student == null) {
            System.out.println("No student found with ID " + id + ".");
            return;
        }
        if (library.studentHasBorrowedAssets(id)) {
            System.out.println("Cannot remove student " + student.getName() + ": they still have borrowed assets.");
            return;
        }

        library.removeStudent(id);
        System.out.println("Student " + student.getName() + " removed.");
    }

    private void searchStudentFlow() {
        String name = prompt("Enter name to search (leave blank to skip): ");
        String yearInput = prompt("Enter year to search (leave blank to skip): ");
        String major = prompt("Enter major to search (leave blank to skip): ");

        Integer year = null;
        if (!yearInput.trim().isEmpty()) {
            try {
                year = Integer.parseInt(yearInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid year, ignoring that filter.");
            }
        }

        ArrayList<Student> results = library.searchStudents(name, year, major);
        printStudentsTable(results);
    }

    // ---- Asset Management Menu ----

    private void assetManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Asset Management Menu =====");
            System.out.println("1. Add Asset");
            System.out.println("2. List Available Assets");
            System.out.println("3. List Borrowed Assets");
            System.out.println("4. Search Borrowed Asset by Student");
            System.out.println("5. Borrow Asset");
            System.out.println("6. Return Asset");
            System.out.println("7. Remove Asset");
            System.out.println("0. Back");
            String choice = prompt("Select an option: ");

            switch (choice) {
                case "1":
                    addAssetFlow();
                    break;
                case "2":
                    listAvailableAssetsFlow();
                    break;
                case "3":
                    listBorrowedAssetsFlow();
                    break;
                case "4":
                    searchBorrowedAssetByStudentFlow();
                    break;
                case "5":
                    borrowAssetFlow();
                    break;
                case "6":
                    returnAssetFlow();
                    break;
                case "7":
                    removeAssetFlow();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void addAssetFlow() {
        String name = prompt("Enter asset name: ");
        AssetType type = promptAssetType("Enter asset type (COMPUTER, CALCULATOR, BOOK): ");
        if (type == null) {
            System.out.println("Invalid type. Add Asset cancelled.");
            return;
        }

        Asset asset = library.addAsset(name, type);
        System.out.println("Asset \"" + asset.getName() + "\" added with ID " + asset.getId() + " (available = true).");
    }

    private void removeAssetFlow() {
        String input = prompt("Enter asset ID or name to remove: ");
        Asset asset = resolveAsset(input);
        if (asset == null) {
            return;
        }
        if (!asset.isAvailable()) {
            System.out.println("Cannot remove asset \"" + asset.getName() + "\": it is currently borrowed.");
            return;
        }

        library.removeAsset(asset.getId());
        System.out.println("Asset \"" + asset.getName() + "\" removed.");
    }

    private void listAvailableAssetsFlow() {
        String typeInput = prompt("Enter asset type to filter (leave blank for all): ");
        AssetType type = null;
        if (!typeInput.trim().isEmpty()) {
            try {
                type = AssetType.valueOf(typeInput.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type, showing all available assets.");
            }
        }

        ArrayList<Asset> results = library.listAvailableAssets(type);
        printAssetsTable(results, false);
    }

    private void listBorrowedAssetsFlow() {
        ArrayList<Asset> results = library.listBorrowedAssets();
        printAssetsTable(results, true);
    }

    private void searchBorrowedAssetByStudentFlow() {
        String name = prompt("Enter student name: ");
        ArrayList<Asset> results = library.searchBorrowedAssetsByStudentName(name);
        printAssetsTable(results, true);
    }

    private void borrowAssetFlow() {
        String assetInput = prompt("Enter asset ID or name to borrow: ");
        Asset asset = resolveAsset(assetInput);
        if (asset == null) {
            return;
        }
        if (!asset.isAvailable()) {
            System.out.println("Asset \"" + asset.getName() + "\" is already borrowed.");
            return;
        }

        String studentInput = prompt("Enter student name or ID: ");
        Student student = resolveStudent(studentInput);
        if (student == null) {
            return;
        }

        LocalDate returnDate = promptDate("Enter planned return date (YYYY-MM-DD): ");
        if (returnDate == null) {
            System.out.println("Invalid date. Borrow Asset cancelled.");
            return;
        }

        boolean success = library.borrowAsset(asset.getId(), student.getId(), returnDate);
        if (success) {
            System.out.println("Asset \"" + asset.getName() + "\" borrowed by " + student.getName() + ".");
        } else {
            System.out.println("Could not borrow asset \"" + asset.getName() + "\".");
        }
    }

    private void returnAssetFlow() {
        Integer assetId = promptInt("Enter asset ID to return: ");
        if (assetId == null) {
            System.out.println("Invalid asset ID.");
            return;
        }

        boolean success = library.returnAsset(assetId);
        if (success) {
            System.out.println("Asset " + assetId + " returned.");
        } else {
            System.out.println("Could not return asset " + assetId + " (not found or not currently borrowed).");
        }
    }

    // ---- Helpers ----

    /** Resolves a student by numeric ID, or by name (disambiguating if multiple match). */
    private Student resolveStudent(String input) {
        try {
            int id = Integer.parseInt(input.trim());
            Student student = library.findStudentById(id);
            if (student == null) {
                System.out.println("No student found with ID " + id + ".");
            }
            return student;
        } catch (NumberFormatException e) {
            ArrayList<Student> matches = library.searchStudents(input, null, null);
            if (matches.isEmpty()) {
                System.out.println("No student found matching \"" + input + "\".");
                return null;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            }
            System.out.println("Multiple students match \"" + input + "\":");
            printStudentsTable(matches);
            Integer id = promptInt("Enter the exact student ID: ");
            if (id == null) {
                return null;
            }
            Student student = library.findStudentById(id);
            if (student == null) {
                System.out.println("No student found with ID " + id + ".");
            }
            return student;
        }
    }

    /** Resolves an asset by numeric ID, or by name (disambiguating if multiple match). */
    private Asset resolveAsset(String input) {
        try {
            int id = Integer.parseInt(input.trim());
            Asset asset = library.findAssetById(id);
            if (asset == null) {
                System.out.println("No asset found with ID " + id + ".");
            }
            return asset;
        } catch (NumberFormatException e) {
            ArrayList<Asset> matches = library.searchAssetsByName(input);
            if (matches.isEmpty()) {
                System.out.println("No asset found matching \"" + input + "\".");
                return null;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            }
            System.out.println("Multiple assets match \"" + input + "\":");
            printAssetsTable(matches, false);
            Integer id = promptInt("Enter the exact asset ID: ");
            if (id == null) {
                return null;
            }
            Asset asset = library.findAssetById(id);
            if (asset == null) {
                System.out.println("No asset found with ID " + id + ".");
            }
            return asset;
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private Integer promptInt(String message) {
        String input = prompt(message);
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate promptDate(String message) {
        String input = prompt(message);
        try {
            return LocalDate.parse(input.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private AssetType promptAssetType(String message) {
        String input = prompt(message);
        try {
            return AssetType.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void printStudentsTable(ArrayList<Student> students) {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        System.out.printf("%-6s %-20s %-6s %-20s%n", "ID", "Name", "Year", "Major");
        for (Student student : students) {
            System.out.printf("%-6d %-20s %-6d %-20s%n",
                    student.getId(), student.getName(), student.getYear(), student.getMajor());
        }
    }

    private void printAssetsTable(ArrayList<Asset> assets, boolean includeBorrowInfo) {
        if (assets.isEmpty()) {
            System.out.println("No assets found.");
            return;
        }
        if (includeBorrowInfo) {
            System.out.printf("%-6s %-20s %-12s %-20s %-15s%n", "ID", "Name", "Type", "Borrowed By", "Planned Return");
            for (Asset asset : assets) {
                Student borrower = library.findStudentById(asset.getBorrowedBy());
                String borrowerName = borrower != null ? borrower.getName() : "Unknown";
                System.out.printf("%-6d %-20s %-12s %-20s %-15s%n",
                        asset.getId(), asset.getName(), asset.getType(), borrowerName, asset.getPlannedReturnDate());
            }
        } else {
            System.out.printf("%-6s %-20s %-12s %-10s%n", "ID", "Name", "Type", "Available");
            for (Asset asset : assets) {
                System.out.printf("%-6d %-20s %-12s %-10s%n",
                        asset.getId(), asset.getName(), asset.getType(), asset.isAvailable());
            }
        }
    }
}
