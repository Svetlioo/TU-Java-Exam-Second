import model.Regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexTester {
    public static List<Boolean> test(Regex regex, String[] strings) {
        List<Boolean> booleanList = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex.getPattern());
        for (String str : strings) {
            if (pattern.matcher(str).matches()) {
                booleanList.add(true);
            } else {
                booleanList.add(false);
            }
        }
        return booleanList;
    }
}
