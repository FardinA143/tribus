package Survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OpenStringQuestion extends Question {
    private List<String> responses;

    public OpenStringQuestion(String text) {
        super(text);
        this.responses = new ArrayList<>();
    }

    @Override
    public void ask() {
        System.out.println(getText());
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        responses.add(response);
    }

    @Override
    public void displayResults() {
        System.out.println("Responses to open question '" + getText() + "':");
        for (String response : responses) {
            System.out.println("- " + response);
        }
    }
}
