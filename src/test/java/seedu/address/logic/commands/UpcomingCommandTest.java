package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.LessonUtil.futureLesson;
import static seedu.address.testutil.LessonUtil.withLesson;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.NextLesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecurringLesson;
import seedu.address.testutil.PersonBuilder;

public class UpcomingCommandTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(10);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_fixedClockAtStartBoundary_includesOnlyUnstartedLessons() throws Exception {
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 10, 0);
        ZoneId zone = ZoneId.of("Asia/Singapore");
        Clock bookingClock = Clock.fixed(start.minusDays(1).atZone(zone).toInstant(), zone);
        NextLesson lesson = new NextLesson(start.toLocalDate(), start.toLocalTime(), "Algebra",
                new LessonDuration("60"), bookingClock);
        Person original = model.getFilteredPersonList().getFirst();
        Person student = new PersonBuilder(original).withNextLesson(lesson).build();
        model.setPerson(original, student);

        for (LocalDateTime now : new LocalDateTime[]{start.minusNanos(1), start, start.plusSeconds(30)}) {
            new UpcomingCommand(Clock.fixed(now.atZone(zone).toInstant(), zone)).execute(model);
            assertEquals(List.of(student), model.getFilteredPersonList());
        }

        Clock nextMinute = Clock.fixed(start.plusMinutes(1).atZone(zone).toInstant(), zone);
        assertCommandFailure(new UpcomingCommand(nextMinute), model, UpcomingCommand.MESSAGE_NO_UPCOMING_LESSON);
    }

    @Test
    public void constructor_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UpcomingCommand(null));
    }

    @Test
    public void execute_noLessons_throwsCommandException() {
        assertCommandFailure(new UpcomingCommand(), model, UpcomingCommand.MESSAGE_NO_UPCOMING_LESSON);
    }

    @Test
    public void execute_multipleLessons_showsOnlyEarliest() throws Exception {
        List<Person> students = List.copyOf(model.getAddressBook().getPersonList());
        Person earliest = withLesson(students.get(2), futureLesson(10, 9, 0, 60));
        model.setPerson(students.get(1), withLesson(students.get(1), futureLesson(10, 14, 0, 60)));
        model.setPerson(students.get(2), earliest);
        model.setPerson(students.get(4), withLesson(students.get(4), futureLesson(11, 8, 0, 60)));

        CommandResult result = new UpcomingCommand().execute(model);

        assertEquals(List.of(earliest), model.getFilteredPersonList());
        assertEquals(String.format(UpcomingCommand.MESSAGE_SUCCESS, DATE + " 09:00"), result.getFeedbackToUser());
    }

    @Test
    public void execute_tiedLessons_showsAllTiedStudents() throws Exception {
        List<Person> students = List.copyOf(model.getAddressBook().getPersonList());
        Person first = withLesson(students.get(0), futureLesson(10, 9, 0, 60));
        Person second = withLesson(students.get(3), futureLesson(10, 9, 0, 60));
        model.setPerson(students.get(0), first);
        model.setPerson(students.get(3), second);

        new UpcomingCommand().execute(model);

        assertEquals(List.of(first, second), model.getFilteredPersonList());
    }

    @Test
    public void execute_activeFilter_searchesWholeAddressBook() throws Exception {
        List<Person> students = List.copyOf(model.getAddressBook().getPersonList());
        Person earliest = withLesson(students.get(0), futureLesson(10, 9, 0, 60));
        model.setPerson(students.get(0), earliest);
        model.updateFilteredPersonList(person -> person.isSamePerson(students.get(1)));

        new UpcomingCommand().execute(model);

        assertEquals(List.of(earliest), model.getFilteredPersonList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UpcomingCommand().execute(null));
    }

    @Test
    public void execute_recurringOnly_usesNextOccurrenceAcrossWeekBoundary() throws Exception {
        Person student = withRecurring(model.getAddressBook().getPersonList().get(0), DayOfWeek.MONDAY, 9);
        model.setPerson(model.getAddressBook().getPersonList().get(0), student);
        CommandResult result = at("2030-06-16T23:59:00").execute(model);
        assertEquals(List.of(student), model.getFilteredPersonList());
        assertEquals(String.format(UpcomingCommand.MESSAGE_SUCCESS, "2030-06-17 09:00"), result.getFeedbackToUser());
    }

    @Test
    public void execute_recurringStartBoundary_rollsForwardOnlyAfterStart() throws Exception {
        Person student = withRecurring(model.getAddressBook().getPersonList().get(0), DayOfWeek.MONDAY, 9);
        model.setPerson(model.getAddressBook().getPersonList().get(0), student);
        for (String now : List.of("2030-06-17T08:59:59", "2030-06-17T09:00:00", "2030-06-17T09:00:30")) {
            assertEquals(String.format(UpcomingCommand.MESSAGE_SUCCESS, "2030-06-17 09:00"),
                    at(now).execute(model).getFeedbackToUser());
        }
        assertEquals(String.format(UpcomingCommand.MESSAGE_SUCCESS, "2030-06-24 09:00"),
                at("2030-06-17T09:01:00").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_mixedLessons_selectsEarliestAndIncludesHiddenRecurringStudent() throws Exception {
        List<Person> students = List.copyOf(model.getAddressBook().getPersonList());
        Person recurring = withRecurring(students.get(0), DayOfWeek.MONDAY, 9);
        Person oneOff = withLesson(students.get(1), lessonOn(LocalDate.of(2030, 6, 18), 10));
        model.setPerson(students.get(0), recurring);
        model.setPerson(students.get(1), oneOff);
        model.updateFilteredPersonList(person -> person.equals(oneOff));
        at("2030-06-17T08:00:00").execute(model);
        assertEquals(List.of(recurring), model.getFilteredPersonList());
        at("2030-06-17T09:01:00").execute(model);
        assertEquals(List.of(oneOff), model.getFilteredPersonList());
    }

    @Test
    public void execute_tiedLessonTypes_eachStudentAppearsOnceAndRecordsUnchanged() throws Exception {
        List<Person> students = List.copyOf(model.getAddressBook().getPersonList());
        Person both = withRecurring(withLesson(students.get(0), lessonOn(LocalDate.of(2030, 6, 17), 9)),
                DayOfWeek.MONDAY, 9);
        Person recurring = withRecurring(students.get(1), DayOfWeek.MONDAY, 9);
        model.setPerson(students.get(0), both);
        model.setPerson(students.get(1), recurring);
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        at("2030-06-17T08:00:00").execute(model);
        assertEquals(List.of(both, recurring), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_pastOneOffWithNoRecurrence_preservesFilterOnFailure() {
        Person student = withLesson(model.getAddressBook().getPersonList().get(0),
                lessonOn(LocalDate.of(2030, 6, 17), 9));
        model.setPerson(model.getAddressBook().getPersonList().get(0), student);
        model.updateFilteredPersonList(person -> person.equals(student));
        assertCommandFailure(at("2030-06-17T09:01:00"), model, UpcomingCommand.MESSAGE_NO_UPCOMING_LESSON);
        assertEquals(List.of(student), model.getFilteredPersonList());
    }

    private static UpcomingCommand at(String now) {
        return new UpcomingCommand(Clock.fixed(LocalDateTime.parse(now).toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
    }

    private static Person withRecurring(Person person, DayOfWeek day, int hour) {
        return new PersonBuilder(person).withRecurringLesson(new RecurringLesson(day, LocalTime.of(hour, 0),
                "Algebra", new LessonDuration("60"))).build();
    }

    private static NextLesson lessonOn(LocalDate date, int hour) {
        return new NextLesson(date, LocalTime.of(hour, 0), "Algebra", new LessonDuration("60"));
    }
}
