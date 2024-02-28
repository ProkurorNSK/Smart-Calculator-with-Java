package calculator;

import java.util.*;
import java.util.function.BiFunction;

public class Main {

    private static final Map<String, Integer> variables = new HashMap<>();
    private static final List<Element> postfix = new ArrayList<>();
    private static final List<Element> infix = new ArrayList<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input;
        while (!Objects.equals(input = sc.nextLine(), "/exit")) {
            if (!input.isBlank()) {
                if (input.equals("/help")) {
                    System.out.println("The program calculates all.");
                } else if (input.startsWith("/")) {
                    System.out.println("Unknown command");
                } else {
                    try {
                        input = input.replaceAll("\\s+", "");
                        if (parseInput(input)) {
                            convertToPostfix();
                            calculate();
                        }
                    } catch (CalculatorException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        System.out.println("Bye!");
    }

    private static void convertToPostfix() {
        postfix.clear();
        Deque<Element> stack = new ArrayDeque<>();
        for (Element element : infix) {
            switch (element.type()) {
                case VARIABLE, NUMBER -> postfix.add(element);
                case BINARY_OPERATOR -> {
                    if (!stack.isEmpty()) {
                        postfix.add(stack.pop());
                    }
                    stack.push(element);
                }
            }
        }
        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }
    }

    private static boolean parseInput(String input) throws CalculatorException {
        String[] parts = input.split("=");
        if (parts.length == 2) {
            String variable = parts[0];
            String assignment = parts[1];
            if (variable.matches("[A-Za-z]+")) {
                if (assignment.matches("[-+]?\\d+")) {
                    variables.put(variable, Integer.parseInt(assignment));
                } else if (!assignment.matches("[A-Za-z]+")) {
                    throw new CalculatorException("Invalid assignment");
                } else if (variables.containsKey(assignment)) {
                    variables.put(variable, variables.get(assignment));
                } else {
                    throw new CalculatorException("Unknown variable");
                }
            } else {
                throw new CalculatorException("Invalid identifier");
            }
            return false;
        } else if (parts.length > 2) {
            throw new CalculatorException("Invalid assignment");
        }

        infix.clear();
        String token = "";
        Type currentType = Type.BINARY_OPERATOR;
        for (char ch : input.toCharArray()) {
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^') {
                addToken(currentType, token);
                token = "";
                currentType = Type.BINARY_OPERATOR;
                BiFunction<Integer, Integer, Integer> function = switch (ch) {
                    case '+' -> Integer::sum;
                    case '-' -> (x, y) -> x - y;
                    case '*' -> (x, y) -> x * y;
                    case '/' -> (x, y) -> x / y;
                    case '^' -> (x, y) -> (int) Math.pow(x, y);
                    default -> null;
                };
                infix.add(new Element(currentType, 0, String.valueOf(ch), function));
            } else if (Character.isDigit(ch)) {
                if (currentType == Type.VARIABLE) {
                    addToken(currentType, token);
                    token = "";
                }
                currentType = Type.NUMBER;
                token = token.concat(String.valueOf(ch));
            } else if (Character.isLetter(ch)) {
                if (currentType == Type.NUMBER) {
                    addToken(currentType, token);
                    token = "";
                }
                currentType = Type.VARIABLE;
                token = token.concat(String.valueOf(ch));
            }
        }

        if (!token.isEmpty()) {
            addToken(currentType, token);
        }

        return true;
    }

    private static void addToken(Type type, String token) throws CalculatorException {
        switch (type) {
            case NUMBER -> {
                try {
                    infix.add(new Element(type, Integer.parseInt(token), token, null));
                } catch (NumberFormatException e) {
                    throw new CalculatorException("Invalid expression");
                }
            }
            case VARIABLE -> {
                if (variables.containsKey(token)) {
                    infix.add(new Element(type, variables.get(token), token, null));
                } else {
                    throw new CalculatorException("Invalid expression");
                }
            }
        }
    }

    private static void calculate() {
        Deque<Element> stack = new ArrayDeque<>();

        for (Element element : postfix) {
            switch (element.type()) {
                case NUMBER, VARIABLE -> stack.push(element);
                case BINARY_OPERATOR -> {
                    int x = stack.pop().number();
                    int y = stack.pop().number();
                    stack.push(new Element(Type.NUMBER, element.biOperator().apply(y, x), "", null));
                }
            }
        }

        System.out.println(stack.pop().number());
    }
}

record Element(Type type, int number, String variable, BiFunction<Integer, Integer, Integer> biOperator) {
}

enum Type {
    NUMBER, VARIABLE, UNARY_OPERATOR, BINARY_OPERATOR, LEFT_PARENTHESIS, RIGHT_PARENTHESIS
}

class CalculatorException extends Exception {
    public CalculatorException(String text) {
        super(text);
    }
}

