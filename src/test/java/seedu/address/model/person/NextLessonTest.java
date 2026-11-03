package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.Assert;

public class NextLessonTest {

    private static final LocalDateTime FUTURE_DATE_TIME = LocalDateTime.now().plusDays(1);
    private static final LocalDate VALID_DATE = FUTURE_DATE_TIME.toLocalDate();
    private static final LocalTime VALID_TIME = FUTURE_DATE_TIME.toLocalTime();
    private static final String VALID_TOPIC = "Algebra";
    private static final LessonDuration VALID_DURATION = new LessonDuration("60");

    @Test
    public void constructor_fixedClock_acceptsStartsAtOrAfterNow() {
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 10, 0);
        ZoneId zone = ZoneId.of("Asia/Singapore");
        for (LocalDateTime now : new LocalDateTime[]{start.minusNanos(1), start}) {
            Clock clock = Clock.fixed(now.atZone(zone).toInstant(), zone);
            NextLesson lesson = new NextLesson(start.toLocalDate(), start.toLocalTime(),
                    VALID_TOPIC, VALID_DURATION, clock);

            assertEquals(start, lesson.getDateTime());
            assertFalse(NextLesson.isDateTimeInPast(lesson.date, lesson.time, clock));
        }
    }

    @Test
    public void constructor_fixedClockLaterInStartMinute_acceptsBooking() {
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 10, 0);
        ZoneId zone = ZoneId.of("Asia/Singapore");
        for (LocalDateTime now : new LocalDateTime[]{start.plusNanos(1), start.plusSeconds(59)}) {
            Clock clock = Clock.fixed(now.atZone(zone).toInstant(), zone);

            assertFalse(NextLesson.isDateTimeInPast(start.toLocalDate(), start.toLocalTime(), clock));
            assertEquals(start, new NextLesson(start.toLocalDate(), start.toLocalTime(), VALID_TOPIC,
                    VALID_DURATION, clock).getDateTime());
        }
    }

    @Test
    public void constructor_fixedClockOneMinuteAfterStart_rejectsPastBooking() {
        LocalDateTime start = LocalDateTime.of(2000, 1, 1, 10, 0);
        ZoneId zone = ZoneId.of("Asia/Singapore");
        Clock clock = Clock.fixed(start.plusMinutes(1).atZone(zone).toInstant(), zone);

        assertTrue(NextLesson.isDateTimeInPast(start.toLocalDate(), start.toLocalTime(), clock));
        Assert.assertThrows(IllegalArgumentException.class, NextLesson.MESSAGE_DATE_TIME_CONSTRAINTS, () ->
                new NextLesson(start.toLocalDate(), start.toLocalTime(), VALID_TOPIC, VALID_DURATION, clock));
    }

    @Test
    public void constructor_nullClock_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () ->
                new NextLesson(VALID_DATE, VALID_TIME, VALID_TOPIC, VALID_DURATION, null));
    }

    @Test
    public void constructor_overlongTopic_throwsIllegalArgumentException() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new NextLesson(VALID_DATE, VALID_TIME,
                "t".repeat(NextLesson.MAX_TOPIC_LENGTH + 1), VALID_DURATION));
        assertEquals("t".repeat(NextLesson.MAX_TOPIC_LENGTH), new NextLesson(VALID_DATE, VALID_TIME,
                "t".repeat(NextLesson.MAX_TOPIC_LENGTH), VALID_DURATION).topic);
    }

    @Test
    public void constructor_pastDate_throwsIllegalArgumentException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        Assert.assertThrows(IllegalArgumentException.class, () -> new NextLesson(
                pastDate, LocalTime.MAX, VALID_TOPIC, VALID_DURATION));
    }

    @Test
    public void constructor_pastDateTime_throwsIllegalArgumentException() {
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(1);

        Assert.assertThrows(IllegalArgumentException.class, () -> new NextLesson(pastDateTime.toLocalDate(),
                pastDateTime.toLocalTime(), VALID_TOPIC, VALID_DURATION));
    }

    @Test
    public void fromStoredValues_ongoingLesson_preservesPastStartAndDuration() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);

        NextLesson lesson = NextLesson.fromStoredValues(start.toLocalDate(), start.toLocalTime(),
                VALID_TOPIC, VALID_DURATION);

        assertEquals(start, lesson.getDateTime());
        assertEquals(start.plusMinutes(60), lesson.getEndDateTime());
        Assert.assertThrows(IllegalArgumentException.class, NextLesson.MESSAGE_DATE_TIME_CONSTRAINTS, () ->
                new NextLesson(start.toLocalDate(), start.toLocalTime(), VALID_TOPIC, VALID_DURATION));
    }

    @Test
    public void fromStoredValues_crossesMidnight_computesEndOnNextDay() {
        LocalDate startDate = LocalDate.of(2020, 1, 1);

        NextLesson lesson = NextLesson.fromStoredValues(startDate, LocalTime.of(23, 30),
                VALID_TOPIC, VALID_DURATION);

        assertEquals(LocalDateTime.of(2020, 1, 2, 0, 30), lesson.getEndDateTime());
    }

    @Test
    public void fromStoredValues_blankTopic_isRejected() {
        Assert.assertThrows(IllegalArgumentException.class, NextLesson.MESSAGE_TOPIC_CONSTRAINTS, () ->
                NextLesson.fromStoredValues(VALID_DATE, VALID_TIME, " ", VALID_DURATION));
    }

    @Test
    public void constructor_futureDateTime_succeeds() {
        NextLesson lesson = new NextLesson(VALID_DATE, VALID_TIME, VALID_TOPIC, VALID_DURATION);

        assertEquals(VALID_DATE, lesson.date);
        assertEquals(VALID_TIME, lesson.time);
        assertEquals(VALID_DURATION, lesson.duration);
    }

    @Test
    public void constructor_nullDate_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> new NextLesson(
                null, VALID_TIME, VALID_TOPIC, VALID_DURATION));
    }

    @Test
    public void constructor_nullTime_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> new NextLesson(
                VALID_DATE, null, VALID_TOPIC, VALID_DURATION));
    }

    @Test
    public void constructor_nullTopic_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> new NextLesson(
                VALID_DATE, VALID_TIME, null, VALID_DURATION));
    }

    @Test
    public void constructor_nullDuration_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> new NextLesson(
                VALID_DATE, VALID_TIME, VALID_TOPIC, null));
    }

    @Test
    public void constructor_blankTopic_throwsIllegalArgumentException() {
        Assert.assertThrows(IllegalArgumentException.class, () -> new NextLesson(
                VALID_DATE, VALID_TIME, "  ", VALID_DURATION));
    }

    @Test
    public void constructor_validTopic_trimsWhitespace() {
        NextLesson lesson = new NextLesson(
                VALID_DATE, VALID_TIME, "  " + VALID_TOPIC + "  ", VALID_DURATION);

        assertEquals(VALID_TOPIC, lesson.topic);
    }

    @Test
    public void equals() {
        NextLesson lesson = new NextLesson(VALID_DATE, VALID_TIME, VALID_TOPIC, VALID_DURATION);

        assertTrue(lesson.equals(lesson));
        assertTrue(lesson.equals(new NextLesson(VALID_DATE, VALID_TIME, VALID_TOPIC, VALID_DURATION)));
        assertFalse(lesson.equals(new NextLesson(
                VALID_DATE.plusDays(1), VALID_TIME, VALID_TOPIC, VALID_DURATION)));
        assertFalse(lesson.equals(new NextLesson(
                VALID_DATE, VALID_TIME.plusMinutes(30), VALID_TOPIC, VALID_DURATION)));
        assertFalse(lesson.equals(new NextLesson(VALID_DATE, VALID_TIME, "Geometry", VALID_DURATION)));
        assertFalse(lesson.equals(new NextLesson(
                VALID_DATE, VALID_TIME, VALID_TOPIC, new LessonDuration("90"))));
        assertFalse(lesson.equals(null));
        assertFalse(lesson.equals(VALID_TOPIC));
    }
}
