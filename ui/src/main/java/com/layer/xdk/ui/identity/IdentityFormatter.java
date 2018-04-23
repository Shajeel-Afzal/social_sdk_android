package com.layer.xdk.ui.identity;

import com.layer.sdk.messaging.Identity;

/**
 * Implement to provide appropriately formatted {@link String}s for an {@link Identity}
 *
 * @see DefaultIdentityFormatter for a sample implementation
 */
public interface IdentityFormatter {
    /**
     * Provide suitably formatted initials to display for the supplied {@link Identity}
     *
     * @param identity an {@link Identity} to get initials for
     * @return a {@link String} containing suitable initials for the supplied {@link Identity}
     */
    String getInitials(Identity identity);

    /**
     * Provide a suitably formatted first name for the supplied {@link Identity}
     *
     * @param identity an {@link Identity} to get the first name for
     * @return a {@link String} containing suitably formatted first name for the supplied
     * {@link Identity}
     */
    String getFirstName(Identity identity);

    /**
     * Provide a suitably formatted last name for the supplied {@link Identity}
     *
     * @param identity an {@link Identity} to get the last name for
     * @return a {@link String} containing suitably formatted last name for the supplied
     * {@link Identity}
     */
    String getLastName(Identity identity);

    /**
     * Provide a suitably formatted display name for the supplied {@link Identity}
     *
     * @param identity an {@link Identity} to get the display name for
     * @return a {@link String} containing suitably formatted display name for the supplied
     * {@link Identity}
     */
    String getDisplayName(Identity identity);

    /**
     * Provides secondary information, if any, that is present on the supplied {@link Identity}
     *
     * @param identity an {@link Identity} to get the secondary information from
     * @return a {@link String} containing the secondary information present on the supplied
     * {@link Identity}
     */
    String getSecondaryInfo(Identity identity);

    /**
     * Get a suitably formatted string to display for when an {@link Identity} is not available
     *
     * @return a {@link String} to display for when an {@link Identity} is not available
     */
    String getUnknownNameString();
}
