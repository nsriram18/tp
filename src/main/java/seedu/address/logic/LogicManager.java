package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Clock;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.AddressBookChangedException;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String FILE_CHANGED_ERROR = "The data file changed in another app window. "
            + "Restart TutorTrack to load the latest students before making changes.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final IOException initialSnapshotError;
    private Optional<String> savedFileSnapshot;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, Clock.systemDefaultZone());
    }

    /**
     * Creates logic whose booking validation and upcoming selection share the supplied clock.
     */
    public LogicManager(Model model, Storage storage, Clock clock) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser(clock);
        IOException snapshotError = null;
        try {
            savedFileSnapshot = storage.readAddressBookFileSnapshot();
        } catch (IOException e) {
            savedFileSnapshot = Optional.empty();
            snapshotError = e;
        }
        initialSnapshotError = snapshotError;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        AddressBook previousAddressBook = new AddressBook(model.getAddressBook());
        Predicate<Person> previousFilter = model.getPersonListFilter();
        commandResult = command.execute(model);

        try {
            // A read-only command may run in a window with stale data. Saving it
            // would overwrite changes made by another window.
            if (!previousAddressBook.equals(model.getAddressBook())) {
                if (initialSnapshotError != null) {
                    throw initialSnapshotError;
                }
                savedFileSnapshot = Optional.of(storage.saveAddressBookIfUnchanged(
                        model.getAddressBook(), savedFileSnapshot));
            }
        } catch (AddressBookChangedException e) {
            restoreModel(previousAddressBook, previousFilter);
            throw new CommandException(FILE_CHANGED_ERROR, e);
        } catch (AccessDeniedException e) {
            restoreModel(previousAddressBook, previousFilter);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            restoreModel(previousAddressBook, previousFilter);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    /** Restores the pre-command records, order and filter after a failed save. */
    private void restoreModel(AddressBook previousAddressBook, Predicate<Person> previousFilter) {
        model.setAddressBook(previousAddressBook);
        model.updateFilteredPersonList(previousFilter);
        logger.warning("Saving failed; restored the student records and displayed filter.");
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
