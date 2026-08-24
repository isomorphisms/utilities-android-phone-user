package com.isomorphisms.mathcharacters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MathCharactersTest {
    @Test
    public void keepsTheRequestedRestrictedRows() {
        assertArrayEquals(new String[] {"←", "↑", "↓", "→", "↔", "↦", "⇒"}, MathCharacters.ROWS[0]);
        assertArrayEquals(new String[] {"ℂ", "ℝ", "ℚ", "ℤ", "ℕ", "∞"}, MathCharacters.ROWS[1]);
        assertArrayEquals(new String[] {"i", "j", "k"}, MathCharacters.ROWS[2]);
        assertEquals("0", MathCharacters.ROWS[3][0]);
        assertEquals("9", MathCharacters.ROWS[3][9]);
        assertArrayEquals(new String[] {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹", "⁺", "⁻", "⁼", "⁽", "⁾", "ⁱ", "ʲ", "ᵏ", "ˡ", "ⁿ"}, MathCharacters.ROWS[4]);
        assertArrayEquals(new String[] {"₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉", "₊", "₋", "₌", "₍", "₎", "ᵢ", "ⱼ", "ₖ", "ₗ"}, MathCharacters.ROWS[5]);
    }
}
