package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.time.Clock;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ClearNextLessonCommand;
import seedu.address.logic.commands.ClearRecurringLessonCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NextLessonCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.RecurringLessonCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UpcomingCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    private final Clock clock;

    /**
     * Creates a parser using the computer's local time zone.
     */
    public AddressBookParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Shares the supplied clock with time-dependent parsers and commands.
     */
    public AddressBookParser(Clock clock) {
        this.clock = requireNonNull(clock);
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case ClearNextLessonCommand.COMMAND_WORD -> new ClearNextLessonCommandParser().parse(arguments);
            case ClearRecurringLessonCommand.COMMAND_WORD ->
                new ClearRecurringLessonCommandParser().parse(arguments);
            case FilterCommand.COMMAND_WORD -> new FilterCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            case NoteCommand.COMMAND_WORD -> new NoteCommandParser().parse(arguments);
            case NextLessonCommand.COMMAND_WORD -> new NextLessonCommandParser(clock).parse(arguments);
            case SortCommand.COMMAND_WORD -> new SortCommand();
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case UpcomingCommand.COMMAND_WORD -> new UpcomingCommand(clock);
            case RecurringLessonCommand.COMMAND_WORD -> new RecurringLessonCommandParser(clock).parse(arguments);
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
