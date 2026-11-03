package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents a student's next lesson.
 */
public class NextLesson {

    public static final int MAX_TOPIC_LENGTH = 200;
    public static final String MESSAGE_TOPIC_CONSTRAINTS = "Lesson topic must not be blank and can be at most "
            + MAX_TOPIC_LENGTH + " characters long.";
    public static final String MESSAGE_DATE_TIME_CONSTRAINTS = "Lesson date and time must not be in the past.";

    public final LocalDate date;
    public final LocalTime time;
    public final String topic;
    public final LessonDuration duration;

    /**
     * Constructs a {@code NextLesson}.
     *
     * @param date lesson date
     * @param time lesson time
     * @param topic lesson topic
     * @param duration lesson duration
     */
    public NextLesson(LocalDate date, LocalTime time, String topic, LessonDuration duration) {
        this(date, time, topic, duration, Clock.systemDefaultZone());
    }

    /**
     * Constructs a new booking, validating its start against the supplied clock.
     */
    public NextLesson(LocalDate date, LocalTime time, String topic, LessonDuration duration, Clock clock) {
        this(date, time, topic, duration, true, clock);
    }

    /**
     * Validates lesson details, additionally rejecting past starts for newly created bookings.
     */
    private NextLesson(LocalDate date, LocalTime time, String topic, LessonDuration duration,
            boolean isNewBooking, Clock clock) {
        requireNonNull(date);
        requireNonNull(time);
        requireNonNull(topic);
        requireNonNull(duration);
        if (isNewBooking && isDateTimeInPast(date, time, clock)) {
            throw new IllegalArgumentException(MESSAGE_DATE_TIME_CONSTRAINTS);
        }
        if (topic.isBlank() || topic.trim().codePointCount(0, topic.trim().length()) > MAX_TOPIC_LENGTH) {
            throw new IllegalArgumentException(MESSAGE_TOPIC_CONSTRAINTS);
        }
        this.date = date;
        this.time = time;
        this.topic = topic.trim();
        this.duration = duration;
    }

    /**
     * Restores a saved booking without applying the new-booking start-time restriction.
     * Storage remains responsible for discarding completed lessons.
     */
    public static NextLesson fromStoredValues(LocalDate date, LocalTime time, String topic, LessonDuration duration) {
        return new NextLesson(date, time, topic, duration, false, Clock.systemDefaultZone());
    }

    /**
     * Returns true if the given lesson date and time are before the current local date and time.
     */
    public static boolean isDateTimeInPast(LocalDate date, LocalTime time) {
        return isDateTimeInPast(date, time, Clock.systemDefaultZone());
    }

    /**
     * Returns whether the start is before the supplied clock's current minute.
     * Starts are entered in {@code HH:mm}, so a start in the current minute is not in the past.
     */
    public static boolean isDateTimeInPast(LocalDate date, LocalTime time, Clock clock) {
        requireNonNull(date);
        requireNonNull(time);
        requireNonNull(clock);
        return LocalDateTime.of(date, time).isBefore(LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES));
    }

    /**
     * Returns the date and time at which this lesson starts.
     */
    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, time);
    }

    /**
     * Returns the end of the occupied interval, including lessons that cross midnight.
     */
    public LocalDateTime getEndDateTime() {
        return getDateTime().plusMinutes(duration.getDurationMinutes());
    }

    @Override
    public String toString() {
        return date + " " + time + " " + duration + " " + topic;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof NextLesson otherNextLesson)) {
            return false;
        }
        return date.equals(otherNextLesson.date)
                && time.equals(otherNextLesson.time)
                && duration.equals(otherNextLesson.duration)
                && topic.equals(otherNextLesson.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, duration, topic);
    }
}
