package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.NextLesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.RecurringLesson;
import seedu.address.model.person.SchoolLevel;
import seedu.address.model.person.StudentNote;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Student's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String schoolLevel;
    private final String subject;
    private final String note;
    private final JsonAdaptedNextLesson nextLesson;
    private final JsonAdaptedRecurringLesson recurringLesson;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("schoolLevel") String schoolLevel, @JsonProperty("subject") String subject,
            @JsonProperty("note") String note,
            @JsonProperty("nextLesson") JsonAdaptedNextLesson nextLesson,
            @JsonProperty("recurringLesson") JsonAdaptedRecurringLesson recurringLesson,
            @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.schoolLevel = schoolLevel;
        this.subject = subject;
        this.note = note;
        this.nextLesson = nextLesson;
        this.recurringLesson = recurringLesson;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} without a school level for legacy test data.
     */
    JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, null, null, null, null, null, tags);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} without a note for legacy test data.
     */
    JsonAdaptedPerson(String name, String phone, String email, String address, String schoolLevel,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, schoolLevel, null, null, null, null, tags);
    }

    /**
     * Constructs a {@code JsonAdaptedPerson} without a subject for legacy test data.
     */
    JsonAdaptedPerson(String name, String phone, String email, String address, String schoolLevel, String note,
            JsonAdaptedNextLesson nextLesson, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, schoolLevel, null, note, nextLesson, null, tags);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        schoolLevel = source.getSchoolLevel().value;
        subject = source.getSubject().value;
        note = source.getNote().value;
        nextLesson = source.getNextLesson().map(JsonAdaptedNextLesson::new).orElse(null);
        recurringLesson = source.getRecurringLesson().map(JsonAdaptedRecurringLesson::new).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        return toModelType(Clock.systemDefaultZone());
    }

    /**
     * Restores a student, evaluating saved lessons against the supplied clock.
     */
    public Person toModelType(Clock clock) throws IllegalValueException {
        requireNonNull(clock);
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException("Student tags must not contain null entries.");
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final SchoolLevel modelSchoolLevel;
        if (schoolLevel == null) {
            modelSchoolLevel = new SchoolLevel(SchoolLevel.DEFAULT_VALUE);
        } else if (!SchoolLevel.isValidSchoolLevel(schoolLevel)) {
            throw new IllegalValueException(SchoolLevel.MESSAGE_CONSTRAINTS);
        } else {
            modelSchoolLevel = new SchoolLevel(schoolLevel);
        }

        final Subject modelSubject;
        if (subject == null) {
            modelSubject = new Subject(Subject.DEFAULT_VALUE);
        } else if (!Subject.isValidSubject(subject)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        } else {
            modelSubject = new Subject(subject);
        }

        if (note != null && !StudentNote.isValidNote(note)) {
            throw new IllegalValueException(StudentNote.MESSAGE_CONSTRAINTS);
        }
        StudentNote modelNote = new StudentNote(note == null ? "" : note);
        Optional<NextLesson> modelNextLesson = nextLesson == null ? Optional.empty() : nextLesson.toModelType(clock);
        Optional<RecurringLesson> modelRecurringLesson = recurringLesson == null
                ? Optional.empty()
                : Optional.of(recurringLesson.toModelType());

        final Set<Tag> modelTags = new HashSet<>(personTags);
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelSchoolLevel, modelSubject, modelNote,
                modelNextLesson, modelRecurringLesson, modelTags);
    }

}
