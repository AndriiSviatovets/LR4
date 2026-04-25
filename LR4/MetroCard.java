public class MetroCard {
    private String cardId;
    private String studentName;
    private double balance;

    public MetroCard(String cardId, String studentName) {
        this.cardId = cardId;
        this.studentName = studentName;
        this.balance = 0.0; // Початковий баланс
    }

    public String getCardId() { return cardId; }
    public String getStudentName() { return studentName; }
    public double getBalance() { return balance; }

    // Синхронізовані методи для уникнення проблем при одночасному доступі
    public synchronized void addFunds(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public synchronized boolean payTrip(double fare) {
        if (this.balance >= fare) {
            this.balance -= fare;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Card [" + cardId + "] Student: " + studentName + ", Balance: " + balance + " UAH";
    }
}