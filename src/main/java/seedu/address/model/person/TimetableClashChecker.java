package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Checks the tutor's timetable using each lesson's duration.
 */
public final class TimetableClashChecker {

    private TimetableClashChecker() {}

    /**
     * Returns the first conflicting student and their next lesson.
     * The target student's old next lesson is excluded because it is being replaced.
     */
    public static Optional<LessonClash<NextLesson>> findNextLessonClash(Person target, NextLesson proposed,
            Iterable<Person> students) {
        requireNonNull(target);
        requireNonNull(proposed);
        requireNonNull(students);
        DatedLessonInterval proposedInterval = DatedLessonInterval.from(proposed);

        for (Person student : students) {
            if (student.isSamePerson(target) || student.getNextLesson().isEmpty()) {
                continue;
            }
            NextLesson existing = student.getNextLesson().orElseThrow();
            if (proposedInterval.overlaps(DatedLessonInterval.from(existing))) {
                return Optional.of(new LessonClash<>(student, existing));
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a next lesson conflicting with a proposed recurring schedule.
     * The same student's next lesson is allowed only when its weekday and start time match exactly.
     */
    public static Optional<LessonClash<NextLesson>> findNextLessonClash(Person target, RecurringLesson proposed,
            Iterable<Person> students) {
        return findNextLessonClash(target, proposed, students, Clock.systemDefaultZone());
    }

    /** Checks only one-off lessons that have not ended at the supplied clock's current time. */
    public static Optional<LessonClash<NextLesson>> findNextLessonClash(Person target, RecurringLesson proposed,
            Iterable<Person> students, Clock clock) {
        requireNonNull(target);
        requireNonNull(proposed);
        requireNonNull(students);
        LocalDateTime now = LocalDateTime.now(requireNonNull(clock));
        WeeklyLessonInterval proposedInterval = WeeklyLessonInterval.from(proposed);
        for (Person student : students) {
            if (student.getNextLesson().isPresent()) {
                NextLesson existing = student.getNextLesson().orElseThrow();
                if (!existing.getEndDateTime().isAfter(now)) {
                    continue;
                }
                if (!isOwnOccurrence(target, student, existing, proposed)
                        && proposedInterval.overlaps(DatedLessonInterval.from(existing).toWeeklyInterval())) {
                    return Optional.of(new LessonClash<>(student, existing));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a recurring schedule conflicting with a proposed next lesson, including the target's schedule.
     * An exact weekday/start-time match for the same student represents the same occurrence and is allowed.
     */
    public static Optional<LessonClash<RecurringLesson>> findRecurringLessonClash(Person target, NextLesson proposed,
            Iterable<Person> students) {
        requireNonNull(target);
        requireNonNull(proposed);
        requireNonNull(students);
        WeeklyLessonInterval proposedInterval = DatedLessonInterval.from(proposed).toWeeklyInterval();
        for (Person student : students) {
            if (student.getRecurringLesson().isPresent()) {
                RecurringLesson existing = student.getRecurringLesson().orElseThrow();
                if (!isOwnOccurrence(target, student, proposed, existing)
                        && proposedInterval.overlaps(WeeklyLessonInterval.from(existing))) {
                    return Optional.of(new LessonClash<>(student, existing));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds another student's recurring schedule that overlaps the proposal; excludes the rule being replaced.
     */
    public static Optional<LessonClash<RecurringLesson>> findRecurringLessonClash(Person target,
            RecurringLesson proposed, Iterable<Person> students) {
        requireNonNull(target);
        requireNonNull(proposed);
        requireNonNull(students);
        WeeklyLessonInterval proposedInterval = WeeklyLessonInterval.from(proposed);
        for (Person student : students) {
            if (!student.isSamePerson(target) && student.getRecurringLesson().isPresent()) {
                RecurringLesson existing = student.getRecurringLesson().orElseThrow();
                if (proposedInterval.overlaps(WeeklyLessonInterval.from(existing))) {
                    return Optional.of(new LessonClash<>(student, existing));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isOwnOccurrence(Person target, Person student, NextLesson next, RecurringLesson rule) {
        return student.isSamePerson(target) && next.date.getDayOfWeek() == rule.getDay()
                && next.time.equals(rule.getTime());
    }

}
