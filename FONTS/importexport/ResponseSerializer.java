package importexport;
    
import java.io.File;
import java.util.Scanner;

public class ResponseSerializer {
    public void toFile(SurveyResponse sb, String path) {

    }

    public Response fromFile(String path) {
        File file = new File(path);

        try(Scanner scan = new Scanner(file)){
            while(scan.hasNextLine()){
                String line = scan.nextLine();
            }
        }
        

        return new Response(); // placeholder
    }

    private lineTo
}