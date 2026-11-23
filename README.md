# ISE SeatPlan — 6th Floor

ISE SeatPlan is a local full-stack application for creating and managing exam seating arrangements for the Information Science and Engineering (ISE) department on the 6th floor. It includes a React staff interface and a Java Spring Boot API. Exam, room, imported student, and saved seat assignment data are stored in a database.

## What the application does

- Creates exam events with exam name, semester(s), section/course label, academic year, term, date, and notes.
- Imports eligible USNs from Excel `.xlsx` and `.xls` files. USNs are handled as text, trimmed, and normalized to uppercase so meaningful letters and leading zeroes are retained.
- Previews workbook sheets and rows, lets staff map the USN and semester information, and shows duplicate, blank, invalid, and unreadable rows for correction or exclusion.
- Manages rooms on the 6th floor, including room name, description/type, enabled status, layout mode, orientation, columns, benches, position counts, and blocked seats.
- Suggests editable, room-specific inclusive USN ranges based on the actual imported students. Ranges are hard constraints during allocation.
- Checks room capacity and range coverage before allocating students. It reports capacity shortages, uncovered students, and unused seats.
- Generates and reviews room-by-room seating, supports USN lookup and manual moves/swaps, and saves a chosen arrangement.
- Prints all rooms together or a single room through the browser’s print dialog, including **Save as PDF** where the browser supports it.
- Keeps saved assignments stable when an exam is reopened or printed; generation and saving are separate steps.

## Main workflow

1. **Create an exam.** Enter the exam details and select single-semester or mixed-semester seating.
2. **Import students.** Upload the workbook(s), confirm the sheet, USN column, and semester mapping, then correct or exclude rows flagged by the preview. For a mixed exam, the current flow uses two source/workbook slots and staff assigns a different semester to each.
3. **Choose rooms and ranges.** Select enabled rooms. Review the proposed USN ranges, accept or edit them, and make sure all students are covered without overlap.
4. **Check layout and capacity.** Review each room’s available positions, including blocked positions and mixed-exam capacity behavior.
5. **Generate and review.** Inspect the arrangement by room, search for a USN, and move or swap students if needed. Unsaved edits are indicated in the review.
6. **Save and print.** Save the chosen arrangement, then print all rooms or an individual room. Use the app’s Back control to return to the previous workflow screen.

From the dashboard, staff can open recent exams and delete an arrangement after confirming the action. Room management is available from the left navigation.

## Student import and semester mapping

The importer supports Excel workbooks with one or more worksheets. It reads cell content as text where possible and provides a preview with source sheet/row information. Confirm the semester mapping before saving the import; semester must not be guessed when the workbook does not identify it clearly. The mixed-semester workflow labels each uploaded source with the selected semester.

The preview is the place to resolve data issues. Review the listed USNs, duplicates, blank or malformed values, and rows that could not be read. Correct a row or explicitly exclude it before continuing. The imported list remains associated with the exam so it can be compared with the final allocation.

## Rooms and seating layouts

Rooms can be added, edited, enabled, or disabled in **Rooms**. Each room has a name/number, floor (default 6), description, Default or Special layout mode, orientation, and an editable physical layout. Layouts support uneven numbers of benches by column, different student-position counts per bench, and blocked positions. The application calculates capacity from the configured available positions; room numbers do not determine capacity.

The **Add sample rooms** action can create editable example rooms for an empty room database. Sample data is optional and may be edited or removed. For example, the sample set includes regular rooms and special layouts such as Room 612 and Lab 614; always review the actual layout and capacity before using a room.

For allocation, default rooms use two positions per bench for a single-semester exam and three effective positions per bench for a two-semester mixed exam. This exam-specific mixed behavior does not overwrite the room’s saved layout. Special rooms use their configured per-bench positions and blocked-seat settings. On special-room benches with two available positions, the allocator creates cross-semester pairs where both cohorts are eligible in that room; if one cohort is exhausted, same-semester fallback seating is allowed so valid students can still be seated. Three-position benches interleave cohorts according to their eligible counts and the selected pattern. Student order within each semester and configured room order are preserved.

Room USN ranges are hard limits: students are not moved to rooms outside their configured ranges to improve the visual pattern. If a hard range constraint prevents seating or full packing, the app reports the uncovered students or remaining seats rather than changing the ranges silently.

## Manual editing, regeneration, and saved plans

