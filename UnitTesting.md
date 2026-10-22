# Unit Testing: Parking Slot Booking
## 0) Team members

| Student ID | Name |
|---|---|
| 011222159 | Md Noman Hossain |
| 011192153 | Md. Fahad Bin Elias |

---

## A) Test Case List

Verdicts are the result against the current, unmodified source. Tests whose name ends in `_defectSuspected` assert the *current* behavior, so a "Pass" there means the defect is really present in the code. Test IDs run from 1 to 109 (WalletTest 1-14, BookingTest 15-19, ParkingSlotTest 20-61, ParkingSystemTest 62-100, VehicleTest 101-109).

### WalletTest.java

| Test ID | Class.Method | Why this test? | Verdict | Comments |
|---|---|---|---|---|
| 1 | `Wallet.Wallet` | New wallet with no-arg constructor should start at zero balance | Pass | Confirms default constructor initializes balance to 0.0 |
| 2 | `Wallet.Wallet` | Wallet constructed with an initial balance should hold that balance | Pass | Straightforward field assignment verified |
| 3 | `Wallet.addFunds` | Adding zero should be rejected (only positive amounts are valid) | Pass | Throws `InvalidAmountException` as documented |
| 4 | `Wallet.deductFunds` | Deducting exactly the full balance should succeed and leave zero | Pass | Boundary case at `balance == amount` |
| 5 | `Wallet.transferFunds` | Transferring the full balance should move everything to the target wallet | Pass | Verifies both source and destination balances after transfer |
| 6 | `Wallet.addFunds` | Negative amount should be rejected | Pass | Throws `InvalidAmountException` |
| 7 | `Wallet.deductFunds` | Negative amount should be rejected | Pass | Throws `InvalidAmountException` |
| 8 | `Wallet.addFunds` | Positive amount should correctly increase balance | Pass | Basic happy-path check, not covered by earlier boundary tests |
| 9 | `Wallet.deductFunds` | Deducting more than the balance should be rejected | Pass | Throws `InsufficientFundsException` |
| 10 | `Wallet.transferFunds` | Transferring more than the balance should be rejected, and must not partially move funds | Pass | Verified both wallets are untouched after the failed transfer, no partial-transfer side effect |
| 11 | `Wallet.transferFunds` | Zero-amount transfer should be rejected | Pass | Throws `InvalidAmountException` before any balance check |
| 12 | `Wallet.transferFunds` | Negative-amount transfer should be rejected | Pass | Throws `InvalidAmountException` |
| 13 | `Wallet.Wallet` | Does the constructor reject a negative starting balance the way `addFunds`/`deductFunds` reject bad amounts? | Defect D8 | **D8**: `Wallet(double)` does no validation, so a wallet (and a Vehicle) can start with a negative balance. |
| 14 | `Wallet.deductFunds` | Deducting zero. Does the same rule as `addFunds` apply? Also added to kill a PIT boundary mutant on `amount > 0`. | Pass | Throws `InvalidAmountException`. The check is strictly `> 0`, not `>= 0`. |

### BookingTest.java

| Test ID | Class.Method | Why this test? | Verdict | Comments |
|---|---|---|---|---|
| 15 | `Booking.Booking` | A newly created booking should default to `ACTIVE` status | Pass | Constructor correctly sets initial status |
| 16 | `Booking.Booking` | All constructor arguments should be stored and retrievable via getters | Pass | Checked bookingId, vehicle, slot, start/end time, amount |
| 17 | `Booking.completeBooking` | Calling `completeBooking()` should transition status to `COMPLETED` | Pass | Simple state transition, works as expected |
| 18 | `Booking.cancelBooking` | Calling `cancelBooking()` should transition status to `CANCELLED` | Pass | Simple state transition, works as expected |
| 19 | `Booking.completeBooking` / `Booking.cancelBooking` | The docs don't say whether a COMPLETED booking can be cancelled later. Does the code stop it? | Defect D1 | **D1**: neither method checks the current status, so COMPLETED gets silently overwritten with CANCELLED. At `ParkingSystem` level this triggers real money transfers (see 71, 72). |

### ParkingSlotTest.java

