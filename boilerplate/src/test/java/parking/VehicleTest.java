package parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    // ---------- Constructors ----------

    @Test
    void constructorWithWallet_storesGivenWalletReference() {
        Wallet w = new Wallet(500.0);
        Vehicle v = new Vehicle(1, VehicleType.CAR, w);

        assertSame(w, v.getWallet());
        assertEquals(500.0, v.getBalance());
    }

    @Test
    void constructorWithDoubleBalance_createsNewWalletWithThatBalance() {
        Vehicle v = new Vehicle(2, VehicleType.MOTORCYCLE, 250.0);

        assertNotNull(v.getWallet());
        assertEquals(250.0, v.getBalance());
    }

    // ---------- Getters ----------

    @Test
    void getVehicleId_returnsIdPassedToConstructor() {
        Vehicle v = new Vehicle(42, VehicleType.TRUCK, 0.0);
        assertEquals(42, v.getVehicleId());
    }

    @Test
    void getVehicleType_returnsTypePassedToConstructor() {
        Vehicle v = new Vehicle(1, VehicleType.BUS, 0.0);
        assertEquals(VehicleType.BUS, v.getVehicleType());
    }

    @Test
    void getBalance_reflectsLiveWalletState() {
        Wallet w = new Wallet(75.0);
        Vehicle v = new Vehicle(1, VehicleType.BICYCLE, w);

        w.addFunds(25.0); // mutate wallet directly, not through Vehicle
        assertEquals(100.0, v.getBalance());
    }

    // ---------- Suspicious behavior ----------

    @Test
    void twoVehiclesSharingSameWalletInstance_seeEachOthersBalanceChanges_defectSuspected() {
        // The Wallet-accepting constructor stores the exact reference passed in,
        // with no defensive copy. Nothing stops two Vehicle objects from sharing
        // one Wallet, so spending by one vehicle silently affects the other's balance.
        Wallet shared = new Wallet(100.0);
        Vehicle v1 = new Vehicle(1, VehicleType.CAR, shared);
        Vehicle v2 = new Vehicle(2, VehicleType.CAR, shared);

        v1.getWallet().deductFunds(40.0);

        assertEquals(60.0, v1.getBalance());
        assertEquals(60.0, v2.getBalance()); // v2's balance changed too — likely unintended
    }

    @Test
    void constructorWithDoubleBalance_negativeInitialBalance_acceptedWithoutValidation_defectSuspected() {
        // Wallet(double balance) does not validate its argument, unlike addFunds()/
        // deductFunds(), which throw InvalidAmountException for non-positive amounts.
        // This lets a vehicle start with a negative balance through this constructor.
        Vehicle v = new Vehicle(1, VehicleType.CAR, -50.0);
        assertEquals(-50.0, v.getBalance());
    }

    @Test
    void constructorWithDoubleBalance_zeroInitialBalance_accepted() {
        Vehicle v = new Vehicle(1, VehicleType.CAR, 0.0);
        assertEquals(0.0, v.getBalance());
    }

    @Test
    void toString_containsVehicleIdTypeAndBalance() {
        Vehicle v = new Vehicle(9, VehicleType.MICROCAR, 15.5);
        String s = v.toString();

        assertTrue(s.contains("9"));
        assertTrue(s.contains("MICROCAR"));
        assertTrue(s.contains("15.5"));
    }

}