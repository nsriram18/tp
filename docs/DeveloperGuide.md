---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TutorTrack Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* TutorTrack is adapted from [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) by the [SE-EDU initiative](https://se-education.org/). The overall architecture (`UI`, `Logic`, `Model`, `Storage`), the command-parsing framework, the JSON storage layer, the GUI skeleton, the build setup, and the structure of the User and Developer Guides come from AB3. Student fields, lesson scheduling, filtering, sorting, and the student detail panel are TutorTrack's own work.
* The undo/redo design under _Proposed implementation_ is adapted from the [AB3 Developer Guide](https://se-education.org/addressbook-level3/DeveloperGuide.html#proposed-undoredo-feature). It is not implemented.
* The [PlantUML](https://plantuml.com/) diagrams use the AB3 diagram styles in `docs/diagrams/style.puml`.
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit 5](https://github.com/junit-team/junit5), and [Checkstyle](https://checkstyle.sourceforge.io/).
* Documentation is built with [MarkBind](https://markbind.org/).

### AI assistance

* Sriram ([@nsriram18](https://github.com/nsriram18)) used OpenAI Codex to assist with implementing bug fixes and
  code-quality improvements, generating regression tests, debugging and investigating CI failures, reviewing pull
  requests, and updating the User and Developer Guides. Codex also helped draft GitHub issue reports and pull request
  descriptions. Validation assistance included running automated tests and, where applicable, application smoke tests.
  
* Lee Hong Sheng ([@RightHandSide](https://github.com/RightHandSide)) used OpenAI Codex to assist with implementing
  bug fixes, refining input validation and user-facing messages, writing and running regression tests, reviewing pull
  requests, and investigating test and CI failures. Codex also helped draft commit messages and pull request
  descriptions.
  
* Toh Wei Jun ([@weijun72](https://github.com/weijun72)) used OpenAI Codex to assist with implementing and refining TutorTrack features and bug fixes, writing and running tests, updating documentation and diagrams, investigating build and CI failures, reviewing pull requests, and drafting commit messages and pull request descriptions. The suggested changes were reviewed and tested before submission.

* Panneer Selvam ([@spadewiz](https://github.com/spadewiz)) used GitHub Copilot to assist with writing code and tests, reviewing pull requests, and drafting GitHub issues and pull request descriptions. Panneer Selvam also used Claude to create issues and pull requests for bug fixes and to edit functional code. The suggested changes were reviewed before submission.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.html).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonDetailsPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

The following sequence diagram shows the same flow for the `filter` command. The command parser creates a
`StudentMatchesCriteriaPredicate` from the supplied school-level, subject, and tag criteria. The command then asks
the model to apply that predicate to its filtered student list.

<puml src="diagrams/FilterSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the filter Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a student).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/java/seedu/address/model/Model.java)


<puml src="diagrams/ModelClassDiagram.puml" width="600" alt="Structure of the Model component" />

The `Model` component,

* stores the student data, i.e., all `Person` objects (which are contained in a `UniquePersonList` object). A `Person` represents one student; the class name is inherited from AB3.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* treats `Person` as an immutable value: commands replace a student with an updated copy (for example `Person#withNote(...)`) rather than mutating it. `AddressBook` rejects duplicates by comparing normalised names.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)



### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-F14a-3/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

JSON saving serializes the complete data before writing a temporary file in the destination directory. The file is
then moved over the destination atomically where supported. If atomic moves are unsupported, saving falls back to
a regular replacement move; this fallback does not guarantee atomicity. Temporary files are removed after handled
write or move failures. This protects existing data from incomplete temporary writes, but is not a guarantee against
power loss or all filesystem failures. The same saving path is used for student records and user preferences.

Before saving changed student records, `LogicManager` asks Storage to compare the data file with the exact contents
seen by that app window. Storage holds a file lock during the comparison and replacement, so two app instances cannot
save between those steps. If the file has changed, the command is rejected and its in-memory change is rolled back;
the user must restart to load the newer records. Read-only commands do not save student data.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Student summary and detail view

The student list uses `PersonCard` to show a concise summary containing the student's name, phone number, and any available academic or lesson information. Default academic values and absent lessons are omitted so that tutors can scan more students without scrolling through complete records.

The `view INDEX` command parses an index relative to the current filtered list. `ViewCommand` rejects an index outside that list and otherwise places the selected immutable `Person` in the returned `CommandResult`. It does not modify the model or storage data.

`MainWindow` reads the optional selected student from `CommandResult` and creates a `PersonDetailsPanel` in the right-hand side of the split layout. The panel shows the student's complete academic, contact, scheduling, tag, and note information. A later successful non-view command restores the prompt, preventing an edited or deleted student's old record from remaining visible.

### Lesson priority and ordering

The following class diagram shows the model and logic classes involved in the `sort` and `upcoming` commands.
Only members relevant to lesson priority are shown.

<puml src="diagrams/LessonPriorityClassDiagram.puml" alt="Classes and relationships for lesson priority and ordering" width="760" />

* `SortCommand` passes `Person.COMPARATOR_BY_NEXT_LESSON` to `Model.sortPersons(...)`, then resets the filter to show all students.
  The comparator orders students by their next lesson's start time, placing students without a next lesson last.
  `ModelManager` delegates sorting to the address book; the storage-list classes are omitted from this focused diagram.
* `UpcomingCommand` reads all students through `Model.getAddressBook()`, compares each future next lesson with the
  next occurrence of each recurring lesson, and shows students whose earliest lesson starts at the overall minimum.
  If none exists, it reports an error.
  The earliest start is the minimum over every student's future one-off lesson and next recurring occurrence, and
  all students tied at that minimum are shown together. The search covers all students, not only the currently
  displayed list.
* `Person` may reference one `NextLesson` and one `RecurringLesson`. `NextLesson.getDateTime()` supplies the one-off
  start time; `RecurringLesson.getNextOccurrence(...)` supplies the next weekly start. `SortCommand` still sorts by
  next lessons only.

### Upcoming lesson selection

`UpcomingCommand` takes one local-time snapshot from its clock, truncated to the minute to match the `HH:mm`
precision of lesson booking, so a lesson starting in the current minute still counts as upcoming.
`RecurringLesson#getNextOccurrence(...)` uses the
next matching weekday, including today, and advances one week if that start minute has passed. The command searches all
student records, not just those currently displayed. A student appears only once when both lesson types represent
the same occurrence. Selection changes only the displayed-list filter, not student records or their saved order.
The filter keeps the same time snapshot until another command changes it; rerun `upcoming` to refresh the selection.
A supplied `Clock` allows deterministic boundary tests.

### Timetable-clash detection

Clash checks return an immutable `LessonClash<T>` containing the conflicting student and the exact next or
recurring lesson selected during validation. Commands obtain the date or weekday, time, and duration from that
result, rather than retrieving the lesson from the student again. The result carries existing immutable objects
without copying records. Next-lesson conflicts retain precedence over recurring conflicts, and feedback wording
and rejected-command state remain unchanged.

`DatedLessonInterval` and `WeeklyLessonInterval` are immutable value objects responsible for overlap arithmetic.
Both use half-open intervals: a start is included, while an end is excluded, so adjacent lessons do not clash.
Dated comparisons use start-time gaps rather than calculating end dates, avoiding overflow at extreme dates.
A dated interval can be projected onto its weekday and time for comparison against a weekly interval. Weekly
comparisons account for occurrences on either side of the Sunday/Monday boundary. `TimetableClashChecker` retains
student identity checks, replacement exclusions, and the same-student occurrence exception.

Both `NextLessonCommand` and `RecurringLessonCommand` use `TimetableClashChecker` before replacing a student record. They check existing next lessons first, then recurring schedules, using all student records rather than only the displayed list. Each check uses the lesson's actual duration; adjacent lessons are allowed, and comparisons account for midnight and week boundaries.

The lesson being replaced is excluded. When comparing a next lesson with the same student's recurring schedule, an exact weekday and start-time match represents the same occurrence and is allowed, even if the topic or duration differs. Other overlapping times are rejected.

When loading saved next lessons, `JsonAdaptedNextLesson` compares the end of the occupied interval with the current local time. It retains ongoing lessons and discards lessons whose end time has been reached. `NextLesson.fromStoredValues` validates saved details without rejecting a past start; the normal constructor continues to reject past starts for new bookings. This keeps clash detection consistent across a restart during a lesson.

The activity diagram below follows parsing, validation, clash rejection, and model updates for both scheduling commands. A rejected command leaves the student records and displayed list unchanged. The diagram ends when the command returns successfully; `LogicManager` then saves the address book through the normal Storage flow. Storage errors are outside this diagram's scope.

<puml src="diagrams/TimetableClashActivityDiagram.puml" alt="Activity diagram for timetable-clash validation and model updates" />

### Recurring lesson feature

The `recurringlesson` command records a weekly lesson comprising a day, time, duration, and topic for a student. Its implementation spans the Model, Logic, Storage, and UI components while keeping the recurring schedule separate from the student's one-off next lesson.

#### Model representation

`RecurringLesson` is a value object containing a `DayOfWeek`, a `LocalTime`, a topic, and a `LessonDuration`. Its constructor requires all four values and trims and rejects a blank topic. `LessonDuration` validates a whole number from 1 to 1440 minutes. `Person` stores the value as an `Optional<RecurringLesson>`, allowing each student to have either zero or one recurring weekly lesson.

Student records are replaced rather than mutated. `Person.withNote(...)`, `withNextLesson(...)`, and `withRecurringLesson(...)` return a new `Person` with only the specified value changed, preserving every other field. The note and lesson setting/clearing commands use these methods; an empty `Optional` explicitly clears a lesson, while null arguments are rejected.

#### Command execution

`AddressBookParser` delegates the command arguments to `RecurringLessonCommandParser`. The command parser requires `day/`, `time/`, `dur/`, and a final `topic/` prefix. It rejects duplicate structured fields before `topic/`, parses the displayed student index, and accepts a case-insensitive day of the week. Everything after the first `topic/`, including further prefix-like text, belongs to the topic. Time must be a valid 24-hour time in the exact `HH:mm` format, and `dur/` must supply a whole number from 1 to 1440 minutes. It then creates the validated `RecurringLesson` and `RecurringLessonCommand` objects.

When executed, `RecurringLessonCommand` obtains the currently displayed student list and checks that the specified index exists. It uses `TimetableClashChecker` to check the proposed lesson against all students' next lessons and recurring schedules, using their durations as described in the timetable clash detection section above. Once these checks pass, it passes the original and replacement `Person` objects to `Model#setPerson(...)`, then resets the displayed-list predicate to `PREDICATE_SHOW_ALL_PERSONS`. If parsing fails, a `ParseException` is thrown; if the displayed index is invalid or the lesson clashes, a `CommandException` is thrown. In these cases, the model is not changed and the address book is not saved.

The following sequence diagram shows the successful execution of `recurringlesson 1 day/MONDAY time/16:30 dur/60 topic/Algebra`, assuming the displayed index is valid and the lesson does not clash with the existing timetable.

<puml src="diagrams/RecurringLessonSequenceDiagram.puml" alt="Sequence diagram for setting a recurring lesson" />

#### Clearing a recurring lesson

`AddressBookParser` delegates `clearrecurringlesson` to `ClearRecurringLessonCommandParser`, which parses the displayed student index and creates a `ClearRecurringLessonCommand`. The command validates the index against the currently displayed student list and checks whether the selected student has a recurring lesson.

If a recurring lesson exists, the command creates a replacement `Person` with `Optional.empty()` in the recurring-lesson field, preserves the student's other details, and passes both records to `Model#setPerson(...)`. If no recurring lesson exists, the student record is left unchanged and the command reports that no recurring lesson is scheduled. Both outcomes reset the displayed-list predicate to `PREDICATE_SHOW_ALL_PERSONS`. An invalid displayed index causes a `CommandException` before the filter is reset or the address book is saved.

The following sequence diagram shows the execution of `clearrecurringlesson 1`, including both possible outcomes for a valid displayed index.

<puml src="diagrams/ClearRecurringLessonSequenceDiagram.puml" alt="Sequence diagram for clearing a recurring lesson" />

Both diagrams treat the Model and Storage components as black boxes. They omit lower-level calls involving argument tokenization, JSON conversion, and exception propagation to keep the command flows readable.

#### Storage and UI updates

After either command changes student data successfully, `LogicManager` passes the address book to the `Storage` component. A `clearrecurringlesson` command that leaves the model unchanged does not save. `JsonAdaptedPerson` represents the optional recurring lesson using `JsonAdaptedRecurringLesson`, which stores its day, time, and topic as JSON-compatible strings and its duration as an integer `durationMinutes` field. When the data is loaded again, the adapter validates the stored values before reconstructing the `RecurringLesson` model object. For compatibility with older data, a missing or null duration defaults to 60 minutes and a stored duration above 1440 is capped at 1440 minutes; zero or negative durations are rejected.

`PersonListPanel` displays the `ObservableList<Person>` exposed by Logic and backed by the model's filtered list. Replacing the `Person` in the model updates this list, after which `PersonCard` formats the recurring schedule for the summary. The `view` command displays the full record, including the topic and note, in `PersonDetailsPanel`.

#### Design considerations

**Aspect: How a recurring schedule is represented:**

* **Alternative 1 (current choice): Store one weekly recurrence rule in each student record.**
  * Pros: Uses a compact value object, avoids generating an unbounded number of future lesson records, and keeps the schedule with the corresponding student.
  * Cons: Supports only one weekly recurring lesson per student and cannot represent exceptions such as a cancellation on a specific date.

* **Alternative 2: Generate and store individual future lesson occurrences.**
  * Pros: Allows a particular occurrence to be cancelled or rescheduled independently.
  * Cons: Requires an end date or generation limit, stores more data, and can become inconsistent with the original recurrence rule.

### \[Proposed\] Undo/redo feature

<box type="info" seamless>

**Proposed design only:** Undo/redo is not implemented in v1.5. The classes, methods, commands, and diagrams in
this section describe a possible future extension; they are not APIs or behaviour available in the current app.
</box>

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations would be exposed in the proposed `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.




--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.html)
* [Testing guide](Testing.html)
* [Logging guide](Logging.html)
* [DevOps guide](DevOps.html)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**: Private tutors who manage multiple students across different subjects, school levels, and lesson schedules, and who are comfortable using a keyboard-driven desktop application.

**Value proposition**: TutorTrack lets private tutors retrieve and update student information quickly by keeping contact details, academic context, and upcoming lesson information together in one local application.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …        | I want to …                                                            | So that I can…                                                |
|----------|---------------|------------------------------------------------------------------------|---------------------------------------------------------------|
| `* * *`  | new user      | view concise usage instructions                                        | start using TutorTrack without external assistance            |
| `* * *`  | private tutor | add a student with contact details and academic information            | keep the information needed for tutoring in one place         |
| `* * *`  | private tutor | list all students                                                      | see the students currently under my care                      |
| `* * *`  | private tutor | find students by name                                                  | retrieve a student's record quickly                           |
| `* * *`  | private tutor | filter students by school level, subject, or tag                       | focus on one group of students                                |
| `* * *`  | private tutor | view a student's complete record                                       | prepare for a lesson efficiently                              |
| `* * *`  | private tutor | edit a student's details                                               | keep the student record accurate                              |
| `* * *`  | private tutor | record the date, time, duration, and topic of a student's next lesson  | know when the lesson is scheduled and what to prepare         |
| `* * *`  | private tutor | delete a student                                                       | remove an obsolete, incorrect, or duplicate record            |
| `* *`    | private tutor | add free-form notes to a student's record                              | remember student-specific information that has no fixed field |
| `* *`    | private tutor | tag students using meaningful categories                               | group students by attributes such as subject or school level  |
| `* *`    | private tutor | view my earliest upcoming lesson across all students                   | know which lesson to prepare for first                        |
| `* *`    | private tutor | set a recurring lesson schedule for a student                          | avoid entering the same lesson details repeatedly             |
| `* *`    | private tutor | clear a student's next or recurring lesson                             | remove lessons that were cancelled or are no longer needed    |
| `* *`    | private tutor | be told when a lesson overlaps another student's lesson                | avoid arranging overlapping lessons                           |
| `*`      | private tutor | sort students by their next lesson                                     | prioritise lesson preparation                                 |
| `*`      | private tutor | view a student's lesson history and learning-progress summary          | understand the student's progress over time                   |

### Use cases

(For all use cases below, the **System** is TutorTrack and the **Actor** is a private tutor.)

#### Use case: UC01 — Add a student

**MSS**

1. Tutor enters the details of a new student.
2. TutorTrack validates the supplied details.
3. TutorTrack adds the student record.
4. TutorTrack displays a confirmation containing the added student's details.

   Use case ends.

**Extensions**

* 2a. One or more required fields are missing or invalid.
  * 2a1. TutorTrack displays an error describing the expected input.
  * 2a2. Use case resumes at step 1.
* 2b. Another student has the same name, ignoring capitalisation and extra spaces.
  * 2b1. TutorTrack informs the tutor that the student already exists.
  * 2b2. Use case resumes at step 1.

#### Use case: UC02 — Find and update a student

**MSS**

1. Tutor searches for a student by name.
2. TutorTrack displays all matching students.
3. Tutor selects a student from the displayed list and supplies the updated details.
4. TutorTrack validates the supplied details.
5. TutorTrack updates the student record.
6. TutorTrack displays a confirmation containing the updated student's details.

   Use case ends.

**Extensions**

* 2a. No student matches the search terms.
  * 2a1. TutorTrack informs the tutor that no students were found.
  * 2a2. Use case ends.
* 3a. The selected index does not refer to a student in the displayed list.
  * 3a1. TutorTrack displays an invalid-index error.
  * 3a2. Use case resumes at step 3.
* 4a. One or more updated fields are invalid.
  * 4a1. TutorTrack displays an error describing the expected input.
  * 4a2. Use case resumes at step 3.

#### Use case: UC03 — Record a student's next lesson

**MSS**

1. Tutor searches for the student by name.
2. TutorTrack displays all matching students.
3. Tutor selects the student and enters the next lesson's date, time, duration, and topic.
4. TutorTrack validates the lesson information.
5. TutorTrack records the next lesson in the student's record.
6. TutorTrack displays the updated student record.

   Use case ends.

**Extensions**

* 2a. No student matches the search terms.
  * 2a1. TutorTrack informs the tutor that no students were found.
  * 2a2. Use case ends.
* 3a. The selected index does not refer to a student in the displayed list.
  * 3a1. TutorTrack displays an invalid-index error.
  * 3a2. Use case resumes at step 3.
* 4a. The lesson date, time, duration, or topic is invalid, or the lesson starts in the past.
  * 4a1. TutorTrack displays an error describing the expected input.
  * 4a2. Use case resumes at step 3.
* 4b. The lesson overlaps another student's lesson.
  * 4b1. TutorTrack identifies the conflicting student and lesson, and leaves all records unchanged.
  * 4b2. Use case resumes at step 3.

#### Use case: UC04 — Delete a student

**MSS**

1. Tutor requests to list all students.
2. TutorTrack displays the student list.
3. Tutor selects a student from the displayed list for deletion.
4. TutorTrack deletes the student record.
5. TutorTrack displays a confirmation containing the deleted student's details.

   Use case ends.

**Extensions**

* 2a. The student list is empty.
  * 2a1. TutorTrack informs the tutor that there are no students to delete.
  * 2a2. Use case ends.
* 3a. The selected index does not refer to a student in the displayed list.
  * 3a1. TutorTrack displays an invalid-index error.
  * 3a2. Use case resumes at step 3.

#### Use case: UC05 — View a student's complete record

**MSS**

1. Tutor requests to view a student using an index from the displayed list.
2. TutorTrack validates the supplied index.
3. TutorTrack displays the student's complete record in the right-hand panel.

   Use case ends.

**Extensions**

* 2a. The selected index does not refer to a student in the displayed list.
  * 2a1. TutorTrack displays an invalid-index error.
  * 2a2. Use case resumes at step 1.

#### Use case: UC06 — Set a student's recurring weekly lesson

**MSS**

1. Tutor enters the index of a student with a weekday, time, duration, and topic.
2. TutorTrack validates the lesson information.
3. TutorTrack checks the lesson against all students' next and recurring lessons.
4. TutorTrack replaces the student's recurring lesson and displays the updated record.

   Use case ends.

**Extensions**

* 1a. The index does not refer to a student in the displayed list.
  * 1a1. TutorTrack displays an invalid-index error.
  * 1a2. Use case resumes at step 1.
* 2a. The weekday, time, duration, or topic is invalid.
  * 2a1. TutorTrack displays an error describing the expected input.
  * 2a2. Use case resumes at step 1.
* 3a. The weekly lesson overlaps another student's lesson, including across midnight or from Sunday into Monday.
  * 3a1. TutorTrack identifies the conflicting student and lesson, and leaves all records unchanged.
  * 3a2. Use case resumes at step 1.

#### Use case: UC07 — Filter students and find the earliest lesson

**MSS**

1. Tutor filters students by school level, subject, and/or tags.
2. TutorTrack displays only the students matching every criterion.
3. Tutor requests the earliest upcoming lesson.
4. TutorTrack displays the students whose lesson starts earliest, searching all students.

   Use case ends.

**Extensions**

* 1a. No criterion is given, or a criterion is blank or invalid.
  * 1a1. TutorTrack displays an error describing the expected input.
  * 1a2. Use case resumes at step 1.
* 2a. No student matches.
  * 2a1. TutorTrack shows an empty list. Use case resumes at step 1.
* 4a. No lesson is scheduled.
  * 4a1. TutorTrack displays an error. Use case ends.

### Non-Functional Requirements

1. TutorTrack should work on any _mainstream OS_ with Java `25` installed.
2. TutorTrack should respond to a command within 1 second when managing up to 1,000 student records under normal operating conditions.
3. TutorTrack should persist data from every successful data-modifying command and recover that data on the next launch after a normal shutdown.
4. TutorTrack should store all student data locally and should not require an internet connection for normal operation.
5. A tutor with above-average typing speed for regular English text should be able to complete common operations faster using commands than by navigating an equivalent mouse-driven interface.
6. A tutor who is familiar with command-line applications should be able to learn the syntax for the core commands within 30 minutes using only the in-app help and User Guide.
7. TutorTrack should remain usable on screens with a resolution of 1280 × 720 or higher without information required for common operations being permanently inaccessible.

### Glossary

* **Academic information**: Tutoring-related information about a student, such as the student's subject and school level.
* **Displayed student list**: The numbered list of students currently shown in TutorTrack. It may contain every student or only the results of the latest search.
* **Mainstream OS**: A currently supported version of Windows, Linux, or macOS that can run Java `25`.
* **Next lesson**: A one-off lesson recorded for a student, with a date, start time, duration, and topic. A student has at most one.
* **Recurring lesson**: A weekly lesson recorded for a student, with a weekday, start time, duration, and topic. A student has at most one.
* **Timetable clash**: Two lessons, across any students, whose time ranges overlap. Lessons that end exactly when another starts do not clash.
* **Private tutor**: A person who independently provides lessons to one or more students and manages the corresponding student information and schedules.
* **Student index**: The positive integer displayed beside a student in the current displayed student list. It is not a permanent identifier and may change when the displayed list changes.
* **Student record**: The collection of contact details, academic information, notes, tags, and lesson information that TutorTrack stores for one student.
* **Tag**: A user-defined label attached to a student record for categorisation.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

Use a separate writable folder with disposable data. Confirm Java 25 with `java -version`; Mac users should use
the Java installation linked from the User Guide. Run each launch from the folder containing the JAR.

1. Launch with `java -jar "TutorTrack.jar"`.<br>
   Expected: The GUI opens with sample students on the first launch. The command box is below the feedback area.
1. Resize and move the window, then close it using its close button.
1. Relaunch using the same command and working directory.<br>
   Expected: The previous window size and position are restored from `preferences.json`.
1. Run `help`, then close the Help window and run `exit`.<br>
   Expected: Help displays the TutorTrack guide URL, and exiting closes the application windows.

### Walkthrough of all features

Use a separate test folder. Start with `clear` so that the data is known, then run the commands in order.
Lesson dates must be in the future, so the examples use 2099.

| Step | Command | Expected result |
| --- | --- | --- |
| 1 | `help` | The help window opens, showing a link to the User Guide. |
| 2 | `clear` | All students are removed and the list is empty. |
| 3 | `add n/Tester One p/91234567 e/one@example.com a/Block 1 l/Secondary 4 s/Mathematics t/walk` | Student 1 is added and appears in the list. |
| 4 | `add n/Tester Two p/92345678 e/two@example.com a/Block 2 l/Primary 6 s/Science t/walk` | Student 2 is added. |
| 5 | `add n/tester  ONE p/93456789 e/dup@example.com a/Block 3` | Rejected as a duplicate name (capitalisation and spaces are ignored). |
| 6 | `list` | Both students are displayed. |
| 7 | `view 1` | The details panel shows Tester One. |
| 8 | `edit 1 p/90000000 t/walk t/edited` | Phone and tags of student 1 are updated. |
| 9 | `find tester` | Both students are displayed. |
| 10 | `filter s/Science` | Only Tester Two is displayed. |
| 11 | `list` | Both students are displayed again. |
| 12 | `note 1 note/Needs extra practice`, then `view 1` | The details panel shows Tester One with the saved note. |
| 13 | `nextlesson 1 date/2099-01-05 time/14:00 dur/60 topic/Algebra` | The lesson is saved for student 1. |
| 14 | `nextlesson 2 date/2099-01-05 time/14:30 dur/60 topic/Cells` | Rejected with a message naming the clashing student. |
| 15 | `nextlesson 2 date/2099-01-05 time/15:00 dur/60 topic/Cells` | Accepted, because back-to-back lessons are allowed. |
| 16 | `recurringlesson 1 day/MONDAY time/16:30 dur/90 topic/Revision` | The recurring lesson is saved for student 1. |
| 17 | `upcoming` | Tester One is shown, at the next Monday 16:30 occurrence of the recurring lesson from step 16, which is earlier than the 2099 one-off lesson. |
| 18 | `sort` | Students with lessons are listed in lesson order. |
| 19 | `clearlesson 1` | Student 1's next lesson is cleared. |
| 20 | `clearrecurringlesson 1` | Student 1's recurring lesson is cleared. |
| 21 | `delete 2` | Tester Two is deleted. |
| 22 | `clear` | All students are removed. |

### Deleting a student

Use a separate test copy. Run `clear` and then add two disposable students:

```text
add n/Delete Alice p/90000001 e/deletealice@example.com a/Block One
add n/Delete Ben p/90000002 e/deleteben@example.com a/Block Two
```

1. Run `list`, followed by `delete 1`.<br>
   Expected: Delete Alice is removed, Delete Ben remains, and feedback identifies the deleted student.
1. Run `delete 0`, `delete x`, `delete`, and `delete 2` separately.<br>
   Expected: Each command reports an invalid format or index. Delete Ben remains unchanged.
1. Run `find Ben`, followed by `delete 1`.<br>
   Expected: Delete Ben is removed using its displayed index. Run `list` to confirm that no students remain.

### Filtering students

Use a separate test copy of TutorTrack in an empty folder so these checks do not affect real student records.
Run the following commands once to add four test students. The `filtercheck` tag distinguishes them from the
sample students loaded on first launch.

```text
add n/Filter Alice p/90000001 e/filteralice@example.com a/Block 1 l/Secondary 4 s/Mathematics t/filtercheck t/weekday t/exam
add n/Filter Ben p/90000002 e/filterben@example.com a/Block 2 l/Secondary 4 s/Mathematics t/filtercheck t/weekday
add n/Filter Cara p/90000003 e/filtercara@example.com a/Block 3 l/Secondary 4 s/Physics t/filtercheck t/weekday t/exam
add n/Filter Dylan p/90000004 e/filterdylan@example.com a/Block 4 l/Secondary 3 s/Mathematics t/filtercheck t/weekday t/exam
```

Run `filter t/filtercheck` before each case below. It should display all four test students.

| Test case | Expected result |
| --- | --- |
| `filter l/Secondary 4 s/Mathematics t/filtercheck t/weekday t/exam` | Only Filter Alice is displayed. All criteria must match; Ben lacks `exam`, Cara has a different subject, and Dylan has a different school level. |
| `filter t/filtercheck t/weekday t/exam` | Alice, Cara, and Dylan are displayed. Ben is excluded even though he has one of the two requested lesson tags. |
| `filter t/filtercheck s/Biology` | No students are displayed, and the result reports zero matches. No records are deleted. |
| `filter` or `filter students s/Mathematics` | The command usage error is displayed, and the four test students remain displayed. |
| `filter l/` | `School level must not be blank.` is displayed, and the displayed list is unchanged. |
| `filter s/` | `Subject must not be blank.` is displayed, and the displayed list is unchanged. |
| `filter t/`, `filter t/week day`, or `filter t/weekday t/exam!` | `Tag names should be alphanumeric` is displayed, and the displayed list is unchanged. |

To test resetting the displayed list:

1. Run `list` and note the total number of students, including the sample students and the four test students.
1. Run `filter l/Secondary 4 s/Mathematics t/filtercheck t/exam`. Only Filter Alice should be displayed.
1. Run `list` again. All students should reappear with the original total and unchanged details.
1. Run `filter t/filtercheck s/Biology`, followed by `list`. All students should also reappear after an empty result.

### Saving data

Use a separate writable test folder containing `TutorTrack.jar` and disposable data. Open a terminal in that
folder and launch with `java -jar "TutorTrack.jar"`, using Java 25 and the Mac Java installation described in the
User Guide where applicable. Use the same working directory on every launch: the relative path
`data/addressbook.json` must refer to the same file. Do not use real student records.

#### Prepare a known saved record

1. Launch the app and run these commands. `clear` removes the sample students from this test copy.

   ```text
   clear
   add n/Persistence Check p/90000001 e/persistence@example.com a/Block One l/Secondary 4 s/Mathematics t/persistcheck
   note 1 note/Keep this note after restart
   ```

1. Close the app using the window's close button and relaunch from the same folder.<br>
   Expected: Exactly one student, Persistence Check, appears with the academic fields, tag, and note unchanged.
1. Close the app. Copy `data/addressbook.json` to `addressbook.backup.json` in the test folder, outside `data`.<br>
   Expected: The backup contains the known record. Keep this backup unchanged throughout the checks below.

#### Missing data file

1. With the app closed, move `data/addressbook.json` to `addressbook.missing-test.json` in the test folder.
1. Relaunch the app.<br>
   Expected: Sample students appear instead of Persistence Check. A missing file causes sample data to be loaded;
   the backup is not restored automatically.
1. Before entering any command, inspect the `data` folder.<br>
   Expected: `addressbook.json` is still absent. Loading sample data alone does not save it.
1. Run `list`.<br>
   Expected: Sample students are listed and `data/addressbook.json` is still absent. Read-only commands do not save.
1. Run `clear`.<br>
   Expected: The sample students are removed and `data/addressbook.json` is created with an empty `persons` array.
1. Close the app, copy `addressbook.backup.json` over `data/addressbook.json`, and relaunch.<br>
   Expected: Only Persistence Check appears, with the original academic fields, tag, and note.

#### Corrupted data file

1. Close the app and confirm that the unchanged backup still exists.
1. Open `data/addressbook.json` in a plain-text editor. Replace its entire contents with this invalid JSON and save:

   ```text
   { invalid json
   ```

1. Relaunch the app.<br>
   Expected: An empty student list appears, rather than sample students or a partially recovered record. The
   application log records the loading failure; the current implementation does not require a GUI error dialog.
1. Before entering any command, inspect the file.<br>
   Expected: Its invalid contents remain. Loading an empty list does not itself repair the file.
1. Run `list`.<br>
   Expected: No students are listed and the invalid file remains unchanged. Read-only commands, including `exit`,
   do not save the empty list.
1. Run `add n/Recovery Check p/90000002 e/recovery@example.com a/Block Two`.<br>
   Expected: Recovery Check is added and the file is overwritten with valid JSON containing that student. Keep the
   backup so the original data can be restored after this test.
1. Close the app, copy the backup over `data/addressbook.json`, and relaunch.<br>
   Expected: Persistence Check and all its saved details return.

#### Valid JSON containing an invalid student

1. Close the app and restore the known-good file from the backup.
1. Edit the student's `phone` value in `data/addressbook.json` to `"abc"`, keeping valid JSON syntax.
1. Relaunch without entering a command.<br>
   Expected: An empty list appears because the student fails phone validation. The invalid file remains unchanged
   until a command changes the student records and saves them. Valid JSON syntax does not make invalid student values acceptable.
1. Close the app using the window's close button, restore the backup over the invalid file, and relaunch.<br>
   Expected: The known student and all saved details return. Close the app before removing the disposable test
   folder. Retain the backup if further testing is planned.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Planned enhancements**

Team size: 4

1. **Include recurring lessons when sorting.** `sort` currently orders students by their one-off next lesson only, so a
   student with only a weekly lesson is listed with unscheduled students. We plan to sort by the earlier of the next
   lesson and the next weekly occurrence, the same start time that `upcoming` uses.
1. **Ignore completed one-off lessons before restart.** A one-off lesson that has ended is removed only when TutorTrack
   next starts, so until then it can still be listed first by `sort` and shown on the student card. We plan to treat a
   lesson as unscheduled once its end time has passed, without waiting for a restart.
1. **Check saved lessons for clashes when loading.** Lessons edited directly in the data file are validated
   individually but not against each other, so overlapping lessons can be loaded. We plan to reject such a data file
   on loading, as with other invalid data, and name the clashing students in the log.
1. **Limit how far ahead a lesson can be scheduled.** `nextlesson` accepts any valid date up to `9999-12-31`, so a
   mistyped year such as `2206` is saved without warning. We plan to reject lesson dates more than 5 years ahead with
   the message `Lesson date must be within 5 years from today.`