| Test ID | Class.Method | Why this test? | Verdict | Comments |
|---|---|---|---|---|
| 20 | `ParkingSlot.ParkingSlot` | A newly created slot should be active by default | Pass |  |
| 21 | `ParkingSlot.deactivate` | Deactivating a slot should make `isActive()` return false | Pass |  |
| 22 | `ParkingSlot.activate` | Re-activating a previously deactivated slot should restore active state | Pass |  |
| 23 | `ParkingSlot.isCompatible` | An inactive slot must never be compatible, even if the vehicle/slot type would otherwise match | Pass | Confirms `isActive` check short-circuits before the type switch |
| 24 | `ParkingSlot.isCompatible` | MOTORCYCLE should be allowed in COMPACT slots per the documentation matrix | Pass |  |
| 25 | `ParkingSlot.isCompatible` | MOTORCYCLE should be allowed in REGULAR slots | Pass |  |
| 26 | `ParkingSlot.isCompatible` | MOTORCYCLE should be allowed in LARGE slots | Pass |  |
| 27 | `ParkingSlot.isCompatible` | MOTORCYCLE should NOT be allowed in HANDICAPPED slots | Pass |  |
| 28 | `ParkingSlot.isCompatible` | CAR should NOT be allowed in COMPACT slots | Pass |  |
| 29 | `ParkingSlot.isCompatible` | CAR should be allowed in REGULAR slots | Pass |  |
| 30 | `ParkingSlot.isCompatible` | CAR should be allowed in LARGE slots | Pass |  |
| 31 | `ParkingSlot.isCompatible` | CAR should NOT be allowed in HANDICAPPED slots | Pass |  |
| 32 | `ParkingSlot.isCompatible` | BUS should NOT be allowed in COMPACT slots | Pass |  |
| 33 | `ParkingSlot.isCompatible` | BUS should NOT be allowed in REGULAR slots | Pass |  |
| 34 | `ParkingSlot.isCompatible` | BUS should be allowed only in LARGE slots | Pass |  |
| 35 | `ParkingSlot.isCompatible` | BUS should NOT be allowed in HANDICAPPED slots | Pass |  |
| 36 | `ParkingSlot.isCompatible` | BICYCLE should be allowed in COMPACT slots | Pass |  |
| 37 | `ParkingSlot.isCompatible` | BICYCLE should be allowed in REGULAR slots | Pass |  |
| 38 | `ParkingSlot.isCompatible` | BICYCLE should be allowed in LARGE slots | Pass |  |
| 39 | `ParkingSlot.isCompatible` | BICYCLE should be allowed in HANDICAPPED slots (the only vehicle type permitted there) | Pass |  |
| 40 | `ParkingSlot.isCompatible` | MICROCAR should be allowed in COMPACT slots | Pass | Flagged for scrutiny because the source's `MICROCAR` case has no `break` statement |
| 41 | `ParkingSlot.isCompatible` | MICROCAR should be allowed in REGULAR slots | Pass |  |
| 42 | `ParkingSlot.isCompatible` | MICROCAR should NOT be allowed in LARGE slots | Pass | Verifies the missing `break` after the `MICROCAR` case does not cause an unintended fall-through side effect |
| 43 | `ParkingSlot.isCompatible` | MICROCAR should NOT be allowed in HANDICAPPED slots | Pass |  |
| 44 | `ParkingSlot.isCompatible` | The docs' compatibility table never mentions TRUCK. What does the code do with it? | Pass |  |
| 45 | `ParkingSlot.isAvailable` | A slot with no bookings at all should be available for any window | Pass |  |
| 46 | `ParkingSlot.isAvailable` | A new request ending exactly when an existing booking starts should be available (touching, not overlapping) | Pass | Confirms boundary is exclusive on this side |
| 47 | `ParkingSlot.isAvailable` | A new request starting exactly when an existing booking ends should be available (touching, not overlapping) | Pass | Confirms boundary is exclusive on this side too |
| 48 | `ParkingSlot.isAvailable` | A new request fully inside an existing booking's window should NOT be available | Pass |  |
| 49 | `ParkingSlot.isAvailable` | A new request overlapping only the start of an existing booking should NOT be available | Pass |  |
| 50 | `ParkingSlot.isAvailable` | A new request overlapping only the end of an existing booking should NOT be available | Pass |  |
| 51 | `ParkingSlot.isAvailable` | A new request fully covering (surrounding) an existing booking should NOT be available | Pass |  |
| 52 | `ParkingSlot.ParkingSlot` | slotId passed to the constructor should be retrievable via getter | Pass |  |
| 53 | `ParkingSlot.ParkingSlot` | slotType passed to the constructor should be retrievable via getter | Pass |  |
| 54 | `ParkingSlot.ParkingSlot` | A newly created slot's wallet should start at zero balance | Pass |  |
| 55 | `ParkingSlot.ParkingSlot` | `getBalance()` should reflect live changes made directly to the slot's wallet | Pass | Confirms `getBalance()` delegates to the live `Wallet` object rather than a cached value |
| 56 | `ParkingSlot.isCompatible` | Direct check: is TRUCK compatible with any slot type? | Defect D9 | **D9**: the switch in `isCompatible()` has no `case TRUCK`, so every slot type ends up at `default: return false`. The docs give TRUCK a pricing rate, so it should be bookable, but it never is. |
| 57 | `ParkingSlot.getBookings` | Does `getBookings()` return the real internal list or a copy? | Defect D11 | **D11**: the list is returned directly. Outside code can call `slot.getBookings().add(...)` and skip the checks in `ParkingSystem.book()`. |
| 58 | `ParkingSlot.isCompatible` | MOTORCYCLE is type-compatible with a REGULAR slot, but the slot is already booked for an overlapping window, does availability correctly block it? | Pass | Written to close a PITest mutation gap: the type-check branches previously never exercised the `isAvailable()` call against a genuinely busy slot for this vehicle type |
| 59 | `ParkingSlot.isCompatible` | BUS is type-compatible with a LARGE slot, but busy for an overlapping window | Pass | Same purpose as 58, for the BUS/LARGE branch |
| 60 | `ParkingSlot.isCompatible` | BICYCLE is type-compatible with a HANDICAPPED slot, but busy for an overlapping window | Pass | Same purpose as 58, for the BICYCLE/HANDICAPPED branch |
| 61 | `ParkingSlot.isCompatible` | MICROCAR is type-compatible with a COMPACT slot, but busy for an overlapping window | Pass | Same purpose as 58, for the MICROCAR/COMPACT branch |

