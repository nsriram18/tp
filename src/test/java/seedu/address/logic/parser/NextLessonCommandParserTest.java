package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON_TIME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NextLessonCommand;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.NextLesson;

public class NextLessonCommandParserTest {

    private final NextLessonCommandParser parser = new NextLessonCommandParser();
    private final String futureDate = LocalDate.now().plusDays(1).toString();
    private final String laterFutureDate = LocalDate.now().plusDays(2).toString();

    @Test
    public void parse_validInput_success() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(14, 30);
        String topic = "Algebra and geometry";
        LessonDuration duration = new LessonDuration("90");

        assertParseSuccess(parser, "1 date/" + date + " time/14:30 dur/90 topic/" + topic,
                new NextLessonCommand(INDEX_FIRST_PERSON, new NextLesson(date, time, topic, duration)));
    }

    @Test
    public void parse_topicContainsCommandPrefixes_success() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(14, 30);
        String topic = "Compare date/ formats, time/ notation, dur/ values, and topic/ labels";
        LessonDuration duration = new LessonDuration("90");

        assertParseSuccess(parser, "1 date/" + date + " time/14:30 dur/90 topic/" + topic,
                new NextLessonCommand(INDEX_FIRST_PERSON, new NextLesson(date, time, topic, duration)));
    }

    @Test
    public void parse_missingRequiredPrefix_failure() {
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 dur/90",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextLessonCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 date/" + futureDate + " dur/90 topic/Algebra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextLessonCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 time/14:30 dur/90 topic/Algebra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextLessonCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 topic/Algebra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextLessonCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_topicBeforeRequiredPrefix_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                Messages.MESSAGE_TOPIC_NOT_LAST + "\n" + NextLessonCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 topic/Algebra dur/90", expectedMessage);
        assertParseFailure(parser, "1 topic/Algebra date/" + futureDate + " time/14:30 dur/90", expectedMessage);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "zero date/" + futureDate + " time/14:30 dur/90 topic/Algebra",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextLessonCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidDate_failure() {
        assertParseFailure(parser, "1 date/2026-02-30 time/14:30 dur/90 topic/Algebra",
                "Lesson date must be a valid date in YYYY-MM-DD format.");
        assertParseFailure(parser, "1 date/11-10-2026 time/14:30 dur/90 topic/Algebra",
                "Lesson date must be a valid date in YYYY-MM-DD format.");
    }

    @Test
    public void parse_invalidTime_failure() {
        assertParseFailure(parser, "1 date/" + futureDate + " time/2:30pm dur/90 topic/Algebra",
                "Lesson time must be a valid time in HH:mm format.");
        assertParseFailure(parser, "1 date/" + futureDate + " time/24:00 dur/90 topic/Algebra",
                "Lesson time must be a valid time in HH:mm format.");
    }

    @Test
    public void parse_pastLesson_failure() {
        assertParseFailure(parser, "1 date/" + LocalDate.now().minusDays(1)
                        + " time/14:30 dur/90 topic/Algebra",
                NextLesson.MESSAGE_DATE_TIME_CONSTRAINTS);
    }

    @Test
    public void parse_blankTopic_failure() {
        assertParseFailure(parser, "1 date/" + LocalDate.now().plusDays(1) + " time/14:30 dur/90 topic/ ",
                NextLesson.MESSAGE_TOPIC_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDuration_failure() {
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 dur/0 topic/Algebra",
                LessonDuration.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 dur/ninety topic/Algebra",
                LessonDuration.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nonPositiveOrNonNumericDuration_failure() {
        for (String duration : new String[] {"0", "-5", "abc"}) {
            assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 dur/" + duration + " topic/Algebra",
                    LessonDuration.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_durationAboveMaximum_failure() {
        int aboveMax = LessonDuration.MAX_DURATION_MINUTES + 1;
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 dur/" + aboveMax + " topic/Algebra",
                LessonDuration.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingOrNegativeIndex_failure() {
        String invalidFormat = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NextLessonCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "date/" + futureDate + " time/14:30 dur/90 topic/Algebra", invalidFormat);
        assertParseFailure(parser, "-1 date/" + futureDate + " time/14:30 dur/90 topic/Algebra", invalidFormat);
    }

    @Test
    public void parse_emptyDate_failure() {
        assertParseFailure(parser, "1 date/ time/14:30 dur/90 topic/Algebra",
                "Lesson date must be a valid date in YYYY-MM-DD format.");
    }

    @Test
    public void parse_emptyTime_failure() {
        assertParseFailure(parser, "1 date/" + futureDate + " time/ dur/90 topic/Algebra",
                "Lesson time must be a valid time in HH:mm format.");
    }

    @Test
    public void parse_emptyTopic_failure() {
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 dur/90 topic/",
                NextLesson.MESSAGE_TOPIC_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateTimePrefix_failure() {
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 time/15:30 dur/90 topic/Algebra",
                getErrorMessageForDuplicatePrefixes(PREFIX_LESSON_TIME));
    }

    @Test
    public void parse_duplicateTopicPrefix_treatedAsTopicText() {
        LocalDate date = LocalDate.now().plusDays(1);
        String topic = "Algebra topic/Geometry";
        assertParseSuccess(parser, "1 date/" + date + " time/14:30 dur/90 topic/" + topic,
                new NextLessonCommand(INDEX_FIRST_PERSON, new NextLesson(date, LocalTime.of(14, 30), topic,
                        new LessonDuration("90"))));
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        assertParseFailure(parser, "1 date/" + futureDate + " date/" + laterFutureDate
                        + " time/14:30 dur/90 topic/Algebra",
                getErrorMessageForDuplicatePrefixes(PREFIX_LESSON_DATE));
        assertParseFailure(parser, "1 date/" + futureDate + " time/14:30 dur/60 dur/90 topic/Algebra",
                getErrorMessageForDuplicatePrefixes(PREFIX_LESSON_DURATION));
    }
}
