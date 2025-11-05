package importexport;

import java.io.File;

public interface SurveySerializer {
    void toFile(Survey s, String path);
    Survey fromFile(String path);
}

public class TxtSurveySerializer implements SurveySerializer {

    public void toFile(Survey s, String path) {

    }

    public Survey fromFile(String path) {
        File file = new File(path);

        try(Scanner scan = new SCanner)

        return new Survey(); // placeholder
    }
}
