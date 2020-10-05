package uk.gov.companieshouse.efs.api;

import java.lang.reflect.Field;
import java.util.Map;

public interface TestEnvironmentSetupHelper {

    public static void setEnvironmentVariable(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable");
        }
    }
}