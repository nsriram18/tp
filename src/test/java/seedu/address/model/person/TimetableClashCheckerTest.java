package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TimetableClashCheckerTest {

    private static final LocalDate DATE = LocalDate.of(2099, 6, 15);
    private final Person target = new PersonBuilder().withName("Target Student").build();

    @Test
    public void findNextLessonClash_recurringAfterMidnight_ignoresLessonOnlyAtItsEnd() {
        NextLesson existing = NextLesson.fromStoredValues(DATE, LocalTime.of(23, 30), "Late topic",
                new LessonDuration("60"));
        Person other = new PersonBuilder().withName("Other Student").withNextLesson(existing).build();
        RecurringLesson proposed = new RecurringLesson(DATE.plusDays(1).getDayOfWeek(), LocalTime.MIDNIGHT,
                "New topic", new LessonDuration("60"));
        Clock ongoing = Clock.fixed(DATE.plusDays(1).atTime(0, 29).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        Clock ended = Clock.fixed(DATE.plusDays(1).atTime(0, 30).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        assertEquals(Optional.of(new LessonClash<>(other, existing)),
                TimetableClashChecker.findNextLessonClash(target, proposed, List.of(other), ongoing));
        assertTrue(TimetableClashChecker.findNextLessonClash(target, proposed, List.of(other), ended).isEmpty());
    }

    @Test
    public void findNextLessonClash_identicalAndPartialOverlaps_returnsConflictingStudent() {
        Person other = new PersonBuilder().withName("Other Student")
                .withNextLesson(lesson(DATE, 14, 0)).build();
        for (LocalTime time : List.of(LocalTime.of(13, 1), LocalTime.of(14, 0), LocalTime.of(14, 59))) {
            assertEquals(Optional.of(new LessonClash<>(other, other.getNextLesson().orElseThrow())),
                TimetableClashChecker.findNextLessonClash(target,
                    new NextLesson(DATE, time, "New topic", new LessonDuration("60")), List.of(target, other)));
        }
    }

    @Test
    public void findNextLessonClash_adjacentAndDifferentDateLessons_returnsEmpty() {
        Person other = new PersonBuilder().withName("Other Student")
                .withNextLesson(lesson(DATE, 14, 0)).build();
        for (NextLesson proposed : List.of(lesson(DATE, 13, 0), lesson(DATE, 15, 0),
                lesson(DATE.plusDays(1), 14, 0))) {
            assertTrue(TimetableClashChecker.findNextLessonClash(target, proposed, List.of(other)).isEmpty());
        }
    }

    @Test
    public void findNextLessonClash_crossMidnight_checksBothDirectionsAndAllowsBoundary() {
        Person lateStudent = new PersonBuilder().withName("Late Student")
                .withNextLesson(lesson(DATE, 23, 30)).build();
        assertEquals(Optional.of(new LessonClash<>(lateStudent, lateStudent.getNextLesson().orElseThrow())),
                TimetableClashChecker.findNextLessonClash(target,
                lesson(DATE.plusDays(1), 0, 15), List.of(lateStudent)));
        assertTrue(TimetableClashChecker.findNextLessonClash(target,
                lesson(DATE.plusDays(1), 0, 30), List.of(lateStudent)).isEmpty());

        Person earlyStudent = new PersonBuilder().withName("Early Student")
                .withNextLesson(lesson(DATE.plusDays(1), 0, 15)).build();
        assertEquals(Optional.of(new LessonClash<>(earlyStudent, earlyStudent.getNextLesson().orElseThrow())),
                TimetableClashChecker.findNextLessonClash(target,
                lesson(DATE, 23, 30), List.of(earlyStudent)));
    }

    @Test
    public void findNextLessonClash_targetOldLessonAndUnscheduledStudents_areIgnored() {
        Person oldTarget = new PersonBuilder(target).withNextLesson(lesson(DATE, 14, 0)).build();
        Person unscheduled = new PersonBuilder().withName("Unscheduled Student").build();
        assertTrue(TimetableClashChecker.findNextLessonClash(oldTarget, lesson(DATE, 14, 30),
                List.of(oldTarget, unscheduled)).isEmpty());
    }

    private NextLesson lesson(LocalDate date, int hour, int minute) {
        return new NextLesson(date, LocalTime.of(hour, minute), "Algebra", new LessonDuration("60"));
    }

    @Test
    public void findNextLessonClash_unequalDurations_checksEarlierLessonsActualEnd() {
        Person longLesson = new PersonBuilder().withName("Long Lesson Student")
                .withNextLesson(new NextLesson(DATE, LocalTime.of(14, 0), "Algebra", new LessonDuration("120")))
                .build();
        assertEquals(Optional.of(new LessonClash<>(longLesson, longLesson.getNextLesson().orElseThrow())),
                TimetableClashChecker.findNextLessonClash(target,
                    new NextLesson(DATE, LocalTime.of(15, 30), "Geometry", new LessonDuration("15")),
                List.of(longLesson)));
        assertTrue(TimetableClashChecker.findNextLessonClash(target,
                lesson(DATE, 16, 0), List.of(longLesson)).isEmpty());

        Person shortLesson = new PersonBuilder().withName("Short Lesson Student")
                .withNextLesson(new NextLesson(DATE, LocalTime.of(14, 0), "Algebra", new LessonDuration("15")))
                .build();
        assertTrue(TimetableClashChecker.findNextLessonClash(target,
                lesson(DATE, 14, 15), List.of(shortLesson)).isEmpty());
        assertEquals(Optional.of(new LessonClash<>(shortLesson, shortLesson.getNextLesson().orElseThrow())),
                TimetableClashChecker.findNextLessonClash(target,
                    new NextLesson(DATE, LocalTime.of(13, 0), "Geometry", new LessonDuration("120")),
                List.of(shortLesson)));
    }

    @Test
    public void findNextLessonClash_maxDurationLesson_checksUntilEndOnNextDay() {
        Person other = new PersonBuilder().withName("Multiday Student")
                .withNextLesson(new NextLesson(DATE, LocalTime.of(23, 30), "Workshop",
                        new LessonDuration(String.valueOf(LessonDuration.MAX_DURATION_MINUTES))))
                .build();
        assertEquals(Optional.of(new LessonClash<>(other, other.getNextLesson().orElseThrow())),
                TimetableClashChecker.findNextLessonClash(target,
                lesson(DATE.plusDays(1), 23, 15), List.of(other)));
        assertTrue(TimetableClashChecker.findNextLessonClash(target,
                lesson(DATE.plusDays(1), 23, 30), List.of(other)).isEmpty());
    }
}
