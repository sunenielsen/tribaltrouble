module com.oddlabs.common {
    requires transitive org.joml;
    requires static org.jspecify;
    requires java.desktop;
    requires java.logging;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.databind;

    exports com.oddlabs.bugreport;
    exports com.oddlabs.event;
    exports com.oddlabs.geometry;
    exports com.oddlabs.http;
    exports com.oddlabs.matchmaking;
    exports com.oddlabs.net;
    exports com.oddlabs.procedural;
    exports com.oddlabs.registration;
    exports com.oddlabs.router;
    exports com.oddlabs.util;
}
