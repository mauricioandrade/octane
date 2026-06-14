package com.octane.shared.config;

// Jackson date serialization is configured via application.yml:
// spring.jackson.serialization.write-dates-as-timestamps=false
// This ensures LocalDateTime fields are serialized as ISO-8601 strings.
public class JacksonConfig {}
