# Unit Testing — Parking Slot Booking

A Java unit-testing project focused on validating the behavior of a parking slot booking system, identifying intentional defects, and evaluating test effectiveness through mutation testing.

## Project Overview

This project was completed as part of the **Software Testing and Quality Assurance** coursework at United International University (UIU). The assignment focuses on designing a reusable JUnit test suite for an existing Java application without modifying its source code.

The tests examine expected functionality, boundary conditions, invalid inputs, booking conflicts, wallet transactions, and state transitions. They also document defects in the original implementation and use PIT mutation testing to assess the strength of the test suite.

## Objectives

- Design comprehensive unit tests for each provided Java class.
- Verify normal behavior, boundary conditions, and invalid inputs.
- Identify and document defects in the existing implementation.
- Detect weak test cases using mutation testing.
- Ensure tests are repeatable and independent of execution order.
- Document test results, defect analysis, and individual contributions.

## Technologies Used

- **Java** — application and test language
- **JUnit 5** — unit-testing framework
- **Maven / Gradle** — project build and test execution, depending on the configured setup
- **PIT Mutation Testing** — evaluation of test effectiveness

## Classes Tested

| Class | Testing Focus |
|---|---|
| `Vehicle` | Construction, vehicle properties, wallet association, balance, and string representation |
| `Wallet` | Balance initialization, adding and deducting funds, transfers, and invalid amounts |
| `ParkingSlot` | Slot activation, vehicle compatibility, booking availability, overlapping time windows, and booking-list access |
| `Booking` | Booking initialization, stored properties, and completion/cancellation state transitions |
| `ParkingSystem` | Booking creation, fee calculation, wallet transactions, available-slot searches, singleton behavior, and system state |

## Testing Approach

The test suite covers several categories:

- **Positive testing:** Verifies valid operations such as creating bookings, adding funds, and completing bookings.
- **Negative testing:** Checks invalid operations, incompatible vehicles, insufficient funds, and invalid booking times.
- **Boundary-value testing:** Examines zero amounts, exact balance deductions, booking time boundaries, and partial-hour durations.
- **State-transition testing:** Checks booking status changes and repeated completion or cancellation attempts.
- **Integration-oriented unit tests:** Verifies interactions among vehicles, parking slots, bookings, and wallets through the public system API.
- **Defect-focused testing:** Documents unexpected behavior in the original source code.
- **Mutation testing:** Identifies test cases that fail to detect artificial changes to the program.

The tests use the provided reset mechanism where necessary to reduce interference from shared singleton or static state.

## Test Results

The completed test report records the following results against the original, unmodified implementation:

| Metric | Result |
|---|---:|
| Total unit tests | 109 |
| Tests passing | 109 |
| Documented defects | 11 |
| PIT mutation score | 98% |
| Mutants killed | 85 of 87 |

**Important:** Passing tests do not mean the application is defect-free. Some tests deliberately verify the current behavior associated with suspected defects, allowing the suite to document existing problems without changing the source code.

## Defect Analysis

The test suite identified issues involving:

- Invalid booking state transitions and repeated financial operations.
- Incorrect billing for bookings shorter than or not evenly divisible by one hour.
- Inconsistent system and parking-slot booking lists after failed operations.
- Shared wallet references between vehicles.
- Acceptance of negative initial wallet balances.
- Vehicle compatibility rules that prevent trucks from being booked.
- Booking operations that accept unregistered vehicles and parking slots.
- Direct access to mutable internal collections and wallet objects.

The complete defect report includes defect IDs, affected methods, observed behavior, and suggested fixes.

## Mutation Testing

PIT was used to evaluate whether the tests detect small, artificial changes to the program.

The completed report records a **98% mutation score (85 of 87 mutants killed)** for the reported `Wallet`, `ParkingSlot`, and `ParkingSystem` analysis.

The surviving mutants were reviewed to understand whether additional tests could detect them or whether the changed behavior was observationally equivalent. One surviving mutant was unreachable because the truck compatibility defect prevented execution from reaching the truck pricing branch.

To run mutation testing in a compatible Maven project with PIT configured:

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

The generated report is normally available under:

```text
target/pit-reports/
```

Actual output depends on the build configuration and the classes included in the PIT run.

## Repository Structure

The repository should reflect the files actually committed. The assignment submission uses the following structure:

```text
.
├── test/
│   ├── VehicleTest.java
│   ├── WalletTest.java
│   ├── ParkingSlotTest.java
│   ├── BookingTest.java
│   └── ParkingSystemTest.java
├── pit-reports/
│   └── ... generated PIT report files
├── unit_test_report.md
└── README.md
```

The original application source is tested without modification. Build configuration files may be included separately if needed to make the project reproducible.

## Team Members

| Student ID | Name |
|---|---|
| 011222159 | Md Noman Hossain |
| 011192153 | Md. Fahad Bin Elias |

### Contributions

**Md Noman Hossain**
- Developed tests for `Booking`, `ParkingSystem`, and `Vehicle`.
- Investigated booking, transaction, and state-transition defects.
- Contributed to mutation testing and test-coverage improvements.

**Md. Fahad Bin Elias**
- Developed tests for `Wallet` and `ParkingSlot`.
- Investigated wallet validation and slot compatibility defects.
- Added tests to address mutation-testing gaps.

## Learning Outcomes

This project demonstrates how systematic unit testing can uncover defects that ordinary happy-path testing may miss. It also illustrates the difference between test execution success and application correctness, and how mutation testing helps evaluate whether assertions meaningfully detect behavioral changes.

## Academic Context

**Course:** CSE 4495 — Software Testing and Quality Assurance  
**Institution:** United International University (UIU)

