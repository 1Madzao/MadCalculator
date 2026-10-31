package com.madstudio.madcalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CalculatorEngine {

    private boolean isDegrees = true;

    public void setDegrees(boolean degrees) {
        this.isDegrees = degrees;
    }

    public boolean isDegrees() {
        return isDegrees;
    }

    public String evaluate(String rawExpression) {
        if (rawExpression == null || rawExpression.trim().isEmpty()) {
            return "";
        }

        String normalized = normalizeExpression(rawExpression);
        if (normalized.isEmpty()) {
            return "";
        }

        double result = parse(normalized);
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new ArithmeticException("Operação inválida");
        }

        return formatNumber(result);
    }

    private String normalizeExpression(String expr) {
        String s = expr.replace(" ", "")
                .replace(",", ".")
                .replace("\u00D7", "*")
                .replace("\u00F7", "/")
                .replace("\u2212", "-")
                .replace("π", "\u03C0");

        boolean trimmed = true;
        while (trimmed && !s.isEmpty()) {
            trimmed = false;
            char last = s.charAt(s.length() - 1);
            if ("+-*/^(\u221A".indexOf(last) != -1) {
                s = s.substring(0, s.length() - 1);
                trimmed = true;
            } else {
                for (String fn : new String[]{"sin", "cos", "tan", "log", "ln"}) {
                    if (s.endsWith(fn)) {
                        s = s.substring(0, s.length() - fn.length());
                        trimmed = true;
                        break;
                    }
                }
            }
        }

        if (s.isEmpty()) return "";

        int openCount = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') openCount++;
            else if (s.charAt(i) == ')') openCount--;
        }
        StringBuilder sb = new StringBuilder(s);
        while (openCount > 0) {
            sb.append(')');
            openCount--;
        }

        return sb.toString();
    }

    private String formatNumber(double value) {
        if (Math.abs(value) < 1e-12) value = 0.0;

        BigDecimal bd = BigDecimal.valueOf(value)
                .round(new MathContext(12, RoundingMode.HALF_UP))
                .stripTrailingZeros();

        double cleanVal = bd.doubleValue();
        if (Math.abs(cleanVal) >= 1e12 || (Math.abs(cleanVal) > 0 && Math.abs(cleanVal) < 1e-7)) {
            DecimalFormat sciFormat = new DecimalFormat("0.######E0", DecimalFormatSymbols.getInstance(Locale.US));
            return sciFormat.format(cleanVal).replace(".", ",");
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,##0.##########", symbols);
        return df.format(bd);
    }

    private double factorial(double n) {
        if (Double.isNaN(n) || Double.isInfinite(n)) {
            throw new ArithmeticException("Operação inválida");
        }
        if (n < 0) {
            throw new ArithmeticException("Fatorial de número negativo");
        }
        long rounded = Math.round(n);
        if (Math.abs(n - rounded) > 1e-9) {
            throw new ArithmeticException("Fatorial apenas para números inteiros");
        }
        if (rounded > 170) {
            throw new ArithmeticException("Valor muito grande para fatorial");
        }
        double res = 1.0;
        for (long i = 2; i <= rounded; i++) {
            res *= i;
        }
        return res;
    }

    private double parse(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                while (ch == ' ') nextChar();
                if (pos < str.length()) {
                    throw new IllegalArgumentException("Caractere inesperado: " + (char) ch);
                }
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    while (ch == ' ') nextChar();
                    if (eat('*')) {
                        x *= parseFactor();
                    } else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0.0) {
                            throw new ArithmeticException("Divisão por zero");
                        }
                        x /= divisor;
                    } else if (isImplicitMultiplierStart()) {
                        x *= parseFactor();
                    } else {
                        return x;
                    }
                }
            }

            boolean isImplicitMultiplierStart() {
                return ch == '(' || ch == '\u03C0' || ch == 'π' || ch == '\u221A'
                        || Character.isLetter(ch)
                        || (ch >= '0' && ch <= '9')
                        || ch == '.';
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) {
                        throw new IllegalArgumentException("Parêntese não fechado");
                    }
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    if (ch == 'E' || ch == 'e') {
                        int nextPos = pos + 1;
                        if (nextPos < str.length()) {
                            char nc = str.charAt(nextPos);
                            if (Character.isDigit(nc) || nc == '+' || nc == '-') {
                                nextChar();
                                if (ch == '+' || ch == '-') nextChar();
                                while (ch >= '0' && ch <= '9') nextChar();
                            }
                        }
                    }
                    try {
                        x = Double.parseDouble(str.substring(startPos, this.pos));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Formato de número inválido");
                    }
                } else if (eat('\u03C0') || eat('π')) {
                    x = Math.PI;
                } else if (eat('\u221A')) {
                    double val = parseFactor();
                    if (val < 0) {
                        throw new ArithmeticException("Raiz quadrada de número negativo");
                    }
                    x = Math.sqrt(val);
                } else if (Character.isLetter(ch)) {
                    while (Character.isLetter(ch)) nextChar();
                    String ident = str.substring(startPos, this.pos);
                    if (ident.equalsIgnoreCase("e")) {
                        x = Math.E;
                    } else if (ident.equalsIgnoreCase("pi")) {
                        x = Math.PI;
                    } else {
                        double arg = parseFactor();
                        x = applyFunction(ident, arg);
                    }
                } else {
                    throw new IllegalArgumentException("Expressão incompleta");
                }

                while (true) {
                    if (eat('!')) {
                        x = factorial(x);
                    } else if (eat('%')) {
                        x /= 100.0;
                    } else {
                        break;
                    }
                }

                if (eat('^')) {
                    double exp = parseFactor();
                    if (x == 0.0 && exp < 0) {
                        throw new ArithmeticException("Divisão por zero");
                    }
                    if (x < 0 && Math.abs(exp - Math.round(exp)) > 1e-9) {
                        throw new ArithmeticException("Raiz de índice par de número negativo");
                    }
                    x = Math.pow(x, exp);
                    if (Double.isNaN(x) || Double.isInfinite(x)) {
                        throw new ArithmeticException("Operação inválida");
                    }
                }

                return x;
            }

            double applyFunction(String func, double arg) {
                switch (func.toLowerCase(Locale.ROOT)) {
                    case "sin":
                        if (isDegrees) {
                            double normalized = arg % 360.0;
                            if (normalized < 0) normalized += 360.0;
                            if (Math.abs(normalized - 180.0) < 1e-12 || Math.abs(normalized) < 1e-12 || Math.abs(normalized - 360.0) < 1e-12) {
                                return 0.0;
                            }
                            return Math.sin(Math.toRadians(arg));
                        }
                        return Math.sin(arg);
                    case "cos":
                        if (isDegrees) {
                            double normalized = arg % 360.0;
                            if (normalized < 0) normalized += 360.0;
                            if (Math.abs(normalized - 90.0) < 1e-12 || Math.abs(normalized - 270.0) < 1e-12) {
                                return 0.0;
                            }
                            return Math.cos(Math.toRadians(arg));
                        }
                        return Math.cos(arg);
                    case "tan":
                        if (isDegrees) {
                            double normalized = arg % 180.0;
                            if (normalized < 0) normalized += 180.0;
                            if (Math.abs(normalized - 90.0) < 1e-9) {
                                throw new ArithmeticException("Tangente indefinida");
                            }
                            if (Math.abs(normalized) < 1e-12 || Math.abs(normalized - 180.0) < 1e-12) {
                                return 0.0;
                            }
                            return Math.tan(Math.toRadians(arg));
                        } else {
                            double mod = Math.abs((arg - Math.PI / 2.0) % Math.PI);
                            if (mod < 1e-9 || Math.abs(mod - Math.PI) < 1e-9) {
                                throw new ArithmeticException("Tangente indefinida");
                            }
                            return Math.tan(arg);
                        }
                    case "ln":
                        if (arg <= 0) {
                            throw new ArithmeticException("Logaritmo de número não positivo");
                        }
                        return Math.log(arg);
                    case "log":
                        if (arg <= 0) {
                            throw new ArithmeticException("Logaritmo de número não positivo");
                        }
                        return Math.log10(arg);
                    default:
                        throw new IllegalArgumentException("Função desconhecida: " + func);
                }
            }
        }.parse();
    }
}