### ParkingSystemTest.java

| Test ID | Class.Method | Why this test? | Verdict | Comments |
|---|---|---|---|---|
| 62 | `ParkingSystem.book` | Happy path: a standard 2-hour CAR/REGULAR booking should compute the correct fee and correctly move funds from vehicle to system | Pass | Baseline sanity check: 2h × 10.0 × 1.0 × 1.0 = 20.0 |
| 63 | `ParkingSystem.book` | Booking with end time before start time should be rejected | Pass | Throws `IllegalBookingTimeException` |
| 64 | `ParkingSystem.book` | Booking with end time equal to start time should be rejected | Pass | Throws `IllegalBookingTimeException`; confirms boundary uses strict inequality |
| 65 | `ParkingSystem.book` | Booking an incompatible vehicle/slot combination should be rejected | Pass | Throws `IllegalArgumentException` |
| 66 | `ParkingSystem.book` | Booking shorter than 1 hour (30 min). How does the fee calculation handle it? | Defect D2 | **D2**: `Duration.toHours()` truncates to 0, so the amount is 0 and `transferFunds(0)` throws `InvalidAmountException`. A valid short booking cannot be made. |
| 67 | `ParkingSystem.book` | After 66 fails, is anything left behind? | Defect D4 | **D4**: the booking is added to the system's list before the transfer. After the failure it is in `system.getBookings()` but not in `slot.getBookings()`. |
| 68 | `ParkingSystem.book` | 1.5 hour booking. Is the extra half hour charged or dropped? | Defect D3 | **D3**: `toHours()` truncates, so only 1 hour is billed (10.0 instead of 20.0 if rounded up). |
| 69 | `ParkingSystem.completeBooking` | Completing a booking should move 80% of the fare from system to slot, keeping 20% | Pass | 20.0 fare → slot gets 16.0, system retains 4.0 |
| 70 | `ParkingSystem.cancelBooking` | Cancelling a booking should refund 90% to the vehicle, system keeping 10% | Pass | 20.0 fare → vehicle refunded 18.0, system retains 2.0 |
| 71 | `ParkingSystem.completeBooking` | `completeBooking()` called twice on the same booking. Is there a guard against paying out twice? | Defect D5 | **D5**: no status check. The second call only fails with `InsufficientFundsException` because the system wallet holds one booking's money. With more money in there, the slot would be paid twice. |
| 72 | `ParkingSystem.cancelBooking` | Cancelling a booking that was already completed. Does the money add up? | Defect D6 | **D6**: `completeBooking()` already paid 80% to the slot, so the 90% refund fails with `InsufficientFundsException`. Completing and cancelling are not mutually exclusive in the code. |
| 73 | `ParkingSystem.getVehicleTypeRate` | MOTORCYCLE's 0.5x rate multiplier, verified via `book()` since the method is private | Pass | 2h × 10.0 × 0.5 × 1.0(REGULAR) = 10.0 |
| 74 | `ParkingSystem.getVehicleTypeRate` | BUS's 2.0x rate multiplier, combined with LARGE slot's 1.5x | Pass | 2h × 10.0 × 2.0 × 1.5 = 60.0 |
| 75 | `ParkingSystem.getVehicleTypeRate` | BICYCLE's 0.2x rate (lowest), combined with COMPACT slot's 0.8x | Pass | 2h × 10.0 × 0.2 × 0.8 = 3.2 |
| 76 | `ParkingSystem.getVehicleTypeRate` | MICROCAR's 1.5x rate, combined with COMPACT slot's 0.8x | Pass | 2h × 10.0 × 1.5 × 0.8 = 24.0 |
| 77 | `ParkingSystem.parkingSlotTypeMultiplier` | HANDICAPPED slot's 1.2x multiplier, only reachable via BICYCLE per the compatibility matrix | Pass | 2h × 10.0 × 0.2 × 1.2 = 4.8; only path that can exercise this multiplier at all |
| 78 | `ParkingSystem.book` | TRUCK vs COMPACT, following up on 44/56, confirmed end-to-end via `book()` | Defect D9 | Throws `IllegalArgumentException`; part of proving TRUCK is unbookable in every slot type |
| 79 | `ParkingSystem.book` | TRUCK vs REGULAR | Defect D9 | Throws `IllegalArgumentException` |
| 80 | `ParkingSystem.book` | TRUCK vs LARGE, the "biggest vehicle" slot type, where TRUCK would most plausibly be expected to fit | Defect D9 | Throws `IllegalArgumentException`; strongest evidence TRUCK is entirely unbookable despite having a documented pricing rate |
| 81 | `ParkingSystem.book` | TRUCK vs HANDICAPPED | Defect D9 | Throws `IllegalArgumentException` |
| 82 | `ParkingSystem.getAvailableParkingSlots` | Should return only slots that are both type-compatible and free for the requested window | Pass | Confirms COMPACT is excluded for a CAR request while REGULAR/LARGE are included |
| 83 | `ParkingSystem.getAvailableParkingSlots` | A slot already booked for an overlapping window should be excluded from results | Pass |  |
| 84 | `ParkingSystem.getAvailableParkingSlots` | A deactivated slot should be excluded from results | Pass |  |
| 85 | `ParkingSystem.ParkingSystem` | `getInstance()` should always return the same singleton instance | Pass |  |
| 86 | `ParkingSystem.ParkingSystem` | `resetForTesting()` should clear all lists and restore default rate/wallet | Pass | Necessary for test isolation given the singleton's shared static state |
| 87 | `ParkingSystem.ParkingSystem` | Changing the hourly rate via setter should affect the price of subsequent bookings | Pass |  |
| 88 | `ParkingSystem.ParkingSystem` | Replacing the system wallet via setter should be reflected in `getBalance()` | Pass |  |
| 89 | `ParkingSystem.ParkingSystem` | Setter methods for vehicles/slots/bookings should fully replace the backing lists | Pass |  |
| 90 | `ParkingSystem.book` | Booking the same slot for an overlapping window with a second vehicle should be rejected | Pass | End-to-end confirmation of `isAvailable()`'s logic through the public `book()` API |
| 91 | `ParkingSystem.book` | Booking the same slot for a non-overlapping (touching) window should succeed | Pass | Confirms exclusive boundary behavior holds at the system level too |
| 92 | `ParkingSystem.book` | Does `book()` require the vehicle and slot to be registered with `addVehicle()`/`addParkingSlot()` first? | Defect D10 | **D10**: nothing checks this. Booking works for objects that were never registered with the system. |
| 93 | `ParkingSystem.getVehicleTypeRate` (indirect) | MOTORCYCLE's 0.5x rate, combined with COMPACT slot's 0.8x | Pass | 2h × 10.0 × 0.5 × 0.8 = 8.0 |
| 94 | `ParkingSystem.getVehicleTypeRate` (indirect) | MOTORCYCLE's 0.5x rate, combined with LARGE slot's 1.5x | Pass | 2h × 10.0 × 0.5 × 1.5 = 15.0 |
| 95 | `ParkingSystem.getVehicleTypeRate` (indirect) | MICROCAR's 1.5x rate, combined with REGULAR slot's 1.0x | Pass | 2h × 10.0 × 1.5 × 1.0 = 30.0 |
| 96 | `ParkingSystem.getVehicleTypeRate` (indirect) | BICYCLE's 0.2x rate, combined with REGULAR slot's 1.0x | Pass | 2h × 10.0 × 0.2 × 1.0 = 4.0 |
| 97 | `ParkingSystem.getVehicleTypeRate` (indirect) | BICYCLE's 0.2x rate, combined with LARGE slot's 1.5x | Pass | 2h × 10.0 × 0.2 × 1.5 = 6.0; completes full coverage of every documented VehicleType × ParkingSlotType combination reachable per the compatibility matrix, no pricing discrepancies found |
| 98 | `ParkingSystem.completeBooking` | Unit-level recheck of 71: does the second call fail, and was the slot paid only once? | Defect D5 | Second call throws `InsufficientFundsException` and `slot.getBalance()` stays 16.0 (not 32.0). A double payout is blocked only because the escrow is empty, not by design. |
| 99 | `ParkingSystem.book` | Booking two consecutive slots, are `bookingId` values assigned sequentially and correctly? | Pass | Written to close a PITest mutation gap on `bookings.size() + 1`; confirms IDs are 1, then 2 |
| 100 | `ParkingSystem.getSYSTEM_WALLET` | Does the getter return the live system wallet object (so external mutation is reflected), or a disconnected copy? | Pass | Returns the live reference, same pattern as `ParkingSlot.getWallet()`/`getBookings()` (D11). |

