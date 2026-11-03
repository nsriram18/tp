package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_LESSON_CLASH = "Cannot schedule lesson: conflicts with %1$s's next lesson "
            + "on %2$s at %3$s for %4$d minutes.";
    public static final String MESSAGE_RECURRING_LESSON_CLASH =
            "Cannot schedule lesson: conflicts with %1$s's recurring lesson every %2$s at %3$s for %4$d minutes.";

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_TOPIC_NOT_LAST = "topic/TOPIC must be the last parameter. "
            + "Move the other parameters before topic/.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The student index provided is invalid.";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Formats a lesson duration with the correct singular or plural minute unit.
     */
    public static String formatLessonDuration(int durationMinutes) {
        return durationMinutes + (durationMinutes == 1 ? " minute" : " minutes");
    }

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an overview message for the number of students listed.
     */
    public static String getStudentsListedOverview(int studentCount) {
        String studentLabel = studentCount == 1 ? "student" : "students";
        return String.format("%1$d %2$s listed!", studentCount, studentLabel);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; School level: ")
                .append(person.getSchoolLevel())
                .append("; Subject: ")
                .append(person.getSubject())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        return builder.toString();
    }

}
