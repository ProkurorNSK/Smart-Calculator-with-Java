package calculator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    private static final Map<String, Integer> variables = new HashMap<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String text;
        while (!Objects.equals(text = sc.nextLine(), "/exit")) {
            if (text.isBlank()) {
                continue;
            } else if (text.equals("/help")) {
                System.out.println("The program calculates the sum of numbers");
            } else {
                try {
                    if (text.matches("^/.*")) {
                        throw new CalculatorException("Unknown command");
                    }
                    calculate(text);
                } catch (CalculatorException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        System.out.println("Bye!");
    }

    private static void calculate(String text) throws CalculatorException {
        text = text.replaceAll("\\s+", "");
        text = text.replaceAll("--", "+");
        text = text.replaceAll("\\+{2,}", "+");
        text = text.replaceAll("\\+-|-\\+", "-");

        String[] parts = text.split("=");
        if (parts.length == 2) {
            if (parts[0].matches("[A-Za-z]+")) {
                if (parts[1].matches("[-+]?\\d+")) {
                    variables.put(parts[0], Integer.parseInt(parts[1]));
                } else if (!parts[1].matches("[A-Za-z]+")) {
                    throw new CalculatorException("Invalid assignment");
                } else if (variables.containsKey(parts[1])) {
                    variables.put(parts[0], variables.get(parts[1]));
                } else {
                    throw new CalculatorException("Unknown variable");
                }
            } else {
                throw new CalculatorException("Invalid identifier");
            }
            return;
        } else if (parts.length > 2) {
            throw new CalculatorException("Invalid assignment");
        }

        text = text.replaceAll("-", " -");
        text = text.replaceAll("\\+", " +");
        String[] numbers = text.trim().split("\\s+");

        int result = 0;
        for (String element : numbers) {
            if (element.matches("[-+]?\\d+")) {
                try {
                    result += Integer.parseInt(element);
                } catch (NumberFormatException e) {
                    throw new CalculatorException("Invalid expression");
                }
            } else if (element.matches("[A-Za-z]+")) {
                if (variables.containsKey(element)) {
                    result += variables.get(element);
                } else {
                    throw new CalculatorException("Unknown variable");
                }
            } else if (element.matches("[+-][A-Za-z]+")) {
                String variable = element.substring(1);
                int index = element.charAt(0) == '-' ? -1 : 1;
                if (variables.containsKey(variable)) {
                    result += index * variables.get(variable);
                } else {
                    throw new CalculatorException("Unknown variable");
                }
            } else {
                throw new CalculatorException("Invalid expression");
            }
        }

        System.out.println(result);
    }
}

class CalculatorException extends RuntimeException {
    public CalculatorException(String text) {
        super(text);
    }
}