### VehicleTest.java

| Test ID | Class.Method | Why this test? | Verdict | Comments |
|---|---|---|---|---|
| 101 | `Vehicle.Vehicle` | Constructing with a pre-made `Wallet` should store and use that exact wallet | Pass | Stores the reference, not a copy. This leads to 106. |
| 102 | `Vehicle.Vehicle` | Constructing with a `double` balance should create a new internal wallet with that balance | Pass |  |
| 103 | `Vehicle.Vehicle` | vehicleId passed to constructor should be retrievable via getter | Pass |  |
| 104 | `Vehicle.Vehicle` | vehicleType passed to constructor should be retrievable via getter | Pass |  |
| 105 | `Vehicle.Vehicle` | `getBalance()` should reflect live changes made directly to the vehicle's wallet | Pass | Confirms delegation to the live `Wallet` object |
| 106 | `Vehicle.Vehicle` | Can two vehicles end up sharing one wallet object? | Defect D7 | **D7**: the `Wallet` constructor keeps the reference with no copy, so two vehicles built with the same wallet see each other's balance changes. |
| 107 | `Vehicle.Vehicle` | Negative `double` balance in the `Vehicle` constructor. Is it validated? | Defect D8 | Evidence for **D8** from the Vehicle side: nothing stops a negative starting balance. |
| 108 | `Vehicle.Vehicle` | Constructing with a zero `double` balance should be accepted (sanity check, not a defect) | Pass | Zero is a legitimate starting balance; included to contrast with 107 |
| 109 | `Vehicle.toString` | `toString()` output should contain the vehicle's id, type, and wallet balance | Pass | Basic representation check |
### Summary

