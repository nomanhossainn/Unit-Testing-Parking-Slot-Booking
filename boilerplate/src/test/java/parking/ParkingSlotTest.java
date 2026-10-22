package parking;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ParkingSlotTest {

    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime end   = LocalDateTime.of(2025, 1, 1, 12, 0);

    // ---------- basic active/inactive ----------

    @Test
    void newSlot_isActiveByDefault() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        assertTrue(slot.isActive());
    }

    @Test
    void deactivate_makesSlotInactive() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.deactivate();
        assertFalse(slot.isActive());
    }

    @Test
    void activate_makesSlotActiveAgain() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.deactivate();
        slot.activate();
        assertTrue(slot.isActive());
    }

    @Test
    void inactiveSlot_isNeverCompatible_evenIfTypeMatches() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.LARGE);
        slot.deactivate();
        assertFalse(slot.isCompatible(VehicleType.BUS, start, end));
    }

    // ---------- MOTORCYCLE ----------

    @Test
    void motorcycle_compact_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.COMPACT).isCompatible(VehicleType.MOTORCYCLE, start, end));
    }

    @Test
    void motorcycle_regular_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.REGULAR).isCompatible(VehicleType.MOTORCYCLE, start, end));
    }

    @Test
    void motorcycle_large_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.LARGE).isCompatible(VehicleType.MOTORCYCLE, start, end));
    }

    @Test
    void motorcycle_handicapped_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.MOTORCYCLE, start, end));
    }

    // ---------- CAR ----------

    @Test
    void car_compact_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.COMPACT).isCompatible(VehicleType.CAR, start, end));
    }

    @Test
    void car_regular_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.REGULAR).isCompatible(VehicleType.CAR, start, end));
    }

    @Test
    void car_large_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.LARGE).isCompatible(VehicleType.CAR, start, end));
    }

    @Test
    void car_handicapped_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.CAR, start, end));
    }

    // ---------- BUS ----------

    @Test
    void bus_compact_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.COMPACT).isCompatible(VehicleType.BUS, start, end));
    }

    @Test
    void bus_regular_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.REGULAR).isCompatible(VehicleType.BUS, start, end));
    }

    @Test
    void bus_large_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.LARGE).isCompatible(VehicleType.BUS, start, end));
    }

    @Test
    void bus_handicapped_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.BUS, start, end));
    }

    // ---------- BICYCLE ----------

    @Test
    void bicycle_compact_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.COMPACT).isCompatible(VehicleType.BICYCLE, start, end));
    }

    @Test
    void bicycle_regular_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.REGULAR).isCompatible(VehicleType.BICYCLE, start, end));
    }

    @Test
    void bicycle_large_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.LARGE).isCompatible(VehicleType.BICYCLE, start, end));
    }

    @Test
    void bicycle_handicapped_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.BICYCLE, start, end));
    }

    // ---------- MICROCAR (this is the suspicious one — no `break` in source!) ----------

    @Test
    void microcar_compact_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.COMPACT).isCompatible(VehicleType.MICROCAR, start, end));
    }

    @Test
    void microcar_regular_true() {
        assertTrue(new ParkingSlot("S1", ParkingSlotType.REGULAR).isCompatible(VehicleType.MICROCAR, start, end));
    }

    @Test
    void microcar_large_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.LARGE).isCompatible(VehicleType.MICROCAR, start, end));
    }

    @Test
    void microcar_handicapped_false() {
        assertFalse(new ParkingSlot("S1", ParkingSlotType.HANDICAPPED).isCompatible(VehicleType.MICROCAR, start, end));
    }

    // ---------- TRUCK — not in the documentation's compatibility table at all! ----------

    @Test
    void truck_large_behavior() {
        // Table doesn't mention TRUCK for any slot type — checking what the code actually does
        assertFalse(new ParkingSlot("S1", ParkingSlotType.LARGE).isCompatible(VehicleType.TRUCK, start, end));
    }
    // ---------- isAvailable: overlap boundary tests ----------

    private Booking makeBooking(ParkingSlot slot, LocalDateTime s, LocalDateTime e) {
        Vehicle v = new Vehicle(1, VehicleType.CAR, 1000.0);
        return new Booking(1, v, slot, s, e, 50.0);
    }

    @Test
    void isAvailable_noExistingBookings_true() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        assertTrue(slot.isAvailable(start, end));
    }

    @Test
    void isAvailable_newRequestEndsExactlyWhenExistingStarts_true() {
        // existing: 10:00-12:00 ; request: 08:00-10:00 (touch, no overlap)
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getBookings().add(makeBooking(slot, start, end)); // 10:00-12:00

        LocalDateTime reqStart = start.minusHours(2); // 08:00
        LocalDateTime reqEnd = start;                 // 10:00
        assertTrue(slot.isAvailable(reqStart, reqEnd));
    }

    @Test
    void isAvailable_newRequestStartsExactlyWhenExistingEnds_true() {
        // existing: 10:00-12:00 ; request: 12:00-14:00 (touch, no overlap)
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getBookings().add(makeBooking(slot, start, end)); // 10:00-12:00

        LocalDateTime reqStart = end;                 // 12:00
        LocalDateTime reqEnd = end.plusHours(2);       // 14:00
        assertTrue(slot.isAvailable(reqStart, reqEnd));
    }

    @Test
    void isAvailable_newRequestFullyInsideExisting_false() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getBookings().add(makeBooking(slot, start, end)); // 10:00-12:00

        assertFalse(slot.isAvailable(start.plusMinutes(15), end.minusMinutes(15))); // 10:15-11:45
    }

    @Test
    void isAvailable_newRequestOverlapsStartOfExisting_false() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getBookings().add(makeBooking(slot, start, end)); // 10:00-12:00

        assertFalse(slot.isAvailable(start.minusMinutes(30), start.plusMinutes(30))); // 09:30-10:30
    }

    @Test
    void isAvailable_newRequestOverlapsEndOfExisting_false() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getBookings().add(makeBooking(slot, start, end)); // 10:00-12:00

        assertFalse(slot.isAvailable(end.minusMinutes(30), end.plusMinutes(30))); // 11:30-12:30
    }

    @Test
    void isAvailable_newRequestFullyCoversExisting_false() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getBookings().add(makeBooking(slot, start, end)); // 10:00-12:00

        assertFalse(slot.isAvailable(start.minusHours(1), end.plusHours(1))); // 09:00-13:00
    }
    @Test
    void getBookings_returnsLiveMutableInternalList_defectSuspected() {
        // D11: getBookings() exposes the slot's internal list by reference,
        // with no defensive copy. External code can mutate the slot's booking
        // history directly, bypassing all booking-creation validation.
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        Booking fake = new Booking(999, new Vehicle(1, VehicleType.CAR, 100.0), slot, start, end, 20.0);

        slot.getBookings().add(fake); // added without ever calling ParkingSystem.book()

        assertEquals(1, slot.getBookings().size());
        assertTrue(slot.getBookings().contains(fake));
    }
    @Test
    void motorcycle_regularSlot_unavailableDueToOverlap_notCompatible() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.REGULAR);
        slot.getBookings().add(makeBooking(slot, start, end)); // occupies 10:00-12:00
        assertFalse(slot.isCompatible(VehicleType.MOTORCYCLE, start.plusMinutes(30), end.plusMinutes(30)));
    }

    @Test
    void bus_largeSlot_unavailableDueToOverlap_notCompatible() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.LARGE);
        slot.getBookings().add(makeBooking(slot, start, end));
        assertFalse(slot.isCompatible(VehicleType.BUS, start.plusMinutes(30), end.plusMinutes(30)));
    }

    @Test
    void bicycle_handicappedSlot_unavailableDueToOverlap_notCompatible() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.HANDICAPPED);
        slot.getBookings().add(makeBooking(slot, start, end));
        assertFalse(slot.isCompatible(VehicleType.BICYCLE, start.plusMinutes(30), end.plusMinutes(30)));
    }

    @Test
    void microcar_compactSlot_unavailableDueToOverlap_notCompatible() {
        ParkingSlot slot = new ParkingSlot("S1", ParkingSlotType.COMPACT);
        slot.getBookings().add(makeBooking(slot, start, end));
        assertFalse(slot.isCompatible(VehicleType.MICROCAR, start.plusMinutes(30), end.plusMinutes(30)));
    }
}