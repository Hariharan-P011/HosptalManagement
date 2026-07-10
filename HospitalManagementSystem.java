import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hospital Management System
 * Java translation of the original C++ project.
 * Uses Queue, PriorityQueue, HashMap, and ArrayList to manage
 * outpatient, inpatient, and emergency patient workflows.
 */
public class HospitalManagementSystem {

    // ---------------------------------------------------------------
    // Data classes (equivalent to the C++ structs)
    // ---------------------------------------------------------------
    static class Patient {
        int id;
        String name;
        String phone;
        String dob;
        int age;
    }

    static class TempBuffer {
        boolean active = false;
        boolean isEmergency = false;
        boolean processed = false;
        boolean inpatient = false;
        List<String> records = new ArrayList<>();
        double medAmount = 0;
        int daysAdmitted = 0;
    }

    // ---------------------------------------------------------------
    // Global data structures
    // ---------------------------------------------------------------
    static Queue<Integer> outpatientQueue = new LinkedList<>();
    // Java's PriorityQueue is a min-heap by default; reverseOrder() makes it
    // behave like C++'s priority_queue<int>, which is a max-heap.
    static PriorityQueue<Integer> emergencyQueue = new PriorityQueue<>(Collections.reverseOrder());
    static Queue<Integer> inpatientQueue = new LinkedList<>();

    static final String PATIENT_FILE = "patients.txt";
    static final String HISTORY_FILE = "history.txt";

    static Map<Integer, TempBuffer> buffers = new HashMap<>();

    static Scanner sc = new Scanner(System.in);

    // ---------------------------------------------------------------
    // Utility Functions
    // ---------------------------------------------------------------
    static boolean isUniqueId(int id) {
        File f = new File(PATIENT_FILE);
        if (!f.exists()) return true;
        try (Scanner fin = new Scanner(f)) {
            while (fin.hasNextInt()) {
                int pid = fin.nextInt();
                fin.next();      // name
                fin.next();      // phone
                fin.next();      // dob
                fin.nextInt();   // age
                if (pid == id) return false;
            }
        } catch (FileNotFoundException e) {
            return true;
        }
        return true;
    }