- Total tests: 14 (Wallet) + 5 (Booking) + 42 (ParkingSlot) + 39 (ParkingSystem) + 9 (Vehicle) = **109**
- All 109 pass against the unmodified source.
- 17 tests are marked "defect suspected" and together back up all 11 defects (D1-D11).
- Final PIT mutation score: **98% (85/87 mutants killed)** on `Wallet`, `ParkingSlot` and `ParkingSystem`.

---

## B) Defects List

| Defect ID | Class.Method | Found by | Description | Suggested fix |
|---|---|---|---|---|
| D1 | `Booking.completeBooking` / `Booking.cancelBooking` | #19 `cancelBooking_afterComplete_overwritesStatusToCancelled` | Neither method looks at the current status before changing it. A `COMPLETED` booking can be set to `CANCELLED` (and the other way round) with no exception. | Throw `IllegalStateException` unless the status is `ACTIVE`. |
| D2 | `ParkingSystem.book` | #66 `book_durationUnderOneHour_defectSuspected` | `Duration.between(...).toHours()` drops partial hours. A 30-minute booking gets 0 billable hours, so the amount is 0 and `transferFunds(0)` throws `InvalidAmountException`. A valid short booking can't be made. | Round up (e.g. `Math.ceil(minutes / 60.0)`), or reject bookings under a minimum length with a clear exception. |
| D3 | `ParkingSystem.book` | #68 `book_oneAndHalfHours_chargesOnlyOneHour_defectSuspected` | Same truncation: a 1.5 hour booking is billed as 1 hour, so the customer is under-charged. | Same as D2. |
| D4 | `ParkingSystem.book` | #67 `book_underOneHour_leavesInconsistentState_defectSuspected` | The booking is added to the system's `bookings` list before the wallet transfer. If the transfer throws (e.g. the D2 case), the booking stays in the system list but never gets into `slot.getBookings()`, so the two are out of sync. | Do the transfer (or validate the amount) first, then add the booking to the lists. |
| D5 | `ParkingSystem.completeBooking` | #71, #98 `completeBooking_calledTwice_defectSuspected_insufficientFundsOnSecondCall` | No check that the booking is already `COMPLETED`, so a second call tries a second 80% payout. In our test it throws `InsufficientFundsException` only because the system wallet holds one booking's money. If other bookings had put more there, the slot would be paid twice without any error. | Check `getBookingStatus() == ACTIVE` at the start of `completeBooking()` and `cancelBooking()`, otherwise throw `IllegalStateException`. |
| D6 | `ParkingSystem.cancelBooking` | #72 `cancelBooking_afterComplete_defectSuspected_insufficientEscrow` | Cancelling an already completed booking tries to refund 90% from the system wallet after 80% already went to the slot, so it throws `InsufficientFundsException`. The two payouts aren't mutually exclusive in the code; if both went through, the system would pay out 170% of one fare. | Same status guard as D5. |
| D7 | `Vehicle(int, VehicleType, Wallet)` | #106 `twoVehiclesSharingSameWalletInstance_seeEachOthersBalanceChanges_defectSuspected` | The constructor stores the `Wallet` reference directly. Two vehicles built with the same `Wallet` share one balance, so an operation on one changes the other. | Copy the balance into a new `Wallet` in the constructor, or document that sharing is intended. |
| D8 | `Wallet(double balance)` | #13 `constructorWithNegativeBalance_acceptedWithoutValidation_defectSuspected` (WalletTest); #107 `constructorWithDoubleBalance_negativeInitialBalance_acceptedWithoutValidation_defectSuspected` (VehicleTest) | The constructor does no validation, unlike `addFunds()`/`deductFunds()`. A `Wallet`, and so a `Vehicle`, can start with a negative balance. | Throw `InvalidAmountException` for negative values (0 can stay allowed for an empty wallet). |
| D9 | `ParkingSlot.isCompatible` | #44, #56 `isCompatible_truck_everySlotType_alwaysFalse_defectSuspected` (ParkingSlotTest); #78-81 `book_truck_compactSlot_throwsIllegalArgumentException_defectSuspected` + 3 siblings (ParkingSystemTest) | The docs give TRUCK a rate (3.0) but the switch in `isCompatible()` has no `case TRUCK`, so every slot type returns false. A TRUCK can never be booked and its pricing code is unreachable. | Add a `case TRUCK:` branch, most likely for `LARGE` slots like BUS. |
| D10 | `ParkingSystem.book` | #92 `book_withUnregisteredVehicleAndSlot_stillSucceeds_defectSuspected` | `book()` never checks that the vehicle and slot were registered with `addVehicle()`/`addParkingSlot()`. Booking works for unregistered objects, so registration is not enforced. | Check both are in the system's own lists at the start of `book()` and throw `IllegalArgumentException` if not, or document that registration is optional. |
| D11 | `ParkingSlot.getBookings`; `ParkingSystem.getVehicles`/`getParkingSlots`/`getBookings`/`getSYSTEM_WALLET` | #57 `getBookings_returnsLiveMutableInternalList_defectSuspected` (ParkingSlotTest); #100 `getSYSTEM_WALLET_returnsLiveSystemWalletReference` (ParkingSystemTest) | These getters return the internal lists/objects directly. Outside code can change them (e.g. `slot.getBookings().add(...)`, or adding money through the returned system wallet) and bypass the checks in `book()`, `completeBooking()` and `cancelBooking()`. | Return `Collections.unmodifiableList(...)` for the lists, and think about exposing only the balance instead of the live `Wallet`. |

