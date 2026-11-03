package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_TOPIC;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NextLessonCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.NextLesson;

/**
 * Parses input arguments and creates a {@code NextLessonCommand} object.
 */
public class NextLessonCommandParser implements Parser<NextLessonCommand> {

    private final Clock clock;

    /**
     * Creates a parser using the computer's local time zone.
     */
    public NextLessonCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a parser that validates new bookings against the supplied clock.
     */
    public NextLessonCommandParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    @Override
    public NextLessonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithTerminalPrefix(args, PREFIX_LESSON_TOPIC,
                PREFIX_LESSON_DATE, PREFIX_LESSON_TIME, PREFIX_LESSON_DURATION);
        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_LESSON_DATE, PREFIX_LESSON_TIME,
                PREFIX_LESSON_DURATION, PREFIX_LESSON_TOPIC);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextLessonCommand.MESSAGE_USAGE);
            throw new ParseException(message, pe);
        }

        if (!arePrefixesPresent(argMultimap, PREFIX_LESSON_DATE, PREFIX_LESSON_TIME,
                PREFIX_LESSON_DURATION, PREFIX_LESSON_TOPIC)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    ParserUtil.getLessonUsageMessage(argMultimap, NextLessonCommand.MESSAGE_USAGE,
                            PREFIX_LESSON_TOPIC, PREFIX_LESSON_DATE, PREFIX_LESSON_TIME, PREFIX_LESSON_DURATION)));
        }

        LocalDate date = ParserUtil.parseLessonDate(argMultimap.getValue(PREFIX_LESSON_DATE).orElseThrow());
        LocalTime time = ParserUtil.parseLessonTime(argMultimap.getValue(PREFIX_LESSON_TIME).orElseThrow());
        LessonDuration duration = ParserUtil.parseLessonDuration(
                argMultimap.getValue(PREFIX_LESSON_DURATION).orElseThrow());
        try {
            return new NextLessonCommand(index, new NextLesson(date, time,
                    argMultimap.getValue(PREFIX_LESSON_TOPIC).orElseThrow(), duration, clock));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
