package parking;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
    LocalDateTime end   = LocalDateTime.of(2025, 1, 1, 12, 0);

    private Vehicle makeVehicle() {
        return new Vehicle(1, VehicleType.CAR, 1000.0);
    }

    private ParkingSlot makeSlot() {
        return new ParkingSlot("S1", ParkingSlotType.REGULAR);
    }

    @Test
    void newBooking_hasActiveStatus() {
        Booking b = new Booking(1, makeVehicle(), makeSlot(), start, end, 20.0);
        assertEquals(BookingStatus.ACTIVE, b.getBookingStatus());
    }

    @Test
    void newBooking_storesAllFieldsCorrectly() {
        Vehicle v = makeVehicle();
        ParkingSlot s = makeSlot();
        Booking b = new Booking(7, v, s, start, end, 42.5);

        assertEquals(7, b.getBookingId());
        assertEquals(v, b.getVehicle());
        assertEquals(s, b.getParkingSlot());
        assertEquals(start, b.getStartTime());
        assertEquals(end, b.getEndTime());
        assertEquals(42.5, b.getAmount());
    }

    @Test
    void completeBooking_setsStatusToCompleted() {
        Booking b = new Booking(1, makeVehicle(), makeSlot(), start, end, 20.0);
        b.completeBooking();
        assertEquals(BookingStatus.COMPLETED, b.getBookingStatus());
    }

    @Test
    void cancelBooking_setsStatusToCancelled() {
        Booking b = new Booking(1, makeVehicle(), makeSlot(), start, end, 20.0);
        b.cancelBooking();
        assertEquals(BookingStatus.CANCELLED, b.getBookingStatus());
    }

    @Test
    void cancelBooking_afterComplete_overwritesStatusToCancelled() {
        // এটা একটা curious edge case — completed booking কি cancel করা যাওয়া উচিত?
        // documentation এই ব্যাপারে কিছু বলে না, তাই কোড আসলে কী করে সেটাই দেখছি এখানে
        Booking b = new Booking(1, makeVehicle(), makeSlot(), start, end, 20.0);
        b.completeBooking();
        b.cancelBooking();
        assertEquals(BookingStatus.CANCELLED, b.getBookingStatus());
    }
}