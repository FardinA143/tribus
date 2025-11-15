package importexport;

import Exceptions.NotValidFileException;
import Response.*;
import java.util.*;

public interface ResponseSerializer {
    void toFile(List<SurveyResponse> s, String path);
    SurveyResponse fromFile(String path) throws NotValidFileException;
}

