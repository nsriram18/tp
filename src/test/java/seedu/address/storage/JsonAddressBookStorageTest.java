package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.NextLessonCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.NextLesson;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullStudent_reportsLoadErrorAndPreservesFile() throws Exception {
        assertMalformedDataIsPreserved("{\"persons\": [null]}");
    }

    @Test
    public void readAddressBook_nullTag_reportsLoadErrorAndPreservesFile() throws Exception {
        assertMalformedDataIsPreserved("""
                {"persons": [{"name": "Test Student", "phone": "90000000",
                  "email": "student@example.com", "address": "Block One", "tags": [null]}]}
                """);
    }

    private void assertMalformedDataIsPreserved(String contents) throws Exception {
        Path filePath = testFolder.resolve("malformed.json");
        Files.writeString(filePath, contents);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        DataLoadingException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DataLoadingException.class, storage::readAddressBook);
        assertInstanceOf(IllegalValueException.class, exception.getCause());
        assertEquals(contents, Files.readString(filePath));
    }

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void readAddressBook_fractionalLessonDuration_throwsDataLoadingException() throws Exception {
        for (String lessonField : List.of("nextLesson", "recurringLesson")) {
            for (String duration : List.of("1.9", "0.9", "60.5", "6.05e1")) {
                Path filePath = testFolder.resolve("fractionalDuration.json");
                String contents = studentWithLessonJson(lessonField, duration);
                Files.writeString(filePath, contents);
                JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

                assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
                assertEquals(contents, Files.readString(filePath));
            }
        }
    }

    @Test
    public void readAddressBook_integerAndLegacyLessonDurations_preservesLoadingRules() throws Exception {
        for (String lessonField : List.of("nextLesson", "recurringLesson")) {
            String[] durations = {"1", "90", "1440", null, "1500"};
            int[] expectedMinutes = {1, 90, 1440, 60, 1440};
            for (int i = 0; i < durations.length; i++) {
                Path filePath = testFolder.resolve("validDuration.json");
                Files.writeString(filePath, studentWithLessonJson(lessonField, durations[i]));
                JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
                var student = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
                int actualMinutes = lessonField.equals("nextLesson")
                        ? student.getNextLesson().orElseThrow().duration.getDurationMinutes()
                        : student.getRecurringLesson().orElseThrow().getDuration().getDurationMinutes();
                assertEquals(expectedMinutes[i], actualMinutes, lessonField + ": " + durations[i]);
            }
        }
    }

    private static String studentWithLessonJson(String lessonField, String duration) {
        String schedule = lessonField.equals("nextLesson") ? "\"date\": \"2099-06-15\""
                : "\"day\": \"MONDAY\"";
        String durationField = duration == null ? "" : ", \"durationMinutes\": " + duration;
        return """
                {"persons": [{"name": "Alpha", "phone": "90000001", "email": "alpha@example.com",
                  "address": "Block One", "tags": [], "%s": {
                    %s, "time": "10:00", "topic": "Algebra"%s
                  }}]}
                """.formatted(lessonField, schedule, durationField);
    }

    @Test
    public void readAddressBook_jsonNull_throwsDataLoadingExceptionWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("nullAddressBook.json");
        String contents = " \n null \n ";
        Files.writeString(filePath, contents);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        assertEquals(contents, Files.readString(filePath));
    }

    @Test
    public void readAddressBook_validEmptyAddressBook_returnsPresentEmptyBook() throws Exception {
        Path filePath = testFolder.resolve("emptyAddressBook.json");
        Files.writeString(filePath, "{\"persons\": []}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_jsonNull_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("null.json");
        Files.writeString(filePath, "null");
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
    }

    @Test
    public void readAddressBook_emptyPersonList_success() throws Exception {
        Path filePath = testFolder.resolve("empty.json");
        Files.writeString(filePath, "{ \"persons\": [] }");
        assertEquals(new AddressBook(), new AddressBook(new JsonAddressBookStorage(filePath).readAddressBook().get()));
    }

    @Test
    public void readAddressBook_fractionalRecurringLessonDuration_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("fractionalRecurring.json");
        Files.writeString(filePath, personJson("\"recurringLesson\": {\"day\": \"MONDAY\", \"time\": \"10:00\", "
                + "\"topic\": \"Algebra\", \"durationMinutes\": 1.9}"));
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
    }

    @Test
    public void readAddressBook_fractionalNextLessonDuration_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("fractionalNext.json");
        Files.writeString(filePath, personJson("\"nextLesson\": {\"date\": \"2999-01-01\", \"time\": \"10:00\", "
                + "\"topic\": \"Algebra\", \"durationMinutes\": 60.5}"));
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
    }

    @Test
    public void readAddressBook_integerRecurringLessonDuration_loadsStoredDuration() throws Exception {
        Path filePath = testFolder.resolve("integerDuration.json");
        Files.writeString(filePath, personJson("\"recurringLesson\": {\"day\": \"MONDAY\", \"time\": \"10:00\", "
                + "\"topic\": \"Algebra\", \"durationMinutes\": 90}"));

        var student = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow()
                .getPersonList().getFirst();

        assertEquals(90, student.getRecurringLesson().orElseThrow().getDuration().getDurationMinutes());
    }

    @Test
    public void readAddressBook_missingRecurringLessonDuration_loadsDefaultDuration() throws Exception {
        Path filePath = testFolder.resolve("legacyDuration.json");
        Files.writeString(filePath, personJson("\"recurringLesson\": {\"day\": \"MONDAY\", \"time\": \"10:00\", "
                + "\"topic\": \"Algebra\"}"));

        var student = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow()
                .getPersonList().getFirst();

        assertEquals(LessonDuration.DEFAULT_DURATION_MINUTES,
                student.getRecurringLesson().orElseThrow().getDuration().getDurationMinutes());
    }

    @Test
    public void readAddressBook_nonNumericOrNonPositiveLessonDuration_throwsDataLoadingException()
            throws Exception {
        for (String lessonField : List.of("nextLesson", "recurringLesson")) {
            for (String duration : List.of("0", "-5", "\"abc\"")) {
                Path filePath = testFolder.resolve("invalidDuration.json");
                Files.writeString(filePath, studentWithLessonJson(lessonField, duration));
                JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

                assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
            }
        }
    }

    private static String personJson(String lessonField) {
        return "{ \"persons\": [ { \"name\": \"Alpha\", \"phone\": \"90000001\", \"email\": \"alpha@example.com\", "
                + "\"address\": \"Block One\", \"tags\": [], " + lessonField + " } ] }";
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    @Test
    public void saveAndReload_noteAddedReplacedAndCleared_preservesText() throws Exception {
        Path filePath = testFolder.resolve("StudentNotes.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);

        String[] notes = {"Parent says \"practise 数学\"; prefers email.\nLearning needs: algebra",
            "Updated parent preferences", ""};
        for (String note : notes) {
            book.setPerson(book.getPersonList().getFirst(), new PersonBuilder(ALICE).withNote(note).build());
            storage.saveAddressBook(book);
            AddressBook reloaded = new AddressBook(storage.readAddressBook().orElseThrow());
            assertEquals(book, reloaded);
            assertEquals(note, reloaded.getPersonList().getFirst().getNote().value);
        }
    }

    @Test
    public void readAddressBook_ongoingLesson_preservesClashAfterRestart() throws Exception {
        Path file = testFolder.resolve("OngoingLesson.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        AddressBook original = new AddressBook();
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        NextLesson ongoing = NextLesson.fromStoredValues(start.toLocalDate(), start.toLocalTime(),
                "Algebra", new LessonDuration("60"));
        original.addPerson(new PersonBuilder(ALICE).withNextLesson(ongoing).build());
        original.addPerson(BENSON);
        storage.saveAddressBook(original);

        AddressBook reloaded = new AddressBook(storage.readAddressBook().orElseThrow());
        LocalDateTime overlappingStart = LocalDateTime.now().plusMinutes(10);
        NextLesson overlapping = new NextLesson(overlappingStart.toLocalDate(), overlappingStart.toLocalTime(),
                "Geometry", new LessonDuration("30"));
        Model model = new ModelManager(reloaded, new UserPrefs());

        assertEquals(original, reloaded);
        assertEquals(ongoing, reloaded.getPersonList().getFirst().getNextLesson().orElseThrow());
        assertThrows(CommandException.class, () ->
                new NextLessonCommand(INDEX_SECOND_PERSON, overlapping).execute(model));
        assertEquals(reloaded, model.getAddressBook());
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
