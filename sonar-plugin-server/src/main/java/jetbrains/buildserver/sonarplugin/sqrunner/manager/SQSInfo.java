package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildserver.sonarplugin.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Created by linfar on 4/4/14.
 * <p/>
 * SonarQube Server info class
 */
public class SQSInfo {
    public static final String SERVERINFO_ID = "serverinfo.id";
    public static final String SONAR_URL = "sonar.host.url";
    public static final String SONAR_JDBC_URL = "sonar.jdbc.url";
    public static final String SONAR_JDBC_USERNAME = "sonar.jdbc.username";
    public static final String SONAR_JDBC_PASSWORD = "sonar.jdbc.password";
    private Properties myProps;

    private SQSInfo(Properties props) {
        myProps = props;
    }

    public String getUrl() {
        return (String)myProps.get(SONAR_URL);
    }

    public String getJDBCUrl() {
        return (String)myProps.get(SONAR_JDBC_URL);
    }

    public String getJDBCUsername() {
        return (String)myProps.get(SONAR_JDBC_USERNAME);
    }

    public String getJDBCPassword() {
        return (String)myProps.get(SONAR_JDBC_PASSWORD);
    }

    public String getId() {
        return (String)myProps.get(SERVERINFO_ID);
    }

    public static SQSInfo from(@NotNull final FileInputStream inStream) throws IOException {
        final Properties props = new Properties();
        props.load(inStream);
        return new SQSInfo(props);
    }

    public static ValidationError[] validate(@NotNull final Map<String, String[]> parameterMap) {
        final Accessor accessor = getAccessor(parameterMap);
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();
        if (!accessor.contains(SERVERINFO_ID)) {
            errors.add(new ValidationError("Server id missing", SERVERINFO_ID));
        }
        if (!accessor.contains(SONAR_URL)) {
            errors.add(new ValidationError("Server url missing", SONAR_URL));
        }
        return errors.toArray(new ValidationError[errors.size()]);
    }

    public static class ValidationError {
        public final String myError;
        public final String myKey;

        public ValidationError(String myError, String myKey) {
            this.myError = myError;
            this.myKey = myKey;
        }
    }

    public static SQSInfo from(@NotNull final Map<String, String[]> parameterMap) {
        return from(getAccessor(parameterMap));
    }

    private static Accessor getAccessor(final Map<String, String[]> parameterMap) {
        return new Accessor() {
            @Nullable
            public String get(@NotNull String key) {
                final String[] strings = parameterMap.get(key);
                if (strings != null && strings.length > 0) {
                    return strings[0];
                } else {
                    return null;
                }
            }
        };
    }

    private static SQSInfo from(Accessor access) {
        Properties props = new Properties();
        put(access, props, SERVERINFO_ID);
        put(access, props, SONAR_URL);
        put(access, props, SONAR_JDBC_URL);
        put(access, props, SONAR_JDBC_USERNAME);
        put(access, props, SONAR_JDBC_PASSWORD);
        return new SQSInfo(props);
    }

    private static void put(Accessor access, Properties props, String key) {
        final String value = access.get(key);
        if (value != null) props.put(key, value);
    }

    public void storeTo(@NotNull final File serverInfoFile) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(serverInfoFile);
            myProps.store(out, "");
        } finally {
            Util.close(out);
        }
    }

    private abstract static class Accessor {
        @Nullable
        public abstract String get(@NotNull final String key);

        public boolean contains(@NotNull final String key) {
            return get(key) != null;
        }
    }
}