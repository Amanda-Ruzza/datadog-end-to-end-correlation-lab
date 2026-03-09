package com.classicjazz.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User context for RUM/session tracking (e.g. Datadog RUM SDK).
 * Persisted to user_context.json when a user signs up, signs in, or completes checkout.
 */
public record UserContext(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("email") String email
) {
    /** Empty context when no user is signed in. */
    public static UserContext empty() {
        return new UserContext(null, null, null);
    }
}