**Not listed as a defect: `MICROCAR` missing `break`.** In `ParkingSlot.isCompatible()` the `case MICROCAR` has no `break`, so when its condition is false it falls through to `default: return false`. Tests 42 and 43 show this is harmless, because `default` returns the same value as every other non-matching case. We looked into it but it doesn't cause wrong behavior.

---

## C) Mutant Analysis

Overall mutation score (PIT report, `Wallet`, `ParkingSlot`, `ParkingSystem`): **98% (85/87 mutants killed)**.

### Md Noman Hossain (011222159)

Overall mutation score: 98% (85/87).

**Killed mutant:** `ParkingSystem.java`, line 50, `bookings.size() + 1` changed to `bookings.size() - 1` (math mutant on the booking ID). `book_multipleBookings_assignsSequentialBookingIds` books two vehicle/slot pairs and asserts the IDs are exactly 1 and 2. With the mutant they would be -1 and 0, so the assertion fails. The test checks the actual ID value, not just that a booking came back.

**Surviving mutant:** `ParkingSystem.java`, line 88, in `getVehicleTypeRate`, `case TRUCK: return 3.0;` changed to `return 0.0;`. PIT reports it as `NO_COVERAGE`, because no test can reach the line. `book()` calls `slot.isCompatible(...)` before pricing, and `isCompatible()` has no `case TRUCK` (D9), so a TRUCK booking always throws `IllegalArgumentException` first. More tests won't help here. It only becomes killable once D9 is fixed.

