import java.time.LocalDate;
import java.util.ArrayList;

public class Library {

    private ArrayList<Student> students;
    private ArrayList<Asset> assets;

    public Library() {
        this.students = new ArrayList<Student>();
        this.assets = new ArrayList<Asset>();
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
    }

    public ArrayList<Asset> getAssets() {
        return assets;
    }

    public void setAssets(ArrayList<Asset> assets) {
        this.assets = assets;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
    }

    public Student findStudentById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    public Asset findAssetById(int id) {
        for (Asset asset : assets) {
            if (asset.getId() == id) {
                return asset;
            }
        }
        return null;
    }

    // ---- Student management ----

    public Student addStudent(String name, int year, String major) {
        Student student = new Student(generateNextStudentId(), name, year, major);
        students.add(student);
        return student;
    }

    public boolean studentHasBorrowedAssets(int studentId) {
        for (Asset asset : assets) {
            if (!asset.isAvailable() && asset.getBorrowedBy() != null && asset.getBorrowedBy() == studentId) {
                return true;
            }
        }
        return false;
    }

    public boolean removeStudent(int id) {
        Student student = findStudentById(id);
        if (student == null || studentHasBorrowedAssets(id)) {
            return false;
        }
        students.remove(student);
        return true;
    }

    public ArrayList<Student> searchStudents(String name, Integer year, String major) {
        ArrayList<Student> result = new ArrayList<Student>();
        for (Student student : students) {
            if (name != null && !name.trim().isEmpty()
                    && !student.getName().toLowerCase().contains(name.trim().toLowerCase())) {
                continue;
            }
            if (year != null && student.getYear() != year) {
                continue;
            }
            if (major != null && !major.trim().isEmpty()
                    && !student.getMajor().toLowerCase().contains(major.trim().toLowerCase())) {
                continue;
            }
            result.add(student);
        }
        return result;
    }

    private int generateNextStudentId() {
        int maxId = 0;
        for (Student student : students) {
            maxId = Math.max(maxId, student.getId());
        }
        return maxId + 1;
    }

    // ---- Asset management ----

    public Asset addAsset(String name, AssetType type) {
        Asset asset = new Asset(generateNextAssetId(), name, type);
        assets.add(asset);
        return asset;
    }

    public boolean removeAsset(int id) {
        Asset asset = findAssetById(id);
        if (asset == null || !asset.isAvailable()) {
            return false;
        }
        assets.remove(asset);
        return true;
    }

    public ArrayList<Asset> searchAssetsByName(String name) {
        ArrayList<Asset> result = new ArrayList<Asset>();
        if (name == null || name.trim().isEmpty()) {
            return result;
        }
        for (Asset asset : assets) {
            if (asset.getName() != null && asset.getName().toLowerCase().contains(name.trim().toLowerCase())) {
                result.add(asset);
            }
        }
        return result;
    }

    public ArrayList<Asset> listAvailableAssets(AssetType type) {
        ArrayList<Asset> result = new ArrayList<Asset>();
        for (Asset asset : assets) {
            if (asset.isAvailable() && (type == null || asset.getType() == type)) {
                result.add(asset);
            }
        }
        return result;
    }

    public ArrayList<Asset> listBorrowedAssets() {
        ArrayList<Asset> result = new ArrayList<Asset>();
        for (Asset asset : assets) {
            if (!asset.isAvailable()) {
                result.add(asset);
            }
        }
        return result;
    }

    public ArrayList<Asset> searchBorrowedAssetsByStudentName(String name) {
        ArrayList<Asset> result = new ArrayList<Asset>();
        for (Asset asset : assets) {
            if (asset.isAvailable() || asset.getBorrowedBy() == null) {
                continue;
            }
            Student student = findStudentById(asset.getBorrowedBy());
            if (student != null && name != null
                    && student.getName().toLowerCase().contains(name.trim().toLowerCase())) {
                result.add(asset);
            }
        }
        return result;
    }

    public boolean borrowAsset(int assetId, int studentId, LocalDate plannedReturnDate) {
        Asset asset = findAssetById(assetId);
        Student student = findStudentById(studentId);
        if (asset == null || student == null) {
            return false;
        }
        return asset.borrow(student, plannedReturnDate);
    }

    public boolean returnAsset(int assetId) {
        Asset asset = findAssetById(assetId);
        if (asset == null) {
            return false;
        }
        return asset.returnAsset();
    }

    private int generateNextAssetId() {
        int maxId = 0;
        for (Asset asset : assets) {
            maxId = Math.max(maxId, asset.getId());
        }
        return maxId + 1;
    }
}
