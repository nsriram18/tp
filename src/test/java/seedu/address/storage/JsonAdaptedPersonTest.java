package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.LessonUtil.futureLesson;
import static seedu.address.testutil.LessonUtil.withLesson;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.LessonDuration;
import seedu.address.model.person.Name;
import seedu.address.model.person.NextLesson;
import seedu.address.model.person.Phone;
import seedu.address.model.person.RecurringLesson;
import seedu.address.model.person.SchoolLevel;
import seedu.address.model.person.StudentNote;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_SCHOOL_LEVEL = "Secondary 4";
    private static final String VALID_SUBJECT = "Mathematics";
    private static final String INVALID_SUBJECT = " ";
    private static final String INVALID_SCHOOL_LEVEL = " ";
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_nullTagName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, List.of(new JsonAdaptedTag((String) null)));
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_validSchoolLevel_returnsPersonWithSchoolLevel() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, VALID_TAGS);

        assertEquals(VALID_SCHOOL_LEVEL, person.toModelType().getSchoolLevel().value);
    }

    @Test
    public void toModelType_missingSchoolLevel_usesDefaultSchoolLevel() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_TAGS);

        assertEquals(SchoolLevel.DEFAULT_VALUE, person.toModelType().getSchoolLevel().value);
    }

    @Test
    public void toModelType_subjectFromPerson_roundTripsSubject() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(new PersonBuilder(BENSON).withSubject(VALID_SUBJECT).build());

        assertEquals(new Subject(VALID_SUBJECT), adapted.toModelType().getSubject());
    }

    @Test
    public void toModelType_missingSubject_usesDefaultSubject() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, VALID_TAGS);

        assertEquals(new Subject(Subject.DEFAULT_VALUE), adapted.toModelType().getSubject());
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, INVALID_SUBJECT, null, null, null, VALID_TAGS);

        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, adapted::toModelType);
    }

    @Test
    public void toModelType_noteIsPreserved() throws Exception {
        String note = "Needs extra time for algebra";
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(new PersonBuilder(BENSON).withNote(note).build());

        assertEquals(new StudentNote(note), adapted.toModelType().getNote());
    }

    @Test
    public void toModelType_missingNote_usesEmptyNote() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, VALID_TAGS);

        assertEquals(new StudentNote(""), adapted.toModelType().getNote());
    }

    @Test
    public void toModelType_nextLessonIsPreserved() throws Exception {
        NextLesson lesson = futureLesson(1, 14, 30, 90);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(withLesson(BENSON, lesson));
        String json = JsonUtil.toJsonString(adapted);
        JsonAdaptedPerson reloaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertEquals(Optional.of(lesson), reloaded.toModelType().getNextLesson());
    }

    @Test
    public void toModelType_missingNextLesson_usesEmptyLesson() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, VALID_TAGS);

        assertEquals(Optional.empty(), adapted.toModelType().getNextLesson());
    }

    @Test
    public void toModelType_personWithoutNextLesson_roundTripsAsEmpty() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(new PersonBuilder(BENSON).withoutNextLesson().build());
        String json = JsonUtil.toJsonString(adapted);
        JsonAdaptedPerson reloaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertEquals(Optional.empty(), reloaded.toModelType().getNextLesson());
    }

    @Test
    public void toModelType_expiredNextLesson_clearsLesson() throws Exception {
        JsonAdaptedNextLesson expiredLesson = new JsonAdaptedNextLesson(
                LocalDate.now().minusDays(1).toString(), "14:30", "Algebra", 90);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, null, expiredLesson, VALID_TAGS);

        assertEquals(Optional.empty(), adapted.toModelType().getNextLesson());
    }

    @Test
    public void toModelType_ongoingNextLesson_keepsOccupiedInterval() throws Exception {
        LocalDateTime started = LocalDateTime.now().minusMinutes(10);
        JsonAdaptedNextLesson ongoing = new JsonAdaptedNextLesson(started.toLocalDate().toString(),
                started.toLocalTime().toString(), "Algebra", 60);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, null, ongoing, VALID_TAGS);

        NextLesson lesson = adapted.toModelType().getNextLesson().orElseThrow();

        assertEquals(started, lesson.getDateTime());
        assertEquals(started.plusMinutes(60), lesson.getEndDateTime());
    }

    @Test
    public void toModelType_nextLessonEndBoundary_keepsBeforeEndAndDiscardsAtEnd() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030, 6, 17, 9, 0);
        LocalDateTime end = start.plusMinutes(60);
        JsonAdaptedNextLesson stored = new JsonAdaptedNextLesson(
                start.toLocalDate().toString(), start.toLocalTime().toString(), "Algebra", 60);
        NextLesson expected = NextLesson.fromStoredValues(start.toLocalDate(), start.toLocalTime(),
                "Algebra", new LessonDuration("60"));
        Clock justBeforeEnd = Clock.fixed(end.minusNanos(1).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        Clock exactlyAtEnd = Clock.fixed(end.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        assertEquals(Optional.of(expected), stored.toModelType(justBeforeEnd));
        assertEquals(Optional.empty(), stored.toModelType(exactlyAtEnd));
    }

    @Test
    public void toModelType_fixedClockDuringLesson_keepsLesson() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030, 6, 17, 9, 0);
        JsonAdaptedNextLesson stored = new JsonAdaptedNextLesson(
                start.toLocalDate().toString(), start.toLocalTime().toString(), "Algebra", 60);
        Clock tenMinutesIn = Clock.fixed(start.plusMinutes(10).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        NextLesson lesson = stored.toModelType(tenMinutesIn).orElseThrow();

        assertEquals(start, lesson.getDateTime());
        assertEquals(start.plusMinutes(60), lesson.getEndDateTime());
    }

    @Test
    public void toModelType_completedTodayNextLesson_clearsLesson() throws Exception {
        LocalDateTime started = LocalDateTime.now().minusMinutes(90);
        JsonAdaptedNextLesson completed = new JsonAdaptedNextLesson(started.toLocalDate().toString(),
                started.toLocalTime().toString(), "Algebra", 60);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, null, completed, VALID_TAGS);

        assertEquals(Optional.empty(), adapted.toModelType().getNextLesson());
    }

    @Test
    public void toModelType_invalidNextLessonDate_throwsIllegalValueException() {
        JsonAdaptedNextLesson invalidLesson = new JsonAdaptedNextLesson("tomorrow", "14:30", "Algebra", 90);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, null, invalidLesson, VALID_TAGS);

        assertThrows(IllegalValueException.class, adapted::toModelType);
    }

    @Test
    public void toModelType_blankNextLessonTopic_throwsIllegalValueException() {
        JsonAdaptedNextLesson invalidLesson = new JsonAdaptedNextLesson(
                LocalDate.now().plusDays(1).toString(), "14:30", "  ", 90);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, null, invalidLesson, VALID_TAGS);

        assertThrows(IllegalValueException.class, NextLesson.MESSAGE_TOPIC_CONSTRAINTS, adapted::toModelType);
    }

    @Test
    public void toModelType_missingNextLessonDuration_usesDefaultDuration() throws Exception {
        JsonAdaptedNextLesson legacyLesson = new JsonAdaptedNextLesson(
                LocalDate.now().plusDays(1).toString(), "14:30", "Algebra", null);

        NextLesson lesson = legacyLesson.toModelType().orElseThrow();

        assertEquals(LessonDuration.DEFAULT_DURATION_MINUTES, lesson.duration.getDurationMinutes());
    }

    @Test
    public void toModelType_legacyNextLessonDurationAboveLimit_capsDuration() throws Exception {
        JsonAdaptedNextLesson legacyLesson = new JsonAdaptedNextLesson(
                LocalDate.now().plusDays(1).toString(), "14:30", "Algebra", 1441);

        NextLesson lesson = legacyLesson.toModelType().orElseThrow();

        assertEquals(1440, lesson.duration.getDurationMinutes());
    }

    @Test
    public void toModelType_nonPositiveNextLessonDuration_throwsIllegalValueException() {
        for (int duration : new int[] {0, -5}) {
            JsonAdaptedNextLesson invalidLesson = new JsonAdaptedNextLesson(
                    LocalDate.now().plusDays(1).toString(), "14:30", "Algebra", duration);

            assertThrows(IllegalValueException.class, LessonDuration.MESSAGE_CONSTRAINTS,
                    invalidLesson::toModelType);
        }
    }

    @Test
    public void toModelType_invalidSchoolLevel_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                INVALID_SCHOOL_LEVEL, VALID_TAGS);

        assertThrows(IllegalValueException.class, SchoolLevel.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_recurringLessonIsPreserved() throws Exception {
        RecurringLesson lesson = new RecurringLesson(DayOfWeek.MONDAY, LocalTime.of(14, 30), "Mathematics",
                new LessonDuration("90"));
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(
                new PersonBuilder(BENSON).withRecurringLesson(lesson).build());

        String json = JsonUtil.toJsonString(adapted);
        JsonAdaptedPerson reloaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertEquals(Optional.of(lesson), reloaded.toModelType().getRecurringLesson());
    }

    @Test
    public void toModelType_missingRecurringLesson_usesEmptyLesson() throws Exception {
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(new PersonBuilder(BENSON).build());

        String json = JsonUtil.toJsonString(adapted);
        JsonAdaptedPerson reloaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertEquals(Optional.empty(), reloaded.toModelType().getRecurringLesson());
    }

    @Test
    public void toModelType_invalidRecurringLessonDay_throwsIllegalValueException() {
        JsonAdaptedRecurringLesson invalidLesson =
                new JsonAdaptedRecurringLesson("FUNDAY", "14:30", "Mathematics", 90);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, VALID_SUBJECT, null, null, invalidLesson, VALID_TAGS);

        assertThrows(IllegalValueException.class, adapted::toModelType);
    }

    @Test
    public void toModelType_invalidRecurringLessonTime_throwsIllegalValueException() {
        JsonAdaptedRecurringLesson invalidLesson =
                new JsonAdaptedRecurringLesson("MONDAY", "25:30", "Mathematics", 90);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, VALID_SUBJECT, null, null, invalidLesson, VALID_TAGS);

        assertThrows(IllegalValueException.class, adapted::toModelType);
    }

    @Test
    public void toModelType_blankRecurringLessonTopic_throwsIllegalValueException() {
        JsonAdaptedRecurringLesson invalidLesson =
                new JsonAdaptedRecurringLesson("MONDAY", "14:30", "   ", 90);
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_SCHOOL_LEVEL, VALID_SUBJECT, null, null, invalidLesson, VALID_TAGS);

        assertThrows(IllegalValueException.class, RecurringLesson.MESSAGE_TOPIC_CONSTRAINTS, adapted::toModelType);
    }

    @Test
    public void toModelType_missingRecurringLessonDuration_usesDefaultDuration() throws Exception {
        JsonAdaptedRecurringLesson legacyLesson =
                new JsonAdaptedRecurringLesson("MONDAY", "14:30", "Mathematics", null);

        RecurringLesson lesson = legacyLesson.toModelType();

        assertEquals(LessonDuration.DEFAULT_DURATION_MINUTES,
                lesson.getDuration().getDurationMinutes());
    }

    @Test
    public void toModelType_legacyRecurringLessonDurationAboveLimit_capsDuration() throws Exception {
        JsonAdaptedRecurringLesson legacyLesson =
                new JsonAdaptedRecurringLesson("MONDAY", "14:30", "Mathematics", 1441);

        RecurringLesson lesson = legacyLesson.toModelType();

        assertEquals(1440, lesson.getDuration().getDurationMinutes());
    }

    @Test
    public void toModelType_invalidRecurringLessonDuration_throwsIllegalValueException() {
        JsonAdaptedRecurringLesson invalidLesson =
                new JsonAdaptedRecurringLesson("MONDAY", "14:30", "Mathematics", 0);

        assertThrows(IllegalValueException.class, LessonDuration.MESSAGE_CONSTRAINTS,
                invalidLesson::toModelType);
    }
}