### Md. Fahad Bin Elias (011192153)

**Killed mutant:** `ParkingSlot.java`, line 28, in the `MOTORCYCLE` case, `return isAvailable(startTime, endTime);` replaced by `return true;` (`TRUE_RETURNS`). It first survived because all our MOTORCYCLE tests used an empty slot, where `isAvailable()` is true anyway. `motorcycle_regularSlot_unavailableDueToOverlap_notCompatible` books a REGULAR slot 10:00-12:00 and then checks a 10:30-12:30 request, which must be `false`, so the mutant fails the `assertFalse`.

**Surviving mutant:** `Wallet.java`, line 39, in `transferFunds()`, `amount > 0` changed to `amount >= 0` (`CONDITIONALS_BOUNDARY`). With the mutant, `amount = 0` gets past the first check and reaches `deductFunds(amount)`, which has its own `amount > 0` check and throws the same `InvalidAmountException`. `transferFunds_zeroAmount_throwsInvalidAmountException` only checks the exception type, so it can't tell the two apart. The observable behavior is the same, so this is practically an equivalent mutant.

---

## D) Individual Contribution

| Student ID | Name | Contribution |
|---|---|---|
| 011222159 | Md Noman Hossain | `BookingTest`, `ParkingSystemTest`, `VehicleTest`; defects D1-D7, D10 and the `ParkingSystem` side of D9/D11; set up and ran PIT (90% to 98%); Sections A and B |
| 011192153 | Md. Fahad Bin Elias | `WalletTest`, `ParkingSlotTest`; defects D8 (Wallet), D9 (ParkingSlot), D11 (`getBookings`); checked the `MICROCAR` missing `break`; PIT gap-closing tests for `ParkingSlot`/`Wallet` |
