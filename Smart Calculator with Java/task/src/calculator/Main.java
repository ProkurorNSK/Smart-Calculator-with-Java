package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Main {

    private static final Map<String, BigInteger> variables = new HashMap<>();
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
//                            printStack(infix);
                            convertToPostfix();
//                            printStack(postfix);
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

    private static void printStack(List<Element> elements) {
        elements.forEach(element -> System.out.print(element + " "));
        System.out.println();
    }

    private static void calculate() {
        Deque<Element> stack = new ArrayDeque<>();

        for (Element element : postfix) {
            switch (element.type) {
                case NUMBER, VARIABLE -> stack.push(element);
                case BINARY_OPERATOR -> {
                    BigInteger x = ((Number) stack.pop()).number;
                    BigInteger y = ((Number) stack.pop()).number;
                    stack.push(new Number(Type.NUMBER, ((BinaryOperator) element).biOperator.apply(y, x)));
                }
                case UNARY_OPERATOR -> {
                    BigInteger y = ((Number) stack.pop()).number;
                    stack.push(new Number(Type.NUMBER, ((UnaryOperator) element).unOperator.apply(y)));
                }
            }
        }

        System.out.println(((Number) stack.pop()).number);
    }

    private static void convertToPostfix() throws CalculatorException {
        postfix.clear();
        Deque<Element> stack = new ArrayDeque<>();
        for (Element element : infix) {
            switch (element.type) {
                case VARIABLE, NUMBER -> {
                    postfix.add(element);
                    while (!stack.isEmpty() && stack.peek().type == Type.UNARY_OPERATOR) {
                        postfix.add(stack.pop());
                    }
                }
                case BINARY_OPERATOR -> {
                    if (stack.isEmpty() || ((Operator)stack.peek()).priority < ((Operator)element).priority) {
                        stack.push(element);
                    } else {
                        while (!stack.isEmpty() && ((Operator)stack.peek()).priority >= ((Operator)element).priority) {
                            postfix.add(stack.pop());
                        }
                        stack.push(element);
                    }
                }
                case UNARY_OPERATOR, LEFT_PARENTHESIS -> stack.push(element);
                case RIGHT_PARENTHESIS -> {
                    while (!(stack.isEmpty() || stack.peek().type == Type.LEFT_PARENTHESIS)) {
                        postfix.add(stack.pop());
                    }
                    if (stack.isEmpty() || stack.peek().type != Type.LEFT_PARENTHESIS) {
                        throw new CalculatorException("Invalid expression");
                    } else {
                        stack.pop();
                    }
                }
            }
        }
        while (!stack.isEmpty()) {
            Element element = stack.pop();
            if (element.type == Type.LEFT_PARENTHESIS || element.type == Type.RIGHT_PARENTHESIS) {
                throw new CalculatorException("Invalid expression");
            } else {
                postfix.add(element);
            }
        }
    }

    private static boolean parseInput(String input) throws CalculatorException {
        String[] parts = input.split("=");
        if (parts.length == 2) {
            String variable = parts[0];
            String assignment = parts[1];
            if (variable.matches("[A-Za-z]+")) {
                if (assignment.matches("[-+]?\\d+")) {
                    variables.put(variable, new BigInteger(assignment));
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
                if (currentType == Type.VARIABLE || currentType == Type.NUMBER || currentType == Type.RIGHT_PARENTHESIS) {
                    addToken(currentType, token);
                    token = "";
                    currentType = Type.BINARY_OPERATOR;
                    int priority = 0;
                    BiFunction<BigInteger, BigInteger, BigInteger> function = null;
                    switch (ch) {
                        case '+' -> {
                            function = BigInteger::add;
                            priority = 1;
                        }
                        case '-' -> {
                            function = BigInteger::subtract;
                            priority = 1;
                        }
                        case '*' -> {
                            function = BigInteger::multiply;
                            priority = 2;
                        }
                        case '/' -> {
                            function = BigInteger::divide;
                            priority = 2;
                        }
                    }
                    infix.add(new BinaryOperator(currentType, priority, function, ch));
                } else {
                    currentType = Type.UNARY_OPERATOR;
                    Function<BigInteger, BigInteger> function = switch (ch) {
                        case '+' -> Function.identity();
                        case '-' -> BigInteger::negate;
                        default -> throw new CalculatorException("Invalid expression");
                    };
                    infix.add(new UnaryOperator(currentType, 4, function, ch));
                }
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
            } else if (ch == '(') {
                currentType = Type.LEFT_PARENTHESIS;
                infix.add(new Operator(currentType, 0, ch));
            } else if (ch == ')') {
                addToken(currentType, token);
                token = "";
                currentType = Type.RIGHT_PARENTHESIS;
                infix.add(new Operator(currentType, 0, ch));
            } else {
                throw new CalculatorException("Invalid expression");
            }
        }

        if (!token.isEmpty()) {
            addToken(currentType, token);
        }

        return true;
    }

    private static void addToken(Type type, String token) throws CalculatorException {
        if (token.isEmpty()) {
            return;
        }

        switch (type) {
            case NUMBER -> {
                try {
                    infix.add(new Number(type, new BigInteger(token)));
                } catch (NumberFormatException e) {
                    throw new CalculatorException("Invalid expression");
                }
            }
            case VARIABLE -> {
                if (variables.containsKey(token)) {
                    infix.add(new Variable(type, variables.get(token), token));
                } else {
                    throw new CalculatorException("Invalid expression");
                }
            }
        }
    }
}

class Element {
    Type type;

    Element(Type type) {
        this.type = type;
    }
}

class Number extends Element {
    BigInteger number;

    Number(Type type, BigInteger number) {
        super(type);
        this.number = number;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }
}

class Variable extends Number {
    String variable;

    Variable(Type type, BigInteger number, String variable) {
        super(type, number);
        this.variable = variable;
    }

    @Override
    public String toString() {
        return variable;
    }
}

class Operator extends Element {
    int priority;
    String symbol;

    Operator(Type type, int priority, char symbol) {
        super(type);
        this.priority = priority;
        this.symbol = String.valueOf(symbol);
    }

    @Override
    public String toString() {
        return symbol;
    }
}

class BinaryOperator extends Operator {
    BiFunction<BigInteger, BigInteger, BigInteger> biOperator;

    BinaryOperator(Type type, int priority, BiFunction<BigInteger, BigInteger, BigInteger> biOperator, char symbol) {
        super(type, priority, symbol);
        this.biOperator = biOperator;
    }
}

class UnaryOperator extends Operator {
    Function<BigInteger, BigInteger> unOperator;

    UnaryOperator(Type type, int priority, Function<BigInteger, BigInteger> unOperator, char symbol) {
        super(type, priority, symbol);
        this.unOperator = unOperator;
    }
}

enum Type {
    NUMBER, VARIABLE, UNARY_OPERATOR, BINARY_OPERATOR, LEFT_PARENTHESIS, RIGHT_PARENTHESIS
}

class CalculatorException extends Exception {
    public CalculatorException(String text) {
        super(text);
    }
}

