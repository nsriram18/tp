package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_TOPIC;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Locale;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RecurringLessonCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.RecurringLesson;

/**
 * Parses input arguments and creates a {@code RecurringLessonCommand} object.
 */
public class RecurringLessonCommandParser implements Parser<RecurringLessonCommand> {
    private static final String MESSAGE_INVALID_DAY = "Lesson day must be a valid day of the week, such as MONDAY.";
    private final Clock clock;

    /** Creates a parser using the computer's local time zone. */
    public RecurringLessonCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Shares the supplied clock with recurring-lesson commands. */
    public RecurringLessonCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public RecurringLessonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithTerminalPrefix(args, PREFIX_LESSON_TOPIC,
                PREFIX_LESSON_DAY, PREFIX_LESSON_TIME, PREFIX_LESSON_DURATION);
        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_LESSON_DAY, PREFIX_LESSON_TIME,
                PREFIX_LESSON_DURATION, PREFIX_LESSON_TOPIC);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecurringLessonCommand.MESSAGE_USAGE);
            throw new ParseException(message, pe);
        }

        if (argMultimap.getValue(PREFIX_LESSON_DAY).isEmpty()
                || argMultimap.getValue(PREFIX_LESSON_TIME).isEmpty()
                || argMultimap.getValue(PREFIX_LESSON_DURATION).isEmpty()
                || argMultimap.getValue(PREFIX_LESSON_TOPIC).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ParserUtil.getLessonUsageMessage(argMultimap, RecurringLessonCommand.MESSAGE_USAGE,
                            PREFIX_LESSON_TOPIC, PREFIX_LESSON_DAY, PREFIX_LESSON_TIME, PREFIX_LESSON_DURATION)));
        }

        DayOfWeek day = parseDay(argMultimap.getValue(PREFIX_LESSON_DAY).orElseThrow());
        LocalTime time = ParserUtil.parseLessonTime(argMultimap.getValue(PREFIX_LESSON_TIME).orElseThrow());
        LessonDuration duration = ParserUtil.parseLessonDuration(
                argMultimap.getValue(PREFIX_LESSON_DURATION).orElseThrow());

        try {
            return new RecurringLessonCommand(index, new RecurringLesson(day, time,
                    argMultimap.getValue(PREFIX_LESSON_TOPIC).orElseThrow(), duration), clock);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    private DayOfWeek parseDay(String day) throws ParseException {
        try {
            return DayOfWeek.valueOf(day.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_INVALID_DAY, e);
        }
    }
}
