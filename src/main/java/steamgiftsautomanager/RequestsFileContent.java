package steamgiftsautomanager;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class RequestsFileContent {
    String cookieName;
    String cookieValue;
    String xsrfToken;
    @With String[] exactMatches;
    String[] anyMatches;
    String[] noMatches;
}
