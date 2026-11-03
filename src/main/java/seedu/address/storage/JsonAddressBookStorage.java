package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private final Clock clock;
    private Optional<String> loadedFileSnapshot;

    /**
     * Creates storage using the computer's local time zone for saved lessons.
     */
    public JsonAddressBookStorage(Path filePath) {
        this(filePath, Clock.systemDefaultZone());
    }

    /**
     * Creates storage that evaluates saved lessons against the supplied clock.
     */
    public JsonAddressBookStorage(Path filePath, Clock clock) {
        this.filePath = filePath;
        this.clock = requireNonNull(clock);
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);
        try {
            Optional<String> fileContents = readCurrentFileSnapshot(filePath);
            if (filePath.toAbsolutePath().equals(this.filePath.toAbsolutePath())) {
                loadedFileSnapshot = fileContents;
            }
            if (fileContents.isEmpty()) {
                return Optional.empty();
            }
            JsonSerializableAddressBook jsonAddressBook = JsonUtil.fromJsonString(
                    fileContents.orElseThrow(), JsonSerializableAddressBook.class);
            if (jsonAddressBook == null) {
                throw new IOException("JSON file must contain a non-null value: " + filePath);
            }
            return Optional.of(jsonAddressBook.toModelType(clock));
        } catch (IOException e) {
            logger.warning("Error reading from jsonFile file " + filePath + ": " + e);
            throw new DataLoadingException(e);
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /** Returns the exact contents used for loading, or reads the file if loading has not happened yet. */
    public Optional<String> readAddressBookFileSnapshot() throws IOException {
        return loadedFileSnapshot == null ? readCurrentFileSnapshot(filePath) : loadedFileSnapshot;
    }

    private Optional<String> readCurrentFileSnapshot(Path path) throws IOException {
        try {
            return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
        } catch (NoSuchFileException exception) {
            return Optional.empty();
        }
    }

    /**
     * Checks and saves under a file lock, preventing another app instance from writing between those steps.
     * The lock file is retained so all instances continue to lock the same file.
     */
    public String saveAddressBookIfUnchanged(ReadOnlyAddressBook addressBook, Optional<String> expectedSnapshot)
            throws IOException {
        requireNonNull(addressBook);
        requireNonNull(expectedSnapshot);
        Path absoluteFilePath = filePath.toAbsolutePath();
        Path lockPath = absoluteFilePath.resolveSibling(absoluteFilePath.getFileName() + ".lock");
        Files.createDirectories(lockPath.getParent());
        synchronized (JsonAddressBookStorage.class) {
            try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock ignored = channel.lock()) {
                if (!readCurrentFileSnapshot(filePath).equals(expectedSnapshot)) {
                    throw new AddressBookChangedException();
                }
                String savedContents = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
                saveAddressBook(addressBook);
                return savedContents;
            }
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), filePath);
    }

}
