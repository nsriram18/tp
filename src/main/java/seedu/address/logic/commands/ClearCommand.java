package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "All students have been removed.";
    public static final String MESSAGE_NO_STUDENTS = "There are no students to remove in this window.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        boolean hasStudents = !model.getAddressBook().getPersonList().isEmpty();
        model.setAddressBook(new AddressBook());
        return new CommandResult(hasStudents ? MESSAGE_SUCCESS : MESSAGE_NO_STUDENTS);
    }
}
