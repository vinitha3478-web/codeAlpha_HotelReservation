/**
 * Simulates a payment gateway for booking payments and cancellation refunds.
 * No real money or network calls are involved - this is for demonstration only.
 */
public class PaymentSimulator {
    private static int transactionCounter = 100001;

    /**
     * Simulates processing a payment and returns a generated transaction ID.
     */
    public static String processPayment(double amount) {
        System.out.println("\n--- Payment Gateway ---");
        System.out.printf("Processing payment of Rs.%.2f ...%n", amount);
        simulateDelay();
        String transactionId = "TXN" + transactionCounter++;
        System.out.println("Payment SUCCESSFUL. Transaction ID: " + transactionId);
        System.out.println("-----------------------\n");
        return transactionId;
    }

    /**
     * Simulates refunding a previous transaction during cancellation.
     */
    public static void processRefund(String originalTransactionId, double amount) {
        System.out.println("\n--- Payment Gateway: Refund ---");
        System.out.printf("Refunding Rs.%.2f for transaction %s ...%n", amount, originalTransactionId);
        simulateDelay();
        System.out.println("Refund SUCCESSFUL.");
        System.out.println("--------------------------------\n");
    }

    private static void simulateDelay() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException ignored) {
        }
    }
}
