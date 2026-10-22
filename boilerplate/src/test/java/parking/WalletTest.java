package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void newWallet_hasZeroBalance() {
        Wallet w = new Wallet();
        assertEquals(0.0, w.getBalance());
    }

    @Test
    void walletWithInitialBalance_hasThatBalance() {
        Wallet w = new Wallet(100.0);
        assertEquals(100.0, w.getBalance());
    }

    @Test
    void addFunds_zeroAmount_throwsInvalidAmountException() {
        Wallet w = new Wallet();
        assertThrows(InvalidAmountException.class, () -> w.addFunds(0));
    }

    @Test
    void deductFunds_exactBalance_succeedsAndLeavesZero() {
        Wallet w = new Wallet(50.0);
        w.deductFunds(50.0);
        assertEquals(0.0, w.getBalance());
    }

    @Test
    void transferFunds_exactBalance_movesEverything() {
        Wallet from = new Wallet(30.0);
        Wallet to = new Wallet(0.0);
        from.transferFunds(to, 30.0);
        assertEquals(0.0, from.getBalance());
        assertEquals(30.0, to.getBalance());
    }

    @Test
    void addFunds_negativeAmount_throwsInvalidAmountException() {
        Wallet w = new Wallet();
        assertThrows(InvalidAmountException.class, () -> w.addFunds(-5));
    }

    @Test
    void deductFunds_negativeAmount_throwsInvalidAmountException() {
        Wallet w = new Wallet(100.0);
        assertThrows(InvalidAmountException.class, () -> w.deductFunds(-5));
    }
    @Test
    void addFunds_positiveAmount_increasesBalance() {
        Wallet w = new Wallet(50.0);
        w.addFunds(25.0);
        assertEquals(75.0, w.getBalance());
    }

    @Test
    void deductFunds_moreThanBalance_throwsInsufficientFundsException() {
        Wallet w = new Wallet(20.0);
        assertThrows(InsufficientFundsException.class, () -> w.deductFunds(50.0));
    }

    @Test
    void transferFunds_moreThanBalance_throwsInsufficientFundsException() {
        Wallet from = new Wallet(10.0);
        Wallet to = new Wallet(0.0);
        assertThrows(InsufficientFundsException.class, () -> from.transferFunds(to, 50.0));
        // Defensive check: failed transfer must not partially move funds
        assertEquals(10.0, from.getBalance());
        assertEquals(0.0, to.getBalance());
    }

    @Test
    void transferFunds_zeroAmount_throwsInvalidAmountException() {
        Wallet from = new Wallet(10.0);
        Wallet to = new Wallet(0.0);
        assertThrows(InvalidAmountException.class, () -> from.transferFunds(to, 0.0));
    }

    @Test
    void transferFunds_negativeAmount_throwsInvalidAmountException() {
        Wallet from = new Wallet(10.0);
        Wallet to = new Wallet(0.0);
        assertThrows(InvalidAmountException.class, () -> from.transferFunds(to, -5.0));
    }

    @Test
    void constructorWithNegativeBalance_acceptedWithoutValidation_defectSuspected() {
        // D8 (direct unit test): Wallet(double) performs no validation, unlike
        // addFunds()/deductFunds() which reject non-positive amounts.
        Wallet w = new Wallet(-100.0);
        assertEquals(-100.0, w.getBalance());
    }
    @Test
    void deductFunds_zeroAmount_throwsInvalidAmountException() {
        Wallet w = new Wallet(50.0);
        assertThrows(InvalidAmountException.class, () -> w.deductFunds(0));
    }
}