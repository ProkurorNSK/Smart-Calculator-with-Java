package calculator;

import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String text;
        while (!Objects.equals(text = sc.nextLine(), "/exit")) {
            if (text.isBlank()) {
                continue;
            }
            else if (text.equals("/help")) {
                System.out.println("The program calculates the sum of numbers");
            } else {
                String[] numbers = text.split("\\s+");
                int result = 0;
                for (String element : numbers) {
                    result += Integer.parseInt(element);
                }
                System.out.println(result);
            }
        }
        System.out.println("Bye!");
    }
}
