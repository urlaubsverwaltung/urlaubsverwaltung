/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;


/**
 * @author  Aljona Murygina
 */
public class NumberUtilTest {

    public NumberUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {
    }


    @After
    public void tearDown() {
    }


    /**
     * Test of formatNumber method, of class NumberUtil.
     */
    @Test
    public void testFormatNumber() {

        Locale locale;
        BigDecimal numberToFormat;
        String returnValue;

        // for German locale
        locale = Locale.GERMAN;

        numberToFormat = BigDecimal.valueOf(6.00);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6", returnValue);

        numberToFormat = BigDecimal.valueOf(6.50);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6,5", returnValue);
        assertNotSame("6.5", returnValue);

        numberToFormat = BigDecimal.valueOf(6.55);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6,6", returnValue);
        assertNotSame("6.5", returnValue);

        numberToFormat = BigDecimal.valueOf(6.54);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6,5", returnValue);
        assertNotSame("6.5", returnValue);

        // for English locale
        locale = Locale.ENGLISH;

        numberToFormat = BigDecimal.valueOf(6.00);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6", returnValue);

        numberToFormat = BigDecimal.valueOf(6.50);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6.5", returnValue);
        assertNotSame("6,5", returnValue);

        numberToFormat = BigDecimal.valueOf(6.55);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6.6", returnValue);
        assertNotSame("6,5", returnValue);

        numberToFormat = BigDecimal.valueOf(6.54);
        returnValue = NumberUtil.formatNumber(numberToFormat, locale);
        assertEquals("6.5", returnValue);
        assertNotSame("6,5", returnValue);
    }


    /**
     * Test of parseNumber method, of class NumberUtil.
     */
    @Test
    public void testParseNumber() {

        Locale locale;
        String numberToParse;
        BigDecimal returnValue;

        locale = Locale.GERMAN;

        numberToParse = "6";
        returnValue = NumberUtil.parseNumber(numberToParse, locale);
        assertEquals(BigDecimal.valueOf(6), returnValue);

        numberToParse = "6,5";
        returnValue = NumberUtil.parseNumber(numberToParse, locale);
        assertEquals(BigDecimal.valueOf(6.5), returnValue);

        numberToParse = "6,55";
        returnValue = NumberUtil.parseNumber(numberToParse, locale);
        assertEquals(BigDecimal.valueOf(6.55), returnValue);

        locale = Locale.ENGLISH;

        numberToParse = "6";
        returnValue = NumberUtil.parseNumber(numberToParse, locale);
        assertEquals(BigDecimal.valueOf(6), returnValue);

        numberToParse = "6.5";
        returnValue = NumberUtil.parseNumber(numberToParse, locale);
        assertEquals(BigDecimal.valueOf(6.5), returnValue);

        numberToParse = "6.55";
        returnValue = NumberUtil.parseNumber(numberToParse, locale);
        assertEquals(BigDecimal.valueOf(6.55), returnValue);
    }
}
