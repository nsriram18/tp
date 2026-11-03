package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.NextLesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecurringLesson;
import seedu.address.testutil.PersonBuilder;

public class RecurringLessonCommandTest {

    private static final LessonDuration DURATION = new LessonDuration("90");
    private static final RecurringLesson RECURRING_LESSON =
            new RecurringLesson(DayOfWeek.MONDAY, LocalTime.of(16, 0), "Algebra", DURATION);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_parsedCommand_ignoresOnlyCompletedOneOffLessons() throws Exception {
        LocalDate date = LocalDate.of(2099, 6, 15);
        NextLesson existing = NextLesson.fromStoredValues(date, LocalTime.of(14, 0), "Existing topic",
                new LessonDuration("60"));
        RecurringLesson proposed = new RecurringLesson(date.getDayOfWeek(), LocalTime.of(14, 30), "New topic",
                new LessonDuration("60"));
        for (LocalTime now : java.util.List.of(LocalTime.of(13, 59), LocalTime.of(14, 30),
                LocalTime.of(15, 0), LocalTime.of(15, 1))) {
            Model testModel = new ModelManager();
            Person target = new PersonBuilder().withName("Target Student").build();
            Person other = new PersonBuilder().withName("Other Student").withNextLesson(existing).build();
            testModel.addPerson(target);
            testModel.addPerson(other);
            Clock clock = Clock.fixed(date.atTime(now).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
            Command command = new AddressBookParser(clock).parseCommand("recurringlesson 1 day/"
                    + date.getDayOfWeek() + " time/14:30 dur/60 topic/New topic");
            if (now.isBefore(LocalTime.of(15, 0))) {
                assertCommandFailure(command, testModel, String.format(Messages.MESSAGE_LESSON_CLASH,
                        other.getName().fullName, date, existing.time, 60));
            } else {
                Model expectedModel = new ModelManager(testModel.getAddressBook(), new UserPrefs());
                expectedModel.setPerson(target, new PersonBuilder(target).withRecurringLesson(proposed).build());
                assertCommandSuccess(command, testModel, getExpectedSuccessMessage(target, proposed), expectedModel);
            }
        }
    }

    @Test
    public void execute_validIndex_setsRecurringLessonAndPreservesStudentDetails() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        NextLesson nextLesson = new NextLesson(
                LocalDate.now().plusDays(1), LocalTime.of(14, 30), "Quadratic equations", DURATION);
        Person studentWithDetails = new PersonBuilder(student)
                .withSubject("Biology")
                .withNote("Needs extra practice")
                .withNextLesson(nextLesson)
                .withTags("priority")
                .build();
        model.setPerson(student, studentWithDetails);

        Person updatedStudent = new PersonBuilder(studentWithDetails)
                .withRecurringLesson(RECURRING_LESSON)
                .build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(studentWithDetails, updatedStudent);

        assertCommandSuccess(new RecurringLessonCommand(INDEX_FIRST_PERSON, RECURRING_LESSON), model,
                getExpectedSuccessMessage(studentWithDetails, RECURRING_LESSON), expectedModel);
    }

    @Test
    public void execute_existingOverlappingRecurringLesson_replacesRecurringLesson() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        RecurringLesson oldLesson =
                new RecurringLesson(DayOfWeek.MONDAY, LocalTime.of(15, 30), "Geometry", DURATION);
        Person studentWithLesson = new PersonBuilder(student).withRecurringLesson(oldLesson).build();
        model.setPerson(student, studentWithLesson);

        Person updatedStudent = new PersonBuilder(studentWithLesson)
                .withRecurringLesson(RECURRING_LESSON)
                .build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(studentWithLesson, updatedStudent);

        assertCommandSuccess(new RecurringLessonCommand(INDEX_FIRST_PERSON, RECURRING_LESSON), model,
                getExpectedSuccessMessage(studentWithLesson, RECURRING_LESSON), expectedModel);
    }

    @Test
    public void execute_oneMinuteDuration_successMessageUsesSingularMinute() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        RecurringLesson lesson = new RecurringLesson(
                DayOfWeek.MONDAY, LocalTime.of(14, 0), "Minimum", new LessonDuration("1"));
        Person updatedStudent = new PersonBuilder(student).withRecurringLesson(lesson).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, updatedStudent);
        String expectedMessage = "Updated recurring lesson for " + student.getName().fullName
                + " to every MONDAY at 14:00 for 1 minute: Minimum";

        assertCommandSuccess(new RecurringLessonCommand(INDEX_FIRST_PERSON, lesson), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_multipleMinutesDuration_successMessageUsesPluralMinutes() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        RecurringLesson lesson = new RecurringLesson(
                DayOfWeek.MONDAY, LocalTime.of(14, 0), "Short lesson", new LessonDuration("2"));
        Person updatedStudent = new PersonBuilder(student).withRecurringLesson(lesson).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, updatedStudent);
        String expectedMessage = "Updated recurring lesson for " + student.getName().fullName
                + " to every MONDAY at 14:00 for 2 minutes: Short lesson";

        assertCommandSuccess(new RecurringLessonCommand(INDEX_FIRST_PERSON, lesson), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexInFilteredList_updatesCorrectStudentAndResetsFilter() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person student = model.getFilteredPersonList().get(0);
        Person updatedStudent = new PersonBuilder(student).withRecurringLesson(RECURRING_LESSON).build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, updatedStudent);

        assertCommandSuccess(new RecurringLessonCommand(INDEX_FIRST_PERSON, RECURRING_LESSON), model,
                getExpectedSuccessMessage(student, RECURRING_LESSON), expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new RecurringLessonCommand(invalidIndex, RECURRING_LESSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexInFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new RecurringLessonCommand(INDEX_SECOND_PERSON, RECURRING_LESSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RecurringLessonCommand command = new RecurringLessonCommand(INDEX_FIRST_PERSON, RECURRING_LESSON);

        assertTrue(command.equals(command));
        assertTrue(command.equals(new RecurringLessonCommand(INDEX_FIRST_PERSON, RECURRING_LESSON)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new RecurringLessonCommand(INDEX_SECOND_PERSON, RECURRING_LESSON)));
        assertFalse(command.equals(new RecurringLessonCommand(INDEX_FIRST_PERSON,
                new RecurringLesson(DayOfWeek.TUESDAY, LocalTime.of(16, 0), "Algebra", DURATION))));
        assertFalse(command.equals(new RecurringLessonCommand(INDEX_FIRST_PERSON,
                new RecurringLesson(DayOfWeek.MONDAY, LocalTime.of(17, 0), "Algebra", DURATION))));
        assertFalse(command.equals(new RecurringLessonCommand(INDEX_FIRST_PERSON,
                new RecurringLesson(DayOfWeek.MONDAY, LocalTime.of(16, 0), "Geometry", DURATION))));
    }

    private String getExpectedSuccessMessage(Person student, RecurringLesson lesson) {
        return String.format(RecurringLessonCommand.MESSAGE_RECURRING_LESSON_UPDATED, student.getName().fullName,
                lesson.getDay(), lesson.getTime(),
                Messages.formatLessonDuration(lesson.getDuration().getDurationMinutes()), lesson.getTopic());
    }
}
