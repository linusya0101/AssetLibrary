import java.time.LocalDate;

public class Asset {

    private int id;
    private String name;
    private AssetType type;
    private boolean available;
    private LocalDate lastCheckOutDate;
    private LocalDate plannedReturnDate;
    private Integer borrowedBy;

    public Asset(int id, String name, AssetType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.available = true;
        this.lastCheckOutDate = null;
        this.plannedReturnDate = null;
        this.borrowedBy = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getType() {
        return type;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getLastCheckOutDate() {
        return lastCheckOutDate;
    }

    public void setLastCheckOutDate(LocalDate lastCheckOutDate) {
        this.lastCheckOutDate = lastCheckOutDate;
    }

    public LocalDate getPlannedReturnDate() {
        return plannedReturnDate;
    }

    public void setPlannedReturnDate(LocalDate plannedReturnDate) {
        this.plannedReturnDate = plannedReturnDate;
    }

    public Integer getBorrowedBy() {
        return borrowedBy;
    }

    public void setBorrowedBy(Integer borrowedBy) {
        this.borrowedBy = borrowedBy;
    }

    public boolean borrow(Student student, LocalDate plannedReturnDate) {
        if (!isAvailable()) {
            return false;
        }

        this.borrowedBy = student.getId();
        this.available = false;
        this.plannedReturnDate = plannedReturnDate;
        this.lastCheckOutDate = LocalDate.now();
        return true;
    }

    public boolean returnAsset() {
        if (borrowedBy == null) {
            return false;
        }

        this.available = true;
        this.borrowedBy = null;
        this.plannedReturnDate = null;
        return true;
    }
}
