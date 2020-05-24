package games.alejandrocoria.mapfrontiers.common.util;

import java.lang.reflect.Field;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ReflectionHelper {
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object obj, String fieldName, Class<T> fieldType)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    private ReflectionHelper() {

    }
}
