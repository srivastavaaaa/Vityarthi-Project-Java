import java.io.*;
import java.util.*;

public class LibraryManagementSystem {
    static final String BOOKS_FILE = "books.csv";
    static final String MEMBERS_FILE = "members.csv";

    public static void main(String[] args) {
        Lib lib = new Lib();
        try { lib.load(); } catch (Exception e) { System.out.println("Couldn't load data — starting fresh."); }

        Scanner in = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println(" Library > choose: 1:add book 2:rm book 3:list all 4:avil 5:reg member 6:issue 7:return 8:members 9:save+exit");
            String ch = in.nextLine().trim();
            switch (ch) {
                case "1": addBookPrompt(in, lib); break;
                case "2": removeBookPrompt(in, lib); break;
                case "3": lib.printAll(); break;
                case "4": lib.printAvailable(); break;
                case "5": registerPrompt(in, lib); break;
                case "6": issuePrompt(in, lib); break;
                case "7": returnPrompt(in, lib); break;
                case "8": lib.printMembers(); break;
                case "9":
                    try { lib.save(); System.out.println("All good — saved."); }
                    catch (Exception e) { System.out.println("Save failed: " + e.getMessage()); }
                    exit = true; break;
                default:
                    System.out.println("Huh? Try one of the numbers.");
            }
        }
        in.close();
    }

    // -- prompts (note: names vary slightly; not a style groomed by linters) --
    private static void addBookPrompt(Scanner sc, Lib lib) {
        System.out.print("Title? "); String t = sc.nextLine().trim();
        System.out.print("Author? "); String a = sc.nextLine().trim();
        System.out.print("ISBN? "); String isbn = sc.nextLine().trim();
        System.out.print("Copies? "); int c = readInt(sc, 1);
        Book b = new Book(isbn, t, a, c);
        boolean added = lib.addOrUpdate(b);
        if (added) System.out.println("Nice — book recorded.");
        else System.out.println("Could not add book. Maybe ISBN clash?");
    }

    private static void removeBookPrompt(Scanner sc, Lib lib) {
        System.out.print("ISBN to remove: "); String is = sc.nextLine().trim();
        try {
            boolean ok = lib.remove(is);
            if (ok) System.out.println("Removed."); else System.out.println("Either not found or copies are loaned out.");
        } catch (Exception e) { System.out.println("Can't remove: " + e.getMessage()); }
    }

    private static void registerPrompt(Scanner sc, Lib lib) {
        System.out.print("Name: "); String name = sc.nextLine().trim();
        System.out.print("Email (optional): "); String email = sc.nextLine().trim();
        System.out.print("Member type (student/faculty/other): "); String type = sc.nextLine().trim();
        Member m = lib.register(name, email, type);
        System.out.println("Welcome — your member id is " + m.id);
        // TODO: send welcome email (lol, later)
    }

    private static void issuePrompt(Scanner sc, Lib lib) {
        System.out.print("Member id: "); String mid = sc.nextLine().trim();
        System.out.print("ISBN: "); String isb = sc.nextLine().trim();
        String res = lib.issue(mid, isb);
        // res encodes richer feedback
        System.out.println(res);
    }

    private static void returnPrompt(Scanner sc, Lib lib) {
        System.out.print("Member id: "); String mid = sc.nextLine().trim();
        System.out.print("ISBN: "); String isb = sc.nextLine().trim();
        String res = lib.receiveReturn(mid, isb);
        System.out.println(res);
    }

    private static int readInt(Scanner sc, int min) {
        while (true) {
            String s = sc.nextLine().trim();
            try { int v = Integer.parseInt(s); if (v < min) throw new NumberFormatException(); return v; }
            catch (NumberFormatException e) { System.out.print("Please enter a number >= " + min + ": "); }
        }
    }
}

// ---- lightweight domain model and logic ----
class Book {
    String isbn;
    String title;
    String author;
    int total;
    int avail;

    Book(String isbn, String t, String a, int copies) {
        this.isbn = isbn; this.title = t; this.author = a; this.total = copies; this.avail = copies;
    }

    void addCopies(int n) { if (n>0) { total += n; avail += n; } }

    boolean borrowOne() { if (avail<=0) return false; avail--; return true; }
    boolean giveBackOne() { if (avail < total) { avail++; return true; } return false; }

    @Override public String toString() { return isbn + " | " + title + " | " + author + " | total=" + total + " avail=" + avail; }
}

class Member {
    String id; String name; String email; String type; // student/faculty/other
    Map<String, Long> loans = new HashMap<>(); // isbn -> epoch day when issued

    Member(String id, String name, String email, String type) { this.id=id; this.name=name; this.email=email; this.type = type; }

    void borrow(String isbn) { loans.put(isbn, System.currentTimeMillis()); }
    boolean returned(String isbn) { return loans.remove(isbn) != null; }

    @Override public String toString() { return id+" | "+name+" | "+type+" | loans="+loans.keySet(); }
}

class Lib {
    Map<String, Book> books = new HashMap<>();
    Map<String, Member> members = new HashMap<>();
    int cntMem = 0; // intentionally short and informal name

    // business rules
    private static final int LOAN_DAYS = 14;
    private static final double FINE_PER_DAY = 1.0; // currency units per day

    // --- operations ---
    boolean addOrUpdate(Book b) {
        if (b==null || b.isbn.isEmpty()) return false;
        if (books.containsKey(b.isbn)) {
            books.get(b.isbn).addCopies(b.total);
        } else {
            books.put(b.isbn, b);
        }
        return true;
    }

    boolean remove(String isbn) throws Exception {
        Book bk = books.get(isbn);
        if (bk==null) return false;
        if (bk.avail != bk.total) throw new Exception("Some copies are currently loaned.");
        books.remove(isbn);
        return true;
    }

    Member register(String name, String email, String type) {
        String id = "MB" + String.format("%03d", ++cntMem);
        Member m = new Member(id, name, email, type);
        members.put(id, m);
        return m;
    }

    void printAll() {
        if (books.isEmpty()) { System.out.println("No books — the shelves are bare."); return; }
        System.out.println("--- Books ---");
        for (Book b : books.values()) System.out.println(b);
    }

    void printAvailable() {
        System.out.println("--- Available ---");
        books.values().stream().filter(b->b.avail>0).forEach(System.out::println);
    }

    void printMembers() {
        System.out.println("--- Members ---");
        if (members.isEmpty()) { System.out.println("No members yet."); return; }
        for (Member m : members.values()) System.out.println(m);
    }

    // returns human-readable message
    String issue(String memberId, String isbn) {
        Member m = members.get(memberId);
        Book b = books.get(isbn);
        if (m==null) return "No such member.";
        if (b==null) return "Book not found.";
        // simple business: students may have up to 3 loans, faculty 10
        int limit = m.type != null && m.type.equalsIgnoreCase("faculty") ? 10 : 3;
        if (m.loans.size() >= limit) return "Loan limit reached ("+limit+"). Return something first.";
        if (!b.borrowOne()) return "Sorry, none available right now.";
        m.borrow(isbn);
        return "Issued — remember to bring it back within " + LOAN_DAYS + " days.";
    }

    String receiveReturn(String memberId, String isbn) {
        Member m = members.get(memberId);
        Book b = books.get(isbn);
        if (m==null) return "Member unknown.";
        if (b==null) return "Hmm, that ISBN is not in our system.";
        Long issuedAt = m.loans.get(isbn);
        if (issuedAt == null) return "This member didn't borrow that book.";
        // calculate fine
        long now = System.currentTimeMillis();
        long days = (now - issuedAt) / (1000L*60*60*24);
        long late = days - LOAN_DAYS;
        double fine = 0.0;
        if (late > 0) {
            fine = late * FINE_PER_DAY;
        }
        boolean removed = m.returned(isbn);
        if (!removed) return "Return failed for unknown reason.";
        b.giveBackOne();
        if (fine > 0) return String.format("Returned. Late by %d day(s). Fine: %.2f", late, fine);
        return "Returned. Thanks — on time!";
    }

    // --- persistence with simple CSV ---
    void save() throws IOException {
        saveBooks(); saveMembers();
    }

    void load() throws IOException {
        loadBooks(); loadMembers();
    }

    private void saveBooks() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LibraryManagementSystem.BOOKS_FILE))) {
            for (Book b : books.values()) {
                // naive CSV, commas in text will break — acceptable for this simple demo
                pw.println(String.join(",", escape(b.isbn), escape(b.title), escape(b.author), String.valueOf(b.total), String.valueOf(b.avail)));
            }
        }
    }

    private void loadBooks() throws IOException {
        File f = new File(LibraryManagementSystem.BOOKS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line; while ((line = br.readLine()) != null) {
                String[] parts = splitCsv(line);
                if (parts.length < 5) continue;
                Book b = new Book(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]));
                b.avail = Integer.parseInt(parts[4]);
                books.put(b.isbn, b);
            }
        }
    }

    private void saveMembers() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LibraryManagementSystem.MEMBERS_FILE))) {
            for (Member m : members.values()) {
                // store loans as semicolon-separated isbn:epoch
                StringBuilder loanSb = new StringBuilder();
                for (Map.Entry<String, Long> e : m.loans.entrySet()) {
                    if (loanSb.length()>0) loanSb.append(";");
                    loanSb.append(e.getKey()).append(":").append(e.getValue());
                }
                pw.println(String.join(",", escape(m.id), escape(m.name), escape(m.email==null?"":m.email), escape(m.type==null?"":m.type), escape(loanSb.toString())));
            }
        }
    }

    private void loadMembers() throws IOException {
        File f = new File(LibraryManagementSystem.MEMBERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line; while ((line = br.readLine()) != null) {
                String[] p = splitCsv(line);
                if (p.length < 5) continue;
                Member m = new Member(p[0], p[1], p[2], p[3]);
                if (p[4] != null && !p[4].isEmpty()) {
                    String[] items = p[4].split(";");
                    for (String it : items) {
                        String[] kv = it.split(":");
                        if (kv.length==2) m.loans.put(kv[0], Long.parseLong(kv[1]));
                    }
                }
                members.put(m.id, m);
                // keep cntMem reasonably ahead of existing ids — assumes format MBNNN
                try { if (m.id.startsWith("MB")) { int v = Integer.parseInt(m.id.substring(2)); if (v > cntMem) cntMem = v; } } catch (Exception ignore) {}
            }
        }
    }

    // crude CSV helpers
    private static String escape(String s) { return s == null ? "" : s.replace(" ","\n").replace(",", "&#44;"); }
        private static String[] splitCsv(String line) {
            // naive inverse of escape: split by comma then replace &#44; back
            String[] parts = line.split(",");
            for (int i=0;i<parts.length;i++) parts[i] = parts[i].replace("&#44;", ",").replace("\n"," ");
            return parts;
        }
    }