On the arrangement review screen, staff can select a student and choose an available destination, or select two assigned seats to swap their students. The application checks that destinations are available and that students remain within their configured room ranges. Changes remain unsaved until **Save arrangement** is used. Staff can discard edits to return to the last saved plan.

The regeneration action creates a new proposed allocation for review using the currently selected rooms and configured layouts/ranges. It is separate from manual editing: regeneration rearranges automatically, while manual editing changes selected students. Replacing an existing saved result requires confirmation; save the proposal explicitly to make it the chosen plan. Reopening and printing a saved plan use its stored assignments and do not reshuffle it.

## Print and export

Use **Print / export** for all rooms or **Print this room** on a room card. The app opens the browser print dialog; choose a printer or **Save as PDF**. The print view includes the ISE department and 6th-floor scope, exam details, room layouts, student totals, and validation information. Page orientation, paper size, and PDF destination are selected in the browser dialog.

## Requirements

- Java 17 or later
- Maven 3.8 or later
- Node.js 20 or later with npm
- MySQL 8 if using MySQL (optional for local development because the backend defaults to a file-based H2 database)

## Run locally

Start the backend in one terminal:

```powershell
cd backend
mvn spring-boot:run
```

The API is available at `http://localhost:8080/api`. By default, H2 stores data in `backend/data/seatplan` and keeps it between runs. The data directory is local and ignored by Git.

Start the frontend in a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the Vite URL shown in the terminal, typically `http://localhost:5173`. The frontend uses `http://localhost:8080/api` by default.

### Use MySQL instead of H2

Create a database in MySQL, for example:

```sql
CREATE DATABASE ise_seatplan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Set the connection values in the same terminal before starting the backend. PowerShell example:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/ise_seatplan'
$env:DB_USER = 'your_mysql_user'
$env:DB_PASSWORD = 'your_mysql_password'
mvn spring-boot:run
```

Bash example:

```sh
export DB_URL='jdbc:mysql://localhost:3306/ise_seatplan'
export DB_USER='your_mysql_user'
export DB_PASSWORD='your_mysql_password'
mvn spring-boot:run
```

The backend uses `spring.jpa.hibernate.ddl-auto=update` to create/update the schema at startup. Do not commit real database credentials. For a non-default API port, set `PORT` before starting Spring and set `VITE_API_URL` for the frontend to the matching address, such as `http://localhost:8081/api`.

### If port 8080 is already in use

Either stop the other application that is using port 8080, or start SeatPlan on another port and point the frontend at it. PowerShell example using port 8081:

```powershell
# Backend terminal
$env:PORT = '8081'
mvn spring-boot:run

# Frontend terminal
$env:VITE_API_URL = 'http://localhost:8081/api'
npm run dev
```

## Project structure

```text
backend/   Spring Boot REST API, allocation/import services, persistence, and Java tests
frontend/  React/Vite staff application and print styles
README.md  Setup and usage guide
```

The backend source is under `backend/src/main/java`; configuration is in `backend/src/main/resources/application.properties`. The frontend entry point and screens are under `frontend/src`.

## Build and tests

Run backend tests:

```powershell
cd backend
mvn test
```

Build the frontend production bundle:

```powershell
cd frontend
npm run build
```

The backend test suite exercises allocation and validation behavior, including duplicates, capacity, uneven/special layouts, blocked seats, room ranges, mixed-semester seating, and workbook import. The frontend production build checks that the React application bundles successfully.

## Troubleshooting

- **Blank page:** confirm the frontend development server is running and open the URL printed by Vite. Check the browser developer console if the page remains blank.
- **API unavailable:** confirm Spring Boot is running, the frontend’s `VITE_API_URL` points to the same port, and that the API responds at `http://localhost:8080/api` (or your configured port).
- **Port conflict:** use the alternate-port instructions above, or identify and stop the process already listening on the port.
- **Excel rows are flagged:** use the preview to confirm the correct USN column and semester/source mapping, then correct or exclude invalid rows explicitly.
- **Insufficient capacity or range conflicts:** review selected rooms, blocked positions, room ranges, and eligible-student count. The app will not silently assign a student outside a range or omit an eligible student.

## Current scope and limitations

SeatPlan is scoped to the ISE department on the 6th floor; room floor values are editable for configuration, but the interface and generated output identify the intended scope. Mixed-semester imports currently use two semester source slots. Automatic allocation handles the mixed bench patterns; the manual editor applies its own validity checks and may be more restrictive for creating a same-semester fallback pair on a two-position special bench than automatic allocation. Print/PDF output uses the browser’s print engine rather than a server-generated PDF file.
