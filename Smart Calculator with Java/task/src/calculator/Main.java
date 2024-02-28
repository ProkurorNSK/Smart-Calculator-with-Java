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
                try {
                    if (text.matches("^/.*")) {
                        throw new UnknownCommandException();
                    }
                    calculate(text);
                } catch (UnknownCommandException | InvalidExpressionException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        System.out.println("Bye!");
    }

    private static void calculate(String text) throws InvalidExpressionException {
        text = text.replaceAll("\\s+", "");
        text = text.replaceAll("--", "+");
        text = text.replaceAll("\\+{2,}", "+");
        text = text.replaceAll("\\+-|-\\+", "-");
        if (!text.matches("^[-+].*")) {
            text = "+" + text;
        }

        text = text.replaceAll("-", " -");
        text = text.replaceAll("\\+", " +");
        String[] numbers = text.trim().split("\\s+");

        int result = 0;
        for (String element : numbers) {

            if (!element.matches("^[+-].*")) {
                throw new InvalidExpressionException();
            }

            try {
                result += Integer.parseInt(element);
            } catch (NumberFormatException e) {
                throw new InvalidExpressionException();
            }
        }

        System.out.println(result);
    }

    static class UnknownCommandException extends RuntimeException {
        public UnknownCommandException() {
            super("Unknown command");
        }
    }

    static class InvalidExpressionException extends RuntimeException {
        public InvalidExpressionException() {
            super("Invalid expression");
        }
    }
}