    static boolean patientExists(int id) {
        File f = new File(PATIENT_FILE);
        if (!f.exists()) return false;
        try (Scanner fin = new Scanner(f)) {
            while (fin.hasNextInt()) {
                int pid = fin.nextInt();
                fin.next();
                fin.next();
                fin.next();
                fin.nextInt();
                if (pid == id) return true;
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        return false;
    }

    static boolean isUniquePhone(String phone) {
        File f = new File(PATIENT_FILE);
        if (!f.exists()) return true;
        try (Scanner fin = new Scanner(f)) {
            while (fin.hasNextInt()) {
                fin.nextInt();          // id
                fin.next();             // name
                String ph = fin.next(); // phone
                fin.next();             // dob
                fin.nextInt();          // age
                if (ph.equals(phone)) return false;
            }
        } catch (FileNotFoundException e) {
            return true;
        }
        return true;
    }

    static boolean isProcessed(int id) {
        if (outpatientQueue.contains(id)) return true;
        if (emergencyQueue.contains(id)) return true;
        if (inpatientQueue.contains(id)) return true;
        return false;
    }

    // ---------------------------------------------------------------
    // 1. Add Patient
    // ---------------------------------------------------------------
    static void addPatient() {
        Patient p = new Patient();
        System.out.print("\nEnter Patient ID: ");
        p.id = sc.nextInt();

        if (!isUniqueId(p.id)) {
            System.out.println("Patient ID already exists.");
            return;
        }

        System.out.print("Enter Name: ");
        p.name = sc.next();

        System.out.print("Enter Phone Number: ");
        p.phone = sc.next();

        if (!isUniquePhone(p.phone)) {
            System.out.println("Phone number already exists.");
            return;
        }

        System.out.print("Enter DOB (dd-mm-yyyy): ");
        p.dob = sc.next();

        System.out.print("Enter Age: ");
        p.age = sc.nextInt();

        try (FileWriter fout = new FileWriter(PATIENT_FILE, true)) {
            fout.write(p.id + " " + p.name + " " + p.phone + " " + p.dob + " " + p.age + "\n");
        } catch (IOException e) {
            System.out.println("Error writing patient file: " + e.getMessage());
            return;
        }
        System.out.println("Patient added successfully.");
    }

    // ---------------------------------------------------------------
    // 2. Search Patient
    // ---------------------------------------------------------------
    static void searchPatient() {
        System.out.print("\nEnter phone number to search: ");
        String phone = sc.next();

        File f = new File(PATIENT_FILE);
        if (!f.exists()) {
            System.out.println("No patient records found.");
            return;
        }

        boolean found = false;
        try (Scanner fin = new Scanner(f)) {
            while (fin.hasNextInt()) {
                int id = fin.nextInt();
                String name = fin.next();
                String ph = fin.next();
                fin.next();     // dob
                fin.nextInt();  // age
                if (ph.equals(phone)) {
                    System.out.println("\nPatient Found:");
                    System.out.println("ID: " + id + "\nName: " + name);
                    found = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No patient records found.");
            return;
        }

        if (!found) {
            System.out.println("No record found for phone number: " + phone);
        }
    }

    // ---------------------------------------------------------------
    // 3. Process Patient
    // ---------------------------------------------------------------
    static void processPatient() {
        System.out.print("\nEnter Patient ID to process: ");
        int id = sc.nextInt();

        if (!patientExists(id)) {
            System.out.println("This patient is not registered. Please add the patient first.");
            return;
        }

        System.out.print("Type of patient (a - Outpatient, b - Emergency): ");
        String typeStr = sc.next();
        char type = Character.toLowerCase(typeStr.charAt(0));

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

    // ---------------------------------------------------------------
    // Date & History Functions
    // ---------------------------------------------------------------
    static String getCurrentDateTime() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return LocalDateTime.now().format(fmt);
    }

    static boolean appendHistoryRecord(int id, String record) {
        try (FileWriter fout = new FileWriter(HISTORY_FILE, true)) {
            fout.write(id + "|" + getCurrentDateTime() + "|" + record + "\n");
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------
    // 7. Emergency Buffer with multiple Temp IDs
    // ---------------------------------------------------------------
    static void emergencyBufferStart() {
        System.out.print("\nEnter Temporary Buffer ID: ");
        int tempId = sc.nextInt();

        if (buffers.containsKey(tempId) && buffers.get(tempId).active) {
            System.out.println("This temporary buffer ID already exists. Choose a different one.");
            return;
        }

        TempBuffer buf = new TempBuffer();
        buf.active = true;
        buf.isEmergency = true;
        buf.processed = true;
        buf.inpatient = true;
        buf.records.clear();
        buf.medAmount = 0;
        buf.daysAdmitted = 0;
        buffers.put(tempId, buf);

        System.out.println("\n*** Emergency Buffer Activated for Temp ID " + tempId + " ***");

        sc.nextLine(); // consume leftover newline before getline-style reads
        while (true) {
            System.out.print("\nEnter treatment name for buffer (or just press ENTER to stop): ");
            String treatment = sc.nextLine();
            if (treatment.isEmpty()) break;
            String rec = "Emergency Treatment: " + treatment;
            buf.records.add(rec);
            System.out.println("Added to buffer.");
        }

        System.out.println("\nBuffer setup complete for Temp ID " + tempId + ".");
    }

    // ---------------------------------------------------------------
    // Medication for normal patients
    // ---------------------------------------------------------------
    static void medication() {
        System.out.print("\nEnter Patient ID for medication: ");
        int id = sc.nextInt();
        sc.nextLine(); // consume newline

        if (!patientExists(id)) {
            System.out.println("This patient is not registered. Please add first.");
            return;
        }

        if (!isProcessed(id)) {
            System.out.println("Patient not yet processed. Please process before medication.");
            return;
        }

        boolean isEmergency = emergencyQueue.contains(id);

        if (isEmergency) {
            System.out.println("\n*** Emergency Patient Detected ***");
            System.out.print("Enter treatment name: ");
            String treatment = sc.nextLine();
            String record = "Emergency Treatment: " + treatment;
            appendHistoryRecord(id, record);

            if (!inpatientQueue.contains(id)) {
                inpatientQueue.add(id);
            }
            System.out.println("Emergency treatment record saved. Patient added to inpatient queue.");
            return;
        }

        System.out.print("Enter doctor's prescription: ");
        String pres = sc.nextLine();
        appendHistoryRecord(id, pres);

        System.out.print("Enter 'c' if patient is inpatient: ");
        String chStr = sc.nextLine();
        char ch = chStr.isEmpty() ? ' ' : Character.toLowerCase(chStr.charAt(0));
        if (ch == 'c') {
            inpatientQueue.add(id);
            System.out.println("Patient added to Inpatient Queue.");
        }
        System.out.println("Prescription saved successfully.");
    }

    // ---------------------------------------------------------------
    // Get latest prescription
    // ---------------------------------------------------------------
    static String getLatestPrescription(int id) {
        File f = new File(HISTORY_FILE);
        if (!f.exists()) return "No prescription found.";

        String lastPres = "";
        try (BufferedReader fin = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = fin.readLine()) != null) {
                if (line.startsWith(id + "|") && !line.contains("Billed")) {
                    lastPres = line;
                }
            }
        } catch (IOException e) {
            return "No prescription found.";
        }

        if (lastPres.isEmpty()) return "No prescription found.";
        int pos = lastPres.lastIndexOf('|');
        return (pos != -1) ? lastPres.substring(pos + 1) : "No prescription found.";
    }

    // ---------------------------------------------------------------
    // Billing For ID
    // ---------------------------------------------------------------
    static void billingForId(int id) {
        if (!patientExists(id)) {
            System.out.println("This patient is not registered. Please add the patient first.");
            return;
        }

        if (!isProcessed(id)) {
            System.out.println("Patient not processed yet. Please process before billing.");
            return;
        }

        String pres = getLatestPrescription(id);
        if (pres.equals("No prescription found.")) {
            System.out.println("\nBilling not possible - no prescription found for this patient.");
            System.out.println("Please record medication first.");
            return;
        }

        System.out.println("\n--- Prescription / Treatment ---\n" + pres);

        boolean isInpatient = inpatientQueue.contains(id);

        double total;
        double medAmount;

        // NOTE: fixed a formatting bug from the original C++ version, where
        // large bill totals (e.g. 180000000) printed in scientific notation
        // (e.g. "1.8e+008"). Amounts are now always shown as plain decimals.
        if (isInpatient) {
            System.out.print("Enter number of days admitted: ");
            int days = sc.nextInt();
            System.out.print("Enter medication amount: ");
            medAmount = sc.nextDouble();
            total = (1000 * days) + medAmount + (800 * days);
            System.out.printf("%nInpatient Bill: %.2f%n", total);
        } else {
            System.out.print("Enter medication amount: ");
            medAmount = sc.nextDouble();
            total = medAmount + 500;
            System.out.printf("%nOutpatient Bill: %.2f%n", total);
        }
        sc.nextLine(); // consume leftover newline

        // Remove the patient from all active queues after billing
        outpatientQueue.remove((Integer) id);
        emergencyQueue.remove((Integer) id);
        inpatientQueue.remove((Integer) id);

        String billRecord = "Billed " + String.format("%.2f", total) + (isInpatient ? " (Inpatient)" : " (Outpatient)");
        appendHistoryRecord(id, billRecord);

        System.out.println("\nBilling completed and patient removed from all queues.");
    }

    static void billing() {
        System.out.print("\nEnter Patient ID for billing: ");
        int id = sc.nextInt();
        billingForId(id);
    }

    // ---------------------------------------------------------------
    // 8. Assign Buffer to Patient (Temp ID + Patient ID)
    // ---------------------------------------------------------------
    static void assignBufferToPatient() {
        System.out.print("\nEnter Temporary Buffer ID to assign: ");
        int tempId = sc.nextInt();

        if (!buffers.containsKey(tempId) || !buffers.get(tempId).active) {
            System.out.println("No active buffer found with this Temp ID.");
            return;
        }

        System.out.print("Enter Patient ID to assign buffer to: ");
        int id = sc.nextInt();

        if (!patientExists(id)) {
            System.out.println("This patient is not registered. Please add the patient first.");
            return;
        }

        TempBuffer buf = buffers.get(tempId);
        for (String rec : buf.records) {
            appendHistoryRecord(id, rec);
        }

        if (buf.inpatient) inpatientQueue.add(id);
        if (buf.isEmergency) emergencyQueue.add(id);

        System.out.println("\nBuffer records assigned to patient ID " + id + ".");
        System.out.println("\nNow running billing for patient ID " + id + "...");
        billingForId(id);

        buffers.remove(tempId);
        System.out.println("\nTemporary buffer " + tempId + " deleted.");
    }

    // ---------------------------------------------------------------
    // 6. Search History
    // ---------------------------------------------------------------
    static void searchHistory() {
        System.out.print("\nEnter patient ID to view history: ");
        int id = sc.nextInt();

        File f = new File(HISTORY_FILE);
        if (!f.exists()) {
            System.out.println("No history records available.");
            return;
        }

        boolean found = false;
        System.out.println("\n--- History for Patient ID " + id + " ---");
        try (BufferedReader fin = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = fin.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", 3);
                if (parts.length < 3) continue;
                String sid = parts[0];
                String date = parts[1];
                String record = parts[2];
                try {
                    if (!sid.isEmpty() && Integer.parseInt(sid) == id) {
                        System.out.println("Date: " + date + "\nRecord: " + record + "\n");
                        found = true;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("No history records available.");
            return;
        }

        if (!found) {
            System.out.println("No history found for Patient ID: " + id);
        }
    }

    // ---------------------------------------------------------------
    // Show All Buffer Status
    // ---------------------------------------------------------------
    static void showBufferStatus() {
        if (buffers.isEmpty()) {
            System.out.println("\nNo temporary buffers available.");
            return;
        }

        System.out.println("\n--- Temporary Buffers ---");
        for (Map.Entry<Integer, TempBuffer> entry : buffers.entrySet()) {
            int tempId = entry.getKey();
            TempBuffer buf = entry.getValue();
            System.out.println("Temp ID: " + tempId
                    + " | Active: " + (buf.active ? "Yes" : "No")
                    + " | Emergency: " + (buf.isEmergency ? "Yes" : "No")
                    + " | Inpatient: " + (buf.inpatient ? "Yes" : "No")
                    + " | Records: " + buf.records.size()
                    + " | Med Amount: " + buf.medAmount
                    + " | Days Admitted: " + buf.daysAdmitted);
        }
    }

    // ---------------------------------------------------------------
    // Main Menu
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n====== HOSPITAL MANAGEMENT SYSTEM ======");
            System.out.println("1. Add Patient");
            System.out.println("2. Search Patient");
            System.out.println("3. Process Patient");
            System.out.println("4. Medication (by Patient ID)");
            System.out.println("5. Billing");
            System.out.println("6. Search History");
            System.out.println("7. Emergency Buffer (no ID - temporary)");
            System.out.println("8. Assign Buffer to Patient (Temp ID -> Patient ID)");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");

            while (!sc.hasNextInt()) {
                System.out.println("Invalid choice. Try again.");
                sc.next();
                System.out.print("Enter your choice: ");
            }
            choice = sc.nextInt();

            switch (choice) {
                case 1: addPatient(); break;
                case 2: searchPatient(); break;
                case 3: processPatient(); break;
                case 4: medication(); break;
                case 5: billing(); break;
                case 6: searchHistory(); break;
                case 7: emergencyBufferStart(); break;
                case 8: assignBufferToPatient(); break;
                case 9: System.out.println("Exiting program..."); break;
                default: System.out.println("Invalid choice. Try again.");
            }

            if (choice >= 1 && choice <= 9 && choice != 9) {
                showBufferStatus();
            }
        } while (choice != 9);

        sc.close();
    }
}
