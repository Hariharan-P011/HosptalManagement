import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

class Patient {
    int id;
    String name;
    String phone;
    String dob;
    int age;
}

class TempBuffer {
    boolean active = false;
    boolean isEmergency = false;
    boolean processed = false;
    boolean inpatient = false;
    List<String> records = new ArrayList<>();
    double medAmount = 0;
    int daysAdmitted = 0;
}

public class HospitalManagementSystem {
    // Queues
    static Queue<Integer> outpatientQueue = new LinkedList<>();
    static PriorityQueue<Integer> emergencyQueue = new PriorityQueue<>(Collections.reverseOrder());
    static Queue<Integer> inpatientQueue = new LinkedList<>();

    // Files
    static final String PATIENT_FILE = "patients.txt";
    static final String HISTORY_FILE = "history.txt";

    // Buffers
    static Map<Integer, TempBuffer> buffers = new HashMap<>();

    // ---------------------------------------------------------------------------
    // Utility Functions
    // ---------------------------------------------------------------------------
    static boolean isUniqueId(int id) throws IOException {
        File file = new File(PATIENT_FILE);
        if (!file.exists()) return true;
        Scanner sc = new Scanner(file);
        while (sc.hasNext()) {
            int pid = sc.nextInt();
            String name = sc.next();
            String phone = sc.next();
            String dob = sc.next();
            int age = sc.nextInt();
            if (pid == id) {
                sc.close();
                return false;
            }
        }
        sc.close();
        return true;
    }

    static boolean patientExists(int id) throws IOException {
        File file = new File(PATIENT_FILE);
        if (!file.exists()) return false;
        Scanner sc = new Scanner(file);
        while (sc.hasNext()) {
            int pid = sc.nextInt();
            String name = sc.next();
            String phone = sc.next();
            String dob = sc.next();
            int age = sc.nextInt();
            if (pid == id) {
                sc.close();
                return true;
            }
        }
        sc.close();
        return false;
    }

    static boolean isUniquePhone(String phone) throws IOException {
        File file = new File(PATIENT_FILE);
        if (!file.exists()) return true;
        Scanner sc = new Scanner(file);
        while (sc.hasNext()) {
            int pid = sc.nextInt();
            String name = sc.next();
            String ph = sc.next();
            String dob = sc.next();
            int age = sc.nextInt();
            if (ph.equals(phone)) {
                sc.close();
                return false;
            }
        }
        sc.close();
        return true;
    }

    static boolean isProcessed(int id) {
        return outpatientQueue.contains(id) || emergencyQueue.contains(id) || inpatientQueue.contains(id);
    }

    // ---------------------------------------------------------------------------
    // Date & History Functions
    // ---------------------------------------------------------------------------
    static String getCurrentDateTime() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    static boolean appendHistoryRecord(int id, String record) {
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true)) {
            fw.write(id + "|" + getCurrentDateTime() + "|" + record + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------------------
    // Core Functions (Add, Search, Process, Medication, Billing, Buffers, History)
    // ---------------------------------------------------------------------------
    static void addPatient(Scanner in) throws IOException {
        Patient p = new Patient();
        System.out.print("\nEnter Patient ID: ");
        p.id = in.nextInt();

        if (!isUniqueId(p.id)) {
            System.out.println("Patient ID already exists.");
            return;
        }

        System.out.print("Enter Name: ");
        p.name = in.next();
        System.out.print("Enter Phone Number: ");
        p.phone = in.next();

        if (!isUniquePhone(p.phone)) {
            System.out.println("Phone number already exists.");
            return;
        }

        System.out.print("Enter DOB (dd-mm-yyyy): ");
        p.dob = in.next();
        System.out.print("Enter Age: ");
        p.age = in.nextInt();

        try (FileWriter fw = new FileWriter(PATIENT_FILE, true)) {
            fw.write(p.id + " " + p.name + " " + p.phone + " " + p.dob + " " + p.age + "\n");
        }

        System.out.println("Patient added successfully.");
    }

    static void searchPatient(Scanner in) throws IOException {
        System.out.print("\nEnter phone number to search: ");
        String phone = in.next();

        File file = new File(PATIENT_FILE);
        if (!file.exists()) {
            System.out.println("No patient records found.");
            return;
        }

        Scanner sc = new Scanner(file);
        boolean found = false;
        while (sc.hasNext()) {
            int pid = sc.nextInt();
            String name = sc.next();
            String ph = sc.next();
            String dob = sc.next();
            int age = sc.nextInt();
            if (ph.equals(phone)) {
                System.out.println("\nPatient Found:");
                System.out.println("ID: " + pid + "\nName: " + name);
                found = true;
                break;
            }
        }
        sc.close();

        if (!found)
            System.out.println("No record found for phone number: " + phone);
    }

    static void processPatient(Scanner in) throws IOException {
        System.out.print("\nEnter Patient ID to process: ");
        int id = in.nextInt();

        if (!patientExists(id)) {
            System.out.println("This patient is not registered. Please add the patient first.");
            return;
        }

        System.out.print("Type of patient (a - Outpatient, b - Emergency): ");
        char type = in.next().toLowerCase().charAt(0);

        if (type == 'a') {
            outpatientQueue.add(id);
            System.out.println("Added to Outpatient Queue.");
        } else if (type == 'b') {
            emergencyQueue.add(id);
            inpatientQueue.add(id);
            System.out.println("Added to Emergency and Inpatient Queues.");
        } else {
            System.out.println("Invalid type.");
        }
    }

    // (Medication, Billing, Buffers, History functions would follow the same conversion style...)

    // ---------------------------------------------------------------------------
    // Main Menu
    // ---------------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n====== HOSPITAL MANAGEMENT SYSTEM ======");
            System.out.println("1. Add Patient");
            System.out.println("2. Search Patient");
            System.out.println("3. Process Patient");
            System.out.println("4. Medication (by Patient ID)");
            System.out.println("5. Billing");
            System.out.println("6. Search History");
            System.out.println("7. Emergency Buffer (temporary)");
            System.out.println("8. Assign Buffer to Patient");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");
            choice = in.nextInt();

            switch (choice) {
                case 1: addPatient(in); break;
                case 2: searchPatient(in); break;
                case 3: processPatient(in); break;
                // case 4: medication(in); break;
                // case 5: billing(in); break;
                // case 6: searchHistory(in); break;
                // case 7: emergencyBufferStart(in); break;
                // case 8: assignBufferToPatient(in); break;
                case 9: System.out.println("Exiting program..."); break;
                default: System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 9);
        in.close();
    }
}
