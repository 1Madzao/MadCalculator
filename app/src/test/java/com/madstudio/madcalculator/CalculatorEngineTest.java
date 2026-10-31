package com.madstudio.madcalculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CalculatorEngineTest {

    private CalculatorEngine engine;

    @Before
    public void setUp() {
        engine = new CalculatorEngine();
    }

    @Test
    public void testBasicArithmetic() {
        assertEquals("12", engine.evaluate("7 + 5"));
        assertEquals("20", engine.evaluate("15 + 5"));

        assertEquals("6", engine.evaluate("10 - 4"));
        assertEquals("3", engine.evaluate("8 \u2212 5"));

        assertEquals("24", engine.evaluate("6 * 4"));
        assertEquals("24", engine.evaluate("6 \u00D7 4"));

        assertEquals("5", engine.evaluate("15 / 3"));
        assertEquals("5", engine.evaluate("15 \u00F7 3"));
        assertEquals("2,5", engine.evaluate("5 / 2"));
    }

    @Test
    public void testOperatorPrecedence() {
        assertEquals("14", engine.evaluate("2 + 3 * 4"));
        assertEquals("14", engine.evaluate("2 + 3 \u00D7 4"));

        assertEquals("20", engine.evaluate("(2 + 3) * 4"));
        assertEquals("20", engine.evaluate("(2 + 3) \u00D7 4"));

        assertEquals("10", engine.evaluate("20 / 2 + 0"));
        assertEquals("2", engine.evaluate("20 / (2 * 5)"));
        assertEquals("11", engine.evaluate("3 + 4 * 2"));
        assertEquals("14", engine.evaluate("2 * 3 + 8"));
    }

    @Test
    public void testImplicitMultiplication() {
        assertEquals("6", engine.evaluate("2(3)"));
        assertEquals("20", engine.evaluate("4(2 + 3)"));

        assertEquals("6", engine.evaluate("(2)3"));

        assertEquals("20", engine.evaluate("(2 + 3)(4)"));
        assertEquals("6", engine.evaluate("(2)(3)"));
        assertEquals("15", engine.evaluate("(2 + 3)(1 + 2)"));

        String res2Pi = engine.evaluate("2\u03C0");
        assertNotNull(res2Pi);
        assertTrue(res2Pi.startsWith("6,28318"));
    }

    @Test
    public void testPiConstant() {
        String pi = engine.evaluate("\u03C0");
        assertNotNull(pi);
        assertTrue(pi.startsWith("3,14159"));

        String piPlusOne = engine.evaluate("\u03C0 + 1");
        assertTrue(piPlusOne.startsWith("4,14159"));

        String piTimesTwo = engine.evaluate("\u03C0 * 2");
        assertTrue(piTimesTwo.startsWith("6,28318"));
    }

    @Test
    public void testPowerAndSquareRoot() {
        assertEquals("8", engine.evaluate("2^3"));
        assertEquals("16", engine.evaluate("4^2"));
        assertEquals("1", engine.evaluate("5^0"));

        assertEquals("3", engine.evaluate("\u221A9"));
        assertEquals("5", engine.evaluate("\u221A25"));

        assertEquals("3", engine.evaluate("\u221A(2 + 7)"));
        assertEquals("5", engine.evaluate("\u221A(16 + 9)"));
    }

    @Test
    public void testTrigonometry() {
        engine.setDegrees(true);
        assertTrue(engine.isDegrees());
        assertEquals("1", engine.evaluate("sin(90)"));
        assertEquals("1", engine.evaluate("cos(0)"));
        assertEquals("0", engine.evaluate("sin(0)"));
        assertEquals("0", engine.evaluate("cos(90)"));

        engine.setDegrees(false);
        assertFalse(engine.isDegrees());
        assertEquals("1", engine.evaluate("cos(0)"));
        assertEquals("0", engine.evaluate("sin(0)"));
        assertEquals("1", engine.evaluate("sin(\u03C0 / 2)"));
        assertEquals("-1", engine.evaluate("cos(\u03C0)"));
    }

    @Test
    public void testFactorial() {
        assertEquals("120", engine.evaluate("5!"));
        assertEquals("1", engine.evaluate("0!"));
        assertEquals("1", engine.evaluate("1!"));
        assertEquals("2", engine.evaluate("2!"));
        assertEquals("6", engine.evaluate("3!"));
        assertEquals("24", engine.evaluate("4!"));
        assertEquals("720", engine.evaluate("6!"));
    }

    @Test(expected = ArithmeticException.class)
    public void testDivisionByZero() {
        engine.evaluate("10 / 0");
    }

    @Test(expected = ArithmeticException.class)
    public void testDivisionByZeroUnicode() {
        engine.evaluate("5 \u00F7 0");
    }

    @Test
    public void testAutoClosingParentheses() {
        assertEquals("5", engine.evaluate("(2+3"));
        assertEquals("14", engine.evaluate("2 + (3 * 4"));
        assertEquals("20", engine.evaluate("((2 + 3) * 4"));
        assertEquals("3", engine.evaluate("\u221A(9"));
    }

    @Test
    public void testEmptyOrNullExpression() {
        assertEquals("", engine.evaluate(null));
        assertEquals("", engine.evaluate(""));
        assertEquals("", engine.evaluate("   "));
    }
}
