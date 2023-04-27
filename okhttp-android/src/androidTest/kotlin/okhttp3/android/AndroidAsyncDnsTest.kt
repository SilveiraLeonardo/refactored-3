
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DomainValidator {
    private static final String DOMAIN_NAME_REGEX = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
    private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile(DOMAIN_NAME_REGEX);

    public static boolean isValidDomainName(String domain) {
        Matcher matcher = DOMAIN_NAME_PATTERN.matcher(domain);
        return matcher.matches();
    }
}
