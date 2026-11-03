package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Checks that booking, upcoming selection and disk loading share an explicitly supplied time source.
 */
public class LessonClockIntegrationTest {

    private static final LocalDateTime START = LocalDateTime.of(2000, 1, 1, 10, 0);
    private static final String BOOKING = "nextlesson 1 date/2000-01-01 time/10:00 dur/60 topic/Algebra";

    @TempDir
    public Path testFolder;

    @Test
    public void execute_beforeAndAtStart_usesClockThroughoutBookingSelectionAndReload() throws Exception {
        for (LocalDateTime now : new LocalDateTime[]{START.minusNanos(1), START}) {
            Clock clock = clockAt(now);
            JsonAddressBookStorage fileStorage = new JsonAddressBookStorage(testFolder.resolve("students.json"), clock);
            StorageManager storage = new StorageManager(fileStorage,
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
            Model model = createModel();
            LogicManager logic = new LogicManager(model, storage, clock);

            logic.execute(BOOKING);
            Person bookedStudent = model.getAddressBook().getPersonList().getFirst();
            logic.execute("upcoming");

            assertEquals(List.of(bookedStudent), model.getFilteredPersonList());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            assertEquals(START, bookedStudent.getNextLesson().orElseThrow().getDateTime());
        }
    }

    @Test
    public void execute_justAfterStart_acceptsBookingInCurrentMinute() throws Exception {
        Clock clock = clockAt(START.plusNanos(1));
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("students.json"), clock),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Model model = createModel();

        new LogicManager(model, storage, clock).execute(BOOKING);

        assertEquals(START, model.getAddressBook().getPersonList().getFirst().getNextLesson()
                .orElseThrow().getDateTime());
    }

    @Test
    public void execute_startInPreviousMinute_rejectsNewBookingWithoutChangingStudents() {
        Clock clock = clockAt(START.plusMinutes(1));
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("students.json"), clock),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Model model = createModel();
        AddressBook original = new AddressBook(model.getAddressBook());

        assertThrows(ParseException.class, () -> new LogicManager(model, storage, clock).execute(BOOKING));
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void readAddressBook_afterLessonEnds_clearsCompletedLesson() throws Exception {
        Clock bookingClock = clockAt(START.minusDays(1));
        Path file = testFolder.resolve("students.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(file, bookingClock),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        new LogicManager(createModel(), storage, bookingClock).execute(BOOKING);

        AddressBook reloaded = new AddressBook(new JsonAddressBookStorage(file, clockAt(START.plusHours(1)))
                .readAddressBook().orElseThrow());

        assertTrue(reloaded.getPersonList().getFirst().getNextLesson().isEmpty());
        assertEquals(2, reloaded.getPersonList().size());
    }

    private static Model createModel() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        return new ModelManager(book, new UserPrefs());
    }

    private static Clock clockAt(LocalDateTime time) {
        ZoneId zone = ZoneId.of("Asia/Singapore");
        return Clock.fixed(time.atZone(zone).toInstant(), zone);
    }
}
