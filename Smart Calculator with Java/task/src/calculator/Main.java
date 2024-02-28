package calculator;

import java.util.Objects;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String text;
        while (!Objects.equals(text = sc.nextLine(), "/exit")) {
            if (text.isBlank()) continue;
            String[] numbers = text.split("\\s+");
            switch (numbers.length) {
                case 1 -> System.out.println(Integer.parseInt(numbers[0]));
                case 2 -> System.out.println(Integer.parseInt(numbers[0]) + Integer.parseInt(numbers[1]));
            }
        }
        System.out.println("Bye!");
    }
}
