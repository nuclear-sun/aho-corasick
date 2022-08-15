package org.sun.ahocorasick.zh;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class FCNDATAutomatonTest {


    @Test
    public void testFussyParseText() {


        FCNDATAutomaton.Builder builder = FCNDATAutomaton.builder();
        builder.put("习惯", null, true);
        builder.put("习大大", null, true);

        FCNDATAutomaton automaton = builder.build();

        List list = automaton.fussyParseText("xi打大");
        System.out.println(list);
    }

    @Test
    public void testTestFussyParseText() {
    }
}