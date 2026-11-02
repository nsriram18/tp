package seedu.address.storage;

import java.io.IOException;

/**
 * Signals that the address-book file changed after this app window loaded it.
 */
public class AddressBookChangedException extends IOException {
    public AddressBookChangedException() {
        super("The address-book file has changed since it was loaded.");
    }
}
