package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DAY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_TIME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.RecurringLessonCommand;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.RecurringLesson;

public class RecurringLessonCommandParserTest {

    private static final String VALID_TIME = "16:30";
    private static final String VALID_TOPIC = "Algebra and geometry";
    private static final String VALID_DURATION = "90";
    private static final String MESSAGE_INVALID_DAY =
            "Lesson day must be a valid day of the week, such as MONDAY.";
    private static final String MESSAGE_INVALID_TIME = "Lesson time must be a valid time in HH:mm format.";

    private final RecurringLessonCommandParser parser = new RecurringLessonCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        assertParseSuccess(parser, "1 day/MONDAY time/" + VALID_TIME
                        + " dur/" + VALID_DURATION + " topic/" + VALID_TOPIC,
                getExpectedCommand(DayOfWeek.MONDAY));
    }

    @Test
    public void parse_topicContainsCommandPrefixes_success() {
        String topic = "Compare day/ values, time/ notation, dur/ values, and topic/ labels";

        assertParseSuccess(parser, "1 day/MONDAY time/" + VALID_TIME
                        + " dur/" + VALID_DURATION + " topic/" + topic,
                new RecurringLessonCommand(INDEX_FIRST_PERSON,
                        new RecurringLesson(DayOfWeek.MONDAY, LocalTime.of(16, 30), topic,
                                new LessonDuration(VALID_DURATION))));
    }

    @Test
    public void parse_lowercaseDay_success() {
        assertParseSuccess(parser, "1 day/monday time/" + VALID_TIME
                        + " dur/" + VALID_DURATION + " topic/" + VALID_TOPIC,
                getExpectedCommand(DayOfWeek.MONDAY));
    }

    @Test
    public void parse_mixedCaseDay_success() {
        assertParseSuccess(parser, "1 day/Monday time/" + VALID_TIME
                        + " dur/" + VALID_DURATION + " topic/" + VALID_TOPIC,
                getExpectedCommand(DayOfWeek.MONDAY));
    }

    @Test
    public void parse_missingRequiredPrefix_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecurringLessonCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "1 time/16:30 dur/90 topic/Algebra", expectedMessage);
        assertParseFailure(parser, "1 day/MONDAY dur/90 topic/Algebra", expectedMessage);
        assertParseFailure(parser, "1 day/MONDAY time/16:30 dur/90", expectedMessage);
        assertParseFailure(parser, "1 day/MONDAY time/16:30 topic/Algebra", expectedMessage);
    }

    @Test
    public void parse_topicBeforeRequiredPrefix_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                Messages.MESSAGE_TOPIC_NOT_LAST + "\n" + RecurringLessonCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "1 day/MONDAY time/16:30 topic/Algebra dur/90", expectedMessage);
        assertParseFailure(parser, "1 topic/Algebra day/MONDAY time/16:30 dur/90", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "zero day/MONDAY time/16:30 dur/90 topic/Algebra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecurringLessonCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingIndex_failure() {
        assertParseFailure(parser, "day/MONDAY time/16:30 dur/90 topic/Algebra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, RecurringLessonCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidDay_failure() {
        assertParseFailure(parser, "1 day/FUNDAY time/16:30 dur/90 topic/Algebra", MESSAGE_INVALID_DAY);
    }

    @Test
    public void parse_invalidTime_failure() {
        assertParseFailure(parser, "1 day/MONDAY time/4pm dur/90 topic/Algebra", MESSAGE_INVALID_TIME);
        assertParseFailure(parser, "1 day/MONDAY time/24:00 dur/90 topic/Algebra", MESSAGE_INVALID_TIME);
    }

    @Test
    public void parse_blankTopic_failure() {
        assertParseFailure(parser, "1 day/MONDAY time/16:30 dur/90 topic/ ",
                RecurringLesson.MESSAGE_TOPIC_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDuration_failure() {
        assertParseFailure(parser, "1 day/MONDAY time/16:30 dur/0 topic/Algebra",
                LessonDuration.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 day/MONDAY time/16:30 dur/ninety topic/Algebra",
                LessonDuration.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        assertParseFailure(parser, "1 day/MONDAY day/TUESDAY time/16:30 dur/90 topic/Algebra",
                getErrorMessageForDuplicatePrefixes(PREFIX_LESSON_DAY));
        assertParseFailure(parser, "1 day/MONDAY time/16:30 time/17:30 dur/90 topic/Algebra",
                getErrorMessageForDuplicatePrefixes(PREFIX_LESSON_TIME));
        assertParseFailure(parser, "1 day/MONDAY time/16:30 dur/60 dur/90 topic/Algebra",
                getErrorMessageForDuplicatePrefixes(PREFIX_LESSON_DURATION));
    }

    private RecurringLessonCommand getExpectedCommand(DayOfWeek day) {
        return new RecurringLessonCommand(INDEX_FIRST_PERSON,
                new RecurringLesson(day, LocalTime.of(16, 30), VALID_TOPIC,
                        new LessonDuration(VALID_DURATION)));
    }
}
