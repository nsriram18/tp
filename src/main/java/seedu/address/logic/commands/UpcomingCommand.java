package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.NextLesson;
import seedu.address.model.person.Person;

/**
 * Shows the student(s) with the earliest upcoming lesson.
 */
public class UpcomingCommand extends Command {

    public static final String COMMAND_WORD = "upcoming";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows the student(s) with the earliest "
            + "upcoming lesson.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Showing student(s) with the earliest upcoming lesson at %1$s.";
    public static final String MESSAGE_NO_UPCOMING_LESSON = "No upcoming lessons are scheduled.";

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Clock clock;

    /**
     * Creates a command using the computer's local time zone.
     */
    public UpcomingCommand() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a command whose upcoming-lesson boundary uses the supplied clock.
     */
    public UpcomingCommand(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime earliest = model.getAddressBook().getPersonList().stream()
                .map(person -> getEarliestStart(person, now))
                .flatMap(Optional::stream)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new CommandException(MESSAGE_NO_UPCOMING_LESSON));

        Predicate<Person> startsAtEarliest = person -> getEarliestStart(person, now)
                .filter(earliest::equals)
                .isPresent();
        model.updateFilteredPersonList(startsAtEarliest);
        return new CommandResult(String.format(MESSAGE_SUCCESS, earliest.format(DATE_TIME_FORMAT)));
    }

    /** Compares a student's one-off lesson with their next weekly occurrence. */
    private Optional<LocalDateTime> getEarliestStart(Person person, LocalDateTime now) {
        Optional<LocalDateTime> nextLesson = person.getNextLesson()
                .map(NextLesson::getDateTime)
                .filter(start -> !start.isBefore(now));
        Optional<LocalDateTime> recurringLesson = person.getRecurringLesson()
                .map(lesson -> lesson.getNextOccurrence(now));
        return Stream.concat(nextLesson.stream(), recurringLesson.stream()).min(LocalDateTime::compareTo);
    }

}
