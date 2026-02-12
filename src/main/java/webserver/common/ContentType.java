package webserver.common;

public enum ContentType {
    HTML(".html", "text/html;charset=utf-8"),
    CSS(".css", "text/css;charset=utf-8"),
    JS(".js", "application/javascript;charset=utf-8"),
    JSON(".json", "application/json;charset=utf-8"),
    PLAIN(".txt", "text/plain;charset=utf-8"),
    XML(".xml", "application/xml;charset=utf-8"),
    PNG(".png", "image/png"),
    JPEG(".jpg", "image/jpeg"),
    GIF(".gif", "image/gif"),
    SVG(".svg", "image/svg+xml"),
    ICO(".ico", "image/x-icon"),
    OCTET_STREAM(".bin", "application/octet-stream");

    private final String extension;
    private final String mineType;

    ContentType(String extension, String mineType) {
        this.extension = extension;
        this.mineType = mineType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMineType() {
        return mineType;
    }

    public static ContentType getContentTypeByPath(String path){
        if (path == null || path.isEmpty()) {
            return OCTET_STREAM;
        }

        String extension = getExtensionFromPath(path);
        if (extension == null) {
            return OCTET_STREAM;
        }

        if (extension.equals(".jpeg")) {
            return JPEG;
        }

        for (ContentType type : values()) {
            if (type.getExtension().equals(extension)) {
                return type;
            }
        }

        return OCTET_STREAM;
    }

    private static String getExtensionFromPath(String path) {
        int lastDotPos = path.lastIndexOf('.');
        if (lastDotPos > 0 && lastDotPos < path.length() - 1) {
            return path.substring(lastDotPos).toLowerCase();
        }

        return null;
    }
}
