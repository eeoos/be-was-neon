package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    private FileUtils() {

    }

    public static byte[] readFileBytes(String relativePath) throws IOException {
        try (InputStream is = FileUtils.class.getClassLoader().getResourceAsStream("templates" + relativePath)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: templates" + relativePath);
            }
            return is.readAllBytes();
        }
    }

    public static String decodeUrl(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
