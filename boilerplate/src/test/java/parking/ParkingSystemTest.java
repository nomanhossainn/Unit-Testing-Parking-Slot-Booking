package parking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ParkingSystemTest {

    ParkingSystem system;
    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime end   = LocalDateTime.of(2025, 1, 1, 12, 0); // 2 hours

    @BeforeEach
    void setUp() {
        system = ParkingSystem.getInstance();
        system.resetForTesting();
    }

    // ---------- Happy path ----------

    @Test
    void book_happyPath_correctAmountAndBalances() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);

        // 2 hours * rate 10.0 * CAR(1.0) * REGULAR(1.0) = 20.0
        Booking b = system.book(car, slot, start, end);

        assertEquals(20.0, b.getAmount());
        assertEquals(BookingStatus.ACTIVE, b.getBookingStatus());
        assertEquals(980.0, car.getBalance());   // 1000 - 20
        assertEquals(20.0, system.getBalance());
    }

    @Test
    void book_endBeforeStart_throwsIllegalBookingTimeException() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        assertThrows(IllegalBookingTimeException.class,
                () -> system.book(car, slot, end, start));
    }

    @Test
    void book_endEqualsStart_throwsIllegalBookingTimeException() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        assertThrows(IllegalBookingTimeException.class,
                () -> system.book(car, slot, start, start));
    }

    @Test
    void book_incompatibleSlot_throwsIllegalArgumentException() {
        Vehicle bus = new Vehicle(1, VehicleType.BUS, 1000.0);
        ParkingSlot compactSlot = new ParkingSlot("S1", ParkingSlotType.COMPACT);
        assertThrows(IllegalArgumentException.class,
                () -> system.book(bus, compactSlot, start, end));
    }

    // ---------- Suspicious: partial-hour billing ----------

    @Test
    void book_durationUnderOneHour_defectSuspected() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);

        LocalDateTime shortEnd = start.plusMinutes(30); // valid, but < 1 hour

        // Duration.toHours() truncates 30min → 0 hours → amount = 0
        // transferFunds(amount=0) throws InvalidAmountException
        assertThrows(InvalidAmountException.class,
                () -> system.book(car, slot, start, shortEnd));
    }

    @Test
    void book_underOneHour_leavesInconsistentState_defectSuspected() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);

        LocalDateTime shortEnd = start.plusMinutes(30);

        assertThrows(InvalidAmountException.class,
                () -> system.book(car, slot, start, shortEnd));

        // Defect: booking was already added to the system's list BEFORE the failed transfer
        assertEquals(1, system.getBookings().size());
        // ...but never reached the slot's own list, since that line runs after the exception
        assertEquals(0, slot.getBookings().size());
    }

    @Test
    void book_oneAndHalfHours_chargesOnlyOneHour_defectSuspected() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);

        LocalDateTime end90 = start.plusMinutes(90); // 1.5 hours
        Booking b = system.book(car, slot, start, end90);

        // Expected if rounded up: 2 hours * 10 = 20.0
        // Actual (toHours() truncates): 1 hour * 10 = 10.0
        assertEquals(10.0, b.getAmount());
    }

    // ---------- completeBooking / cancelBooking ----------

    @Test
    void completeBooking_movesEightyPercentToSlot() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);
        Booking b = system.book(car, slot, start, end); // amount 20.0

        system.completeBooking(b);

        assertEquals(BookingStatus.COMPLETED, b.getBookingStatus());
        assertEquals(16.0, slot.getBalance());  // 80% of 20
        assertEquals(4.0, system.getBalance());  // system keeps 20%
    }

    @Test
    void cancelBooking_refundsNinetyPercentToVehicle() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);
        Booking b = system.book(car, slot, start, end); // amount 20.0, car balance now 980

        system.cancelBooking(b);

        assertEquals(BookingStatus.CANCELLED, b.getBookingStatus());
        assertEquals(998.0, car.getBalance());   // 980 + 18 (90% of 20)
        assertEquals(2.0, system.getBalance());   // system keeps 10%
    }

    @Test
    void completeBooking_calledTwice_defectSuspected_insufficientFundsOnSecondCall() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);
        Booking b = system.book(car, slot, start, end); // amount 20.0

        system.completeBooking(b); // first call succeeds, slot gets 16.0

        // Defect: completeBooking() has no guard against being called twice on
        // the same booking. Here the second call happens to throw because the
        // system's escrow wallet is depleted — but that's incidental, not an
        // intentional safety check. If the system wallet had enough balance
        // (e.g. from another booking), the slot would silently get paid twice.
        assertThrows(InsufficientFundsException.class, () -> system.completeBooking(b));
        assertEquals(16.0, slot.getBalance()); // only the first payout went through
    }

    @Test
    void cancelBooking_afterComplete_defectSuspected_insufficientEscrow() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);
        Booking b = system.book(car, slot, start, end); // amount 20.0, escrow(system) = 20

        system.completeBooking(b); // system pays 16 to slot, system balance now = 4

        // Now cancelling tries to refund 90% of 20 = 18, but system wallet only has 4 left
        assertThrows(InsufficientFundsException.class,
                () -> system.cancelBooking(b));
    }

    // ---------- Pricing multipliers (per documentation table) ----------

    @Test
    void book_motorcycle_appliesHalfRateMultiplier() {
        Vehicle bike = new Vehicle(1, VehicleType.MOTORCYCLE, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR); // multiplier 1.0
        system.addVehicle(bike);
        system.addParkingSlot(slot);

        // 2 hours * 10.0 * MOTORCYCLE(0.5) * REGULAR(1.0) = 10.0
        Booking b = system.book(bike, slot, start, end);
        assertEquals(10.0, b.getAmount());
    }

    @Test
    void book_bus_appliesDoubleRateMultiplier() {
        Vehicle bus = new Vehicle(1, VehicleType.BUS, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.LARGE); // multiplier 1.5
        system.addVehicle(bus);
        system.addParkingSlot(slot);

        // 2 hours * 10.0 * BUS(2.0) * LARGE(1.5) = 60.0
        Booking b = system.book(bus, slot, start, end);
        assertEquals(60.0, b.getAmount());
    }

    @Test
    void book_bicycle_appliesLowestRateMultiplier() {
        Vehicle bicycle = new Vehicle(1, VehicleType.BICYCLE, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.COMPACT); // multiplier 0.8
        system.addVehicle(bicycle);
        system.addParkingSlot(slot);

        // 2 hours * 10.0 * BICYCLE(0.2) * COMPACT(0.8) = 3.2
        Booking b = system.book(bicycle, slot, start, end);
        assertEquals(3.2, b.getAmount(), 0.0001);
    }

    @Test
    void book_microcar_inCompactSlot_appliesCorrectMultipliers() {
        Vehicle microcar = new Vehicle(1, VehicleType.MICROCAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.COMPACT); // multiplier 0.8
        system.addVehicle(microcar);
        system.addParkingSlot(slot);

        // 2 hours * 10.0 * MICROCAR(1.5) * COMPACT(0.8) = 24.0
        Booking b = system.book(microcar, slot, start, end);
        assertEquals(24.0, b.getAmount());
    }

    @Test
    void book_bicycle_inHandicappedSlot_appliesHandicappedMultiplier() {
        // HANDICAPPED slot type is only ever reachable via BICYCLE per the
        // compatibility matrix — this is the only test path that can exercise
        // the HANDICAPPED=1.2 multiplier at all.
        Vehicle bicycle = new Vehicle(1, VehicleType.BICYCLE, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.HANDICAPPED); // multiplier 1.2
        system.addVehicle(bicycle);
        system.addParkingSlot(slot);

        // 2 hours * 10.0 * BICYCLE(0.2) * HANDICAPPED(1.2) = 4.8
        Booking b = system.book(bicycle, slot, start, end);
        assertEquals(4.8, b.getAmount(), 0.0001);
    }

    // ---------- TRUCK: never bookable — defect suspected ----------

    @Test
    void book_truck_compactSlot_throwsIllegalArgumentException_defectSuspected() {
        Vehicle truck = new Vehicle(1, VehicleType.TRUCK, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.COMPACT);
        assertThrows(IllegalArgumentException.class, () -> system.book(truck, slot, start, end));
    }

    @Test
    void book_truck_regularSlot_throwsIllegalArgumentException_defectSuspected() {
        Vehicle truck = new Vehicle(1, VehicleType.TRUCK, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        assertThrows(IllegalArgumentException.class, () -> system.book(truck, slot, start, end));
    }

    @Test
    void book_truck_largeSlot_throwsIllegalArgumentException_defectSuspected() {
        // Documentation defines a pricing rate for TRUCK (3.0) but isCompatible()
        // has no case for TRUCK at all, so it always falls through to `default:
        // return false`. Even the LARGE slot — normally the "biggest vehicle"
        // slot type — rejects TRUCK. A TRUCK vehicle can never be booked into
        // any parking slot in the system.
        Vehicle truck = new Vehicle(1, VehicleType.TRUCK, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.LARGE);
        assertThrows(IllegalArgumentException.class, () -> system.book(truck, slot, start, end));
    }

    @Test
    void book_truck_handicappedSlot_throwsIllegalArgumentException_defectSuspected() {
        Vehicle truck = new Vehicle(1, VehicleType.TRUCK, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.HANDICAPPED);
        assertThrows(IllegalArgumentException.class, () -> system.book(truck, slot, start, end));
    }

    // ---------- getAvailableParkingSlots ----------

    @Test
    void getAvailableParkingSlots_returnsOnlyCompatibleAndFreeSlots() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot regular = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        ParkingSlot compact = new ParkingSlot("S2", ParkingSlotType.COMPACT); // incompatible with CAR
        ParkingSlot large = new ParkingSlot("S3", ParkingSlotType.LARGE);
        system.addParkingSlot(regular);
        system.addParkingSlot(compact);
        system.addParkingSlot(large);

        java.util.List<ParkingSlot> available = system.getAvailableParkingSlots(car, start, end);

        assertTrue(available.contains(regular));
        assertTrue(available.contains(large));
        assertFalse(available.contains(compact));
        assertEquals(2, available.size());
    }

    @Test
    void getAvailableParkingSlots_excludesAlreadyBookedSlotForOverlappingWindow() {
        Vehicle car1 = new Vehicle(1, VehicleType.CAR, 1000.0);
        Vehicle car2 = new Vehicle(2, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car1);
        system.addVehicle(car2);
        system.addParkingSlot(slot);

        system.book(car1, slot, start, end); // occupies 10:00-12:00

        java.util.List<ParkingSlot> available = system.getAvailableParkingSlots(car2, start, end);
        assertFalse(available.contains(slot));
    }

    @Test
    void getAvailableParkingSlots_excludesInactiveSlot() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.deactivate();
        system.addParkingSlot(slot);

        java.util.List<ParkingSlot> available = system.getAvailableParkingSlots(car, start, end);
        assertFalse(available.contains(slot));
    }

    // ---------- Singleton behavior ----------

    @Test
    void getInstance_alwaysReturnsSameInstance() {
        ParkingSystem a = ParkingSystem.getInstance();
        ParkingSystem b = ParkingSystem.getInstance();
        assertSame(a, b);
    }

    // ---------- Setters / resetForTesting ----------

    @Test
    void resetForTesting_clearsVehiclesSlotsBookingsAndRestoresDefaults() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);
        system.book(car, slot, start, end);
        system.setPARKING_RATE_PER_HOUR(99.0);

        system.resetForTesting();

        assertTrue(system.getVehicles().isEmpty());
        assertTrue(system.getParkingSlots().isEmpty());
        assertTrue(system.getBookings().isEmpty());
        assertEquals(10.0, system.getPARKING_RATE_PER_HOUR());
        assertEquals(0.0, system.getBalance());
    }

    @Test
    void setPARKING_RATE_PER_HOUR_affectsSubsequentBookingPrice() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car);
        system.addParkingSlot(slot);
        system.setPARKING_RATE_PER_HOUR(5.0);

        // 2 hours * 5.0 * CAR(1.0) * REGULAR(1.0) = 10.0
        Booking b = system.book(car, slot, start, end);
        assertEquals(10.0, b.getAmount());
    }

    @Test
    void setSYSTEM_WALLET_replacesWalletUsedForFutureTransfers() {
        Wallet preloaded = new Wallet(500.0);
        system.setSYSTEM_WALLET(preloaded);

        assertEquals(500.0, system.getBalance());
    }

    @Test
    void setVehicles_and_setParkingSlots_and_setBookings_replaceBackingLists() {
        Vehicle car = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);

        system.setVehicles(java.util.List.of(car));
        system.setParkingSlots(java.util.List.of(slot));

        assertEquals(1, system.getVehicles().size());
        assertEquals(1, system.getParkingSlots().size());
        assertSame(car, system.getVehicles().get(0));
        assertSame(slot, system.getParkingSlots().get(0));
    }

    // ---------- Double-booking prevention at ParkingSystem level ----------

    @Test
    void book_sameSlotOverlappingWindow_secondBookingThrowsIllegalArgumentException() {
        Vehicle car1 = new Vehicle(1, VehicleType.CAR, 1000.0);
        Vehicle car2 = new Vehicle(2, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car1);
        system.addVehicle(car2);
        system.addParkingSlot(slot);

        system.book(car1, slot, start, end); // occupies 10:00-12:00

        assertThrows(IllegalArgumentException.class,
                () -> system.book(car2, slot, start.plusMinutes(30), end.plusMinutes(30)));
    }

    @Test
    void book_sameSlotNonOverlappingWindow_secondBookingSucceeds() {
        Vehicle car1 = new Vehicle(1, VehicleType.CAR, 1000.0);
        Vehicle car2 = new Vehicle(2, VehicleType.CAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        system.addVehicle(car1);
        system.addVehicle(car2);
        system.addParkingSlot(slot);

        system.book(car1, slot, start, end); // 10:00-12:00
        Booking b2 = system.book(car2, slot, end, end.plusHours(2)); // 12:00-14:00, touches but no overlap

        assertEquals(BookingStatus.ACTIVE, b2.getBookingStatus());
        assertEquals(2, slot.getBookings().size());
    }

    // ---------- Suspicious: book() doesn't require registration ----------

    @Test
    void book_withUnregisteredVehicleAndSlot_stillSucceeds_defectSuspected() {
        // Neither addVehicle() nor addParkingSlot() was called for these objects,
        // yet book() has no check that they belong to the system's own lists.
        // The vehicle/slot registration mechanism appears to be purely advisory.
        Vehicle unregisteredCar = new Vehicle(1, VehicleType.CAR, 1000.0);
        ParkingSlot unregisteredSlot = new ParkingSlot("S1", ParkingSlotType.REGULAR);

        Booking b = system.book(unregisteredCar, unregisteredSlot, start, end);

        assertEquals(BookingStatus.ACTIVE, b.getBookingStatus());
        assertFalse(system.getVehicles().contains(unregisteredCar));
        assertFalse(system.getParkingSlots().contains(unregisteredSlot));
    }

    // ---------- ParkingSlot getters (routed through ParkingSystem-created slots) ----------

    @Test
    void getSlotId_returnsIdPassedToConstructor() {
        ParkingSlot slot = new ParkingSlot("S99", ParkingSlotType.LARGE);
        assertEquals("S99", slot.getSlotId());
    }

    @Test
    void getSlotType_returnsTypePassedToConstructor() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.HANDICAPPED);
        assertEquals(ParkingSlotType.HANDICAPPED, slot.getSlotType());
    }

    @Test
    void newSlot_hasZeroBalance() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        assertEquals(0.0, slot.getBalance());
    }

    @Test
    void getBalance_reflectsWalletFunding() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getWallet().addFunds(15.0);
        assertEquals(15.0, slot.getBalance());
    }

    @Test
    void isCompatible_truck_everySlotType_alwaysFalse_defectSuspected() {
        // D9 (direct unit test): isCompatible()'s switch has no case for TRUCK,
        // so every slot type falls through to `default: return false`, even
        // though the documentation defines a pricing rate for TRUCK vehicles.
        for (ParkingSlotType type : ParkingSlotType.values()) {
            ParkingSlot slot = new ParkingSlot("S1", type);
            assertFalse(slot.isCompatible(VehicleType.TRUCK, start, end),
                    "TRUCK should not be compatible with " + type + " per current code, but documentation implies it should be bookable");
        }
    }

    // ---------- Remaining pricing multiplier combinations ----------

    @Test
    void book_motorcycle_compactSlot_appliesCorrectMultipliers() {
        Vehicle bike = new Vehicle(1, VehicleType.MOTORCYCLE, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.COMPACT); // 0.8
        system.addVehicle(bike);
        system.addParkingSlot(slot);
        // 2 * 10.0 * 0.5 * 0.8 = 8.0
        assertEquals(8.0, system.book(bike, slot, start, end).getAmount());
    }

    @Test
    void book_motorcycle_largeSlot_appliesCorrectMultipliers() {
        Vehicle bike = new Vehicle(1, VehicleType.MOTORCYCLE, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.LARGE); // 1.5
        system.addVehicle(bike);
        system.addParkingSlot(slot);
        // 2 * 10.0 * 0.5 * 1.5 = 15.0
        assertEquals(15.0, system.book(bike, slot, start, end).getAmount());
    }

    @Test
    void book_microcar_regularSlot_appliesCorrectMultipliers() {
        Vehicle microcar = new Vehicle(1, VehicleType.MICROCAR, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR); // 1.0
        system.addVehicle(microcar);
        system.addParkingSlot(slot);
        // 2 * 10.0 * 1.5 * 1.0 = 30.0
        assertEquals(30.0, system.book(microcar, slot, start, end).getAmount());
    }

    @Test
    void book_bicycle_regularSlot_appliesCorrectMultipliers() {
        Vehicle bicycle = new Vehicle(1, VehicleType.BICYCLE, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR); // 1.0
        system.addVehicle(bicycle);
        system.addParkingSlot(slot);
        // 2 * 10.0 * 0.2 * 1.0 = 4.0
        assertEquals(4.0, system.book(bicycle, slot, start, end).getAmount());
    }

    @Test
    void book_bicycle_largeSlot_appliesCorrectMultipliers() {
        Vehicle bicycle = new Vehicle(1, VehicleType.BICYCLE, 1000.0);
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.LARGE); // 1.5
        system.addVehicle(bicycle);
        system.addParkingSlot(slot);
        // 2 * 10.0 * 0.2 * 1.5 = 6.0
        assertEquals(6.0, system.book(bicycle, slot, start, end).getAmount());
    }
    @Test
    void book_multipleBookings_assignsSequentialBookingIds() {
        Vehicle car1 = new Vehicle(1, VehicleType.CAR, 1000.0);
        Vehicle car2 = new Vehicle(2, VehicleType.CAR, 1000.0);
        ParkingSlot slot1 = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        ParkingSlot slot2 = new ParkingSlot("S2", ParkingSlotType.REGULAR);
        system.addVehicle(car1);
        system.addVehicle(car2);
        system.addParkingSlot(slot1);
        system.addParkingSlot(slot2);

        Booking first = system.book(car1, slot1, start, end);
        Booking second = system.book(car2, slot2, start, end);

        assertEquals(1, first.getBookingId());
        assertEquals(2, second.getBookingId());
    }

    @Test
    void getSYSTEM_WALLET_returnsLiveSystemWalletReference() {
        Wallet w = system.getSYSTEM_WALLET();
        assertNotNull(w);
        assertEquals(system.getBalance(), w.getBalance());

        w.addFunds(50.0); // mutate directly through the returned reference
        assertEquals(50.0, system.getBalance()); // confirms it's the SAME object, not a copy
    }
}