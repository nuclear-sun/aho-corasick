package org.sun.ahocorasick.hanzi;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ShapeSimTableTest {

    ShapeSimTable shapeSimTable;

    @BeforeClass
    void setUp() {
        shapeSimTable = ShapeSimTable.getInstance();
    }

    @Test
    public void testGetSimilarity() {

        double similarity = shapeSimTable.getSimilarity('爱', '寺');
        System.out.println(similarity);

    }

    @Test
    public void testGetSimilarChars() {
        String similarChars = shapeSimTable.getSimilarChars('习');
        System.out.println(similarChars);
    }

    @Test
    public void test() {
        String a= "𬴊";

        int length = a.length();



        System.out.println(length);

        System.out.println(a.charAt(0));

        char[] chars = Character.toChars(0x29F8C);

        String b = "𩾌";

        assertEquals(chars[0], b.charAt(0));
        assertEquals(chars[1], b.charAt(1));
        assertEquals(chars.length, b.length());

        System.out.println(chars.length);

    }
}