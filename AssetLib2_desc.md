# AssetLib2 — Solution Description

## Purpose

AssetLib2 is a console application for managing the lending of physical
assets (computers, calculators, books) to students, such as in a school or
university equipment-checkout desk. A librarian/staff user drives a
menu-based terminal UI to register students and assets, check assets in and
out, and look up who has what.

## Application Structure

### `AssetType` (enum)
Classifies an asset as one of: `COMPUTER`, `CALCULATOR`, `BOOK`.

### `Student`
Represents a borrower: `id`, `name`, `year`, `major`, with getters/setters.

### `Asset`
Represents a single lendable item and owns the borrowing state machine:
`id`, `name`, `type` (`AssetType`), `available`, `lastCheckOutDate`,
`plannedReturnDate`, `borrowedBy` (student id).
- New assets start `available = true` with no checkout info.
- `borrow(Student, LocalDate plannedReturnDate)` — fails if already
  checked out; otherwise marks it unavailable, records the borrower and
  `lastCheckOutDate` (today), and stores the planned return date.
- `returnAsset()` — fails if nothing is borrowed; otherwise clears the
  borrower/return date and marks the asset available again.

### `Library`
The in-memory repository and business-logic layer behind the menu. Holds
all students and assets and auto-generates unique IDs (`max existing + 1`)
when adding either one. Exposes:
- **Students:** `addStudent(name, year, major)`, `removeStudent(id)`
  (blocked while the student has a borrowed asset outstanding),
  `searchStudents(name, year, major)` (each filter optional, case-insensitive
  substring match on name/major), `studentHasBorrowedAssets(id)`,
  `findStudentById(id)`.
- **Assets:** `addAsset(name, type)`, `removeAsset(id)` (blocked while
  borrowed), `searchAssetsByName(name)`, `listAvailableAssets(type)` (type
  optional), `listBorrowedAssets()`, `searchBorrowedAssetsByStudentName(name)`,
  `borrowAsset(assetId, studentId, plannedReturnDate)`, `returnAsset(assetId)`,
  `findAssetById(id)`.

### `Menu`
The console UI. Loops over a Main Menu with two sub-menus:
- **Student Management Menu:** Add Student, Remove Student, Search Student.
- **Asset Management Menu:** Add Asset, List Available Assets, List
  Borrowed Assets, Search Borrowed Asset by Student, Borrow Asset, Return
  Asset, Remove Asset.

Results are printed as fixed-width tables. Students and assets can be
referred to by numeric ID or by name almost everywhere a lookup is needed
(e.g. "Enter student name or ID", "Enter asset ID or name"); when a name
matches more than one record, the menu prints the matches and asks for the
exact ID to disambiguate. Invalid input (bad number, unknown asset type,
unparseable date) is rejected with a message rather than crashing the flow.

### `Main`
Entry point — constructs a `Library` and a `Menu` and starts the menu loop.
State lives only in memory for the process's lifetime; nothing is persisted
to disk or a database.

## Use Cases

1. **Enroll a student.** Staff adds a new student (name, year, major); the
   system assigns the next available ID.
2. **Register a new asset.** Staff adds an asset by name and type
   (computer/calculator/book); the system assigns an ID and marks it
   available.
3. **Check out an asset.** Staff looks up an available asset (by ID or
   name) and a student (by ID or name), sets a planned return date, and
   the system marks the asset unavailable and records who has it.
4. **Check in a returned asset.** Staff enters the asset's ID; the system
   clears the borrower and return date and marks it available again.
5. **See what's available to lend.** Staff lists all available assets,
   optionally filtered to one type (e.g. only calculators).
6. **See what's currently out.** Staff lists all borrowed assets together
   with who has each one and when it's due back.
7. **Find a student's borrowed items.** Staff searches by a student's name
   to see every asset currently checked out to them.
8. **Look up a student's record.** Staff searches by name, year, and/or
   major to find matching students and review their details.
9. **Decommission an asset.** Staff removes an asset by ID or name; this is
   refused while the asset is still checked out.
10. **Offboard a student.** Staff removes a student by ID; this is refused
    while the student still has an outstanding loan, preventing orphaned
    borrow records.

## Design Notes

- **In-memory only.** No persistence layer or database — all state is lost
  when the process exits. Suitable as a teaching example or a starting
  point for a persisted version, not for production use as-is.
- **State duplication in `Asset`.** `available` and `borrowedBy` both
  encode "is this asset currently out," kept in sync by `borrow`/
  `returnAsset`. Both fields still have public setters, so external code
  could desynchronize them if it bypasses those methods.
- **Linear lookups.** All `find`/`search` operations scan the full list —
  fine at this scale, but would benefit from a `Map<Integer, T>` if the
  dataset grows significantly.
- **Mutable IDs.** `Student`/`Asset` expose `setId`, which could break
  lookups if changed after the entity is added to the `Library`; nothing
  currently prevents this.

## Suggested Next Steps

1. Derive `isAvailable()` from `borrowedBy == null` instead of storing a
   separate flag, or remove the public setters for `available`/`borrowedBy`
   to prevent inconsistent state.
2. Add overdue tracking (comparing `plannedReturnDate` to the current date)
   if due-date enforcement becomes in scope.
3. Add a persistence layer (file or database) so data survives restarts.
4. Add automated tests around `Library`'s business rules (duplicate IDs,
   blocked removals, search filtering).
