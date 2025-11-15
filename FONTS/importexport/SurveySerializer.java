package importexport;

import Exceptions.NotValidFileException;
import Survey.Survey;

public interface SurveySerializer {
    void toFile(Survey s, String path);
    Survey fromFile(String path) throws NotValidFileException;
}
