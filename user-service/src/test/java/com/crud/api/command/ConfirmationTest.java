package com.crud.api.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ConfirmationTest {

    @ParameterizedTest
    @CsvSource({
            "y, true",
            "Y, true",
            "yes, false",
            "n, false",
            "N, false",
            "no, false",
            " , false",
            "'', false"
    })
    void isConfirmed_ShouldReturnExpectedResult(String input, boolean expected) {
        assertEquals(expected, Confirmation.isConfirmed(input));
    }

    @Test
    void isConfirmed_WithNull_ShouldReturnFalse() {
        assertFalse(Confirmation.isConfirmed(null));
    }

    @Test
    void values_ShouldContainYesAndNo() {
        Confirmation[] values = Confirmation.values();
        assertEquals(2, values.length);
        assertEquals(Confirmation.YES, values[0]);
        assertEquals(Confirmation.NO, values[1]);
    }

    @Test
    void value_ShouldReturnCorrectString() {
        assertEquals("y", Confirmation.YES.value());
        assertEquals("n", Confirmation.NO.value());
    }

    @ParameterizedTest
    @CsvSource({
            "y, true",
            "Y, true",
            "yes, false",
            "n, false",
            "'', false"
    })
    void yes_Matches_ShouldReturnExpectedResult(String input, boolean expected) {
        assertEquals(expected, Confirmation.YES.matches(input));
    }

    @ParameterizedTest
    @CsvSource({
            "n, true",
            "N, true",
            "no, false",
            "y, false",
            "'', false"
    })
    void no_Matches_ShouldReturnExpectedResult(String input, boolean expected) {
        assertEquals(expected, Confirmation.NO.matches(input));
    }

    @Test
    void matches_WithNull_ShouldReturnFalse() {
        assertFalse(Confirmation.YES.matches(null));
        assertFalse(Confirmation.NO.matches(null));
    }
}
