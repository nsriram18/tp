package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.LessonClash;
import seedu.address.model.person.NextLesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.RecurringLesson;
import seedu.address.model.person.TimetableClashChecker;

/**
 * Adds or replaces the recurring weekly lesson for a student.
 */
public class RecurringLessonCommand extends Command {
    public static final String COMMAND_WORD = "recurringlesson";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sets or replaces the recurring weekly lesson for "
            + "the student at INDEX.\n"
            + "Parameters: INDEX (must be a positive integer) day/DAY time/HH:mm dur/MINUTES topic/TOPIC\n"
            + "DAY must be a day of the week, such as MONDAY.\n"
            + "Example: " + COMMAND_WORD + " 1 day/MONDAY time/16:30 dur/90 topic/Algebra";
    public static final String MESSAGE_RECURRING_LESSON_UPDATED =
            "Updated recurring lesson for %1$s to every %2$s at %3$s for %4$s: %5$s";

    private static final Logger logger = LogsCenter.getLogger(RecurringLessonCommand.class);

    private final Index index;
    private final RecurringLesson recurringLesson;
    private final Clock clock;

    /**
     * Creates a command to set the recurring lesson for the student at {@code index}.
     *
     * @param index displayed index of the student to update
     * @param recurringLesson new recurring lesson
     */
    public RecurringLessonCommand(Index index, RecurringLesson recurringLesson) {
        this(index, recurringLesson, Clock.systemDefaultZone());
    }

    /** Creates a command using the supplied clock to ignore completed one-off lessons at execution time. */
    public RecurringLessonCommand(Index index, RecurringLesson recurringLesson, Clock clock) {
        requireNonNull(index);
        requireNonNull(recurringLesson);
        this.index = index;
        this.recurringLesson = recurringLesson;
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = getDisplayedStudent(model, index);
        List<Person> allStudents = model.getAddressBook().getPersonList();
        validateNoNextLessonClash(student, allStudents);
        validateNoRecurringLessonClash(student, allStudents);

        Person updatedStudent = student.withRecurringLesson(Optional.of(recurringLesson));
        model.setPerson(student, updatedStudent);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(String.format(MESSAGE_RECURRING_LESSON_UPDATED, student.getName().fullName,
                recurringLesson.getDay(), recurringLesson.getTime(),
                Messages.formatLessonDuration(recurringLesson.getDuration().getDurationMinutes()),
                recurringLesson.getTopic()));
    }


    /** Rejects a next-lesson clash unless it is an exact occurrence of this student's proposed schedule. */
    private void validateNoNextLessonClash(Person student, List<Person> allStudents) throws CommandException {
        Optional<LessonClash<NextLesson>> nextConflict = TimetableClashChecker.findNextLessonClash(student,
                recurringLesson, allStudents, clock);
        if (nextConflict.isPresent()) {
            LessonClash<NextLesson> clash = nextConflict.orElseThrow();
            NextLesson lesson = clash.lesson();
            logger.info("Rejected recurring lesson: next-lesson overlap detected; student data unchanged.");
            throw new CommandException(String.format(Messages.MESSAGE_LESSON_CLASH,
                    clash.student().getName().fullName, lesson.date, lesson.time,
                    lesson.duration.getDurationMinutes()));
        }
    }

    /** Rejects a clash with any student's recurring lesson, excluding the schedule being replaced. */
    private void validateNoRecurringLessonClash(Person student, List<Person> allStudents) throws CommandException {
        Optional<LessonClash<RecurringLesson>> recurringConflict = TimetableClashChecker.findRecurringLessonClash(
                student, recurringLesson, allStudents);
        if (recurringConflict.isPresent()) {
            LessonClash<RecurringLesson> clash = recurringConflict.orElseThrow();
            RecurringLesson lesson = clash.lesson();
            logger.info("Rejected recurring lesson: recurring timetable overlap detected; student data unchanged.");
            throw new CommandException(String.format(Messages.MESSAGE_RECURRING_LESSON_CLASH,
                    clash.student().getName().fullName, lesson.getDay(), lesson.getTime(),
                    lesson.getDuration().getDurationMinutes()));
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RecurringLessonCommand otherRecurringLessonCommand)) {
            return false;
        }
        return index.equals(otherRecurringLessonCommand.index)
                && recurringLesson.equals(otherRecurringLessonCommand.recurringLesson);
    }
}
