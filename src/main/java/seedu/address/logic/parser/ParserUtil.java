package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SchoolLevel;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INVALID_LESSON_TIME = "Lesson time must be a valid time in HH:mm format.";
    public static final String MESSAGE_INVALID_LESSON_DATE = "Lesson date must be a valid date in YYYY-MM-DD format.";

    private static final Pattern LESSON_DATE_FORMAT = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String schoolLevel} into a {@code SchoolLevel}.
     *
     * @throws ParseException if the given {@code schoolLevel} is invalid.
     */
    public static SchoolLevel parseSchoolLevel(String schoolLevel) throws ParseException {
        requireNonNull(schoolLevel);
        String trimmedSchoolLevel = schoolLevel.trim();
        if (!SchoolLevel.isValidSchoolLevel(trimmedSchoolLevel)) {
            throw new ParseException(SchoolLevel.MESSAGE_CONSTRAINTS);
        }
        return new SchoolLevel(trimmedSchoolLevel);
    }

    /**
     * Parses a {@code String subject} into a {@code Subject}.
     *
     * @throws ParseException if the given {@code subject} is invalid.
     */
    public static Subject parseSubject(String subject) throws ParseException {
        requireNonNull(subject);
        String trimmedSubject = subject.trim();
        if (!Subject.isValidSubject(trimmedSubject)) {
            throw new ParseException(Subject.MESSAGE_CONSTRAINTS);
        }
        return new Subject(trimmedSubject);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a lesson time into a {@code LocalTime} using the exact {@code HH:mm} format.
     * Whitespace is not trimmed; command parsers receive trimmed values from the argument tokenizer.
     *
     * @throws NullPointerException if {@code time} is null.
     * @throws ParseException if {@code time} has an invalid format or is outside the valid clock range.
     */
    public static LocalTime parseLessonTime(String time) throws ParseException {
        requireNonNull(time);
        if (!time.matches("\\d{2}:\\d{2}")) {
            throw new ParseException(MESSAGE_INVALID_LESSON_TIME);
        }
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_LESSON_TIME, e);
        }
    }

    /**
     * Returns the usage message for a lesson command with a missing required prefix. If a missing prefix
     * appears inside the terminal value, the message explains that the terminal prefix must come last.
     */
    public static String getLessonUsageMessage(ArgumentMultimap argMultimap, String usage, Prefix terminalPrefix,
            Prefix... requiredPrefixes) {
        String terminalValue = " " + argMultimap.getValue(terminalPrefix).orElse("");
        boolean isPrefixAfterTerminal = Arrays.stream(requiredPrefixes)
                .anyMatch(prefix -> argMultimap.getValue(prefix).isEmpty()
                        && terminalValue.contains(" " + prefix.getPrefix()));
        return isPrefixAfterTerminal ? Messages.MESSAGE_TOPIC_NOT_LAST + "\n" + usage : usage;
    }

    /**
     * Parses a lesson date into a {@code LocalDate} using the exact {@code YYYY-MM-DD} format.
     * Whitespace is not trimmed; command parsers receive trimmed values from the argument tokenizer.
     *
     * @throws NullPointerException if {@code date} is null.
     * @throws ParseException if {@code date} has an invalid format or is not a real calendar date.
     */
    public static LocalDate parseLessonDate(String date) throws ParseException {
        requireNonNull(date);
        if (!LESSON_DATE_FORMAT.matcher(date).matches()) {
            throw new ParseException(MESSAGE_INVALID_LESSON_DATE);
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_LESSON_DATE, e);
        }
    }

    /**
     * Parses a {@code String durationMinutes} into a {@code LessonDuration}.
     *
     * @throws ParseException if the given {@code durationMinutes} is invalid.
     */
    public static LessonDuration parseLessonDuration(String durationMinutes) throws ParseException {
        requireNonNull(durationMinutes);
        String trimmedDurationMinutes = durationMinutes.trim();
        if (!LessonDuration.isValidDuration(trimmedDurationMinutes)) {
            throw new ParseException(LessonDuration.MESSAGE_CONSTRAINTS);
        }
        return new LessonDuration(trimmedDurationMinutes);
    }
}
