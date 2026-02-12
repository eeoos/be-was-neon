package router.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntPathMatcher {
    private static final Logger logger = LoggerFactory.getLogger(AntPathMatcher.class);

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

    public boolean isMatch(String pattern, String path) {
        if (pattern == null || path == null) {
            return false;
        }

        if (!pattern.contains("{")) {
            return pattern.equals(path);
        }

        String regex = convertPatternToRegex(pattern);
        return Pattern.compile(regex).matcher(path).matches();
    }

    public Map<String, String> extractPathVariables(String pattern, String path) {
        Map<String, String> variables = new HashMap<>();

        if (!isMatch(pattern, path)) {
            return variables;
        }

        List<String> variableNames = new ArrayList<>();
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pattern);
        while (matcher.find()) {
            variableNames.add(matcher.group(1));
        }

        Pattern regexPattern = Pattern.compile(convertPatternToRegex(pattern));
        Matcher pathMatcher = regexPattern.matcher(path);

        if (pathMatcher.find() && pathMatcher.groupCount() == variableNames.size()) {
            for (int i = 0; i < variableNames.size(); i++) {
                String value = pathMatcher.group(i + 1);
                variables.put(variableNames.get(i), value);
            }
        }
        return variables;
    }

    private String convertPatternToRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        regex.append("^");

        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pattern);
        int lastEnd = 0;

        while (matcher.find()) {
            regex.append(Pattern.quote(pattern.substring(lastEnd, matcher.start())));
            regex.append("([^/]+)");
            lastEnd = matcher.end();
        }

        if (lastEnd < pattern.length()) {
            regex.append(Pattern.quote(pattern.substring(lastEnd)));
        }

        regex.append("$");
        return regex.toString();
    }
}
