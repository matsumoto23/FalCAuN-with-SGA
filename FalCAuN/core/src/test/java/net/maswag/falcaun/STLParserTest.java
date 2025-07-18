package net.maswag.falcaun;

import net.maswag.falcaun.TemporalConst.STLConst;
import net.maswag.falcaun.TemporalAnd.STLAnd;
import net.maswag.falcaun.TemporalEventually.STLEventually;
import net.maswag.falcaun.TemporalGlobally.STLGlobally;
import net.maswag.falcaun.TemporalImply.STLImply;
import net.maswag.falcaun.TemporalLogic.STLCost;
import net.maswag.falcaun.TemporalNext.STLNext;
import net.maswag.falcaun.TemporalOr.STLOr;
import net.maswag.falcaun.TemporalNot.STLNot;
import net.maswag.falcaun.TemporalRelease.STLRelease;
import net.maswag.falcaun.TemporalSub.STLSub;
import net.maswag.falcaun.TemporalUntil.STLUntil;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static net.maswag.falcaun.STLAbstractAtomic.Operation.*;
import static org.junit.jupiter.api.Assertions.*;

class STLParserTest {

    @Test
    void atomic() {
        List<String> inputs = Arrays.asList(
                "signal(0) < 10.0",
                "signal(10) > 4.2",
                "signal(2) != -0.2",
                "signal(1) == -20");
        List<STLOutputAtomic> expectedList = Arrays.asList(
                new STLOutputAtomic(0, lt, 10.0),
                new STLOutputAtomic(10, gt, 4.2),
                new STLOutputAtomic(2, ne, -0.2),
                new STLOutputAtomic(1, eq, -20));

        for (int i = 0; i < inputs.size(); i++) {
            CharStream stream = CharStreams.fromString(inputs.get(i));
            net.maswag.falcaun.STLLexer lexer = new net.maswag.falcaun.STLLexer(stream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            net.maswag.falcaun.STLParser parser = new net.maswag.falcaun.STLParser(tokens);
            ParseTree tree = parser.atomic();
            net.maswag.falcaun.STLVisitor<STLCost> visitor = new STLVisitorImpl();

            assertEquals(expectedList.get(i).toString(), visitor.visit(tree).toString());
        }
    }

    @Nested
    class Expr {
        private List<String> inputs;
        private List<STLCost> expectedList;

        @BeforeEach
        void setUp() {
            inputs = Arrays.asList(
                    "signal(0) < 10.0",
                    "signal(1) > 4.2",
                    "signal(1) == -20",
                    "(signal(0) < 10.0) || (signal(1) == 2.2)",
                    "(signal(0) < 10.0) || (signal(1) == 2.2 && signal(0) > 10.0)",
                    "(signal(0) < -1.0) -> (signal(1) == 2.2)",
                    "X (signal(1) == -20)",
                    "[] (signal(1) == -20)",
                    "<> (signal(1) == -20)",
                    "alw_[0,2] (signal(1) == -20)",
                    "ev_[10,20] (signal(1) == -20)",
                    "[] ((signal(2) != 4 && X (signal(2) == 4)) -> []_[0,1] (signal(2) == 4))",
                    "[] ((signal(2) == 3) -> signal(0) > 20)", // S2
                    "alw((signal(1) < 4770) || (X (signal(1) > 600)))", // S5
                    "(signal(0) > 100) U (signal(1) < 20)", // Until
                    "(signal(0) > 100) R (signal(1) < 20)", // Release
                    "(!(signal(0) > 0)) || (alw_[0,5] signal(1) < 15)"
            );
            expectedList = Arrays.asList(
                    new STLOutputAtomic(0, lt, 10.0),
                    new STLOutputAtomic(1, gt, 4.2),
                    new STLOutputAtomic(1, eq, -20),
                    new STLOr(new STLOutputAtomic(0, lt, 10.0),
                            new STLOutputAtomic(1, eq, 2.2)),
                    new STLOr(new STLOutputAtomic(0, lt, 10.0),
                            new STLAnd(new STLOutputAtomic(1, eq, 2.2),
                                    new STLOutputAtomic(0, gt, 10.0))),
                    new STLImply(new STLOutputAtomic(0, lt, -1.0),
                            new STLOutputAtomic(1, eq, 2.2)),
                    new STLNext(new STLOutputAtomic(1, eq, -20), true),
                    new STLGlobally(new STLOutputAtomic(1, eq, -20)),
                    new STLEventually(new STLOutputAtomic(1, eq, -20)),
                    new STLSub(
                            new STLGlobally(new STLOutputAtomic(1, eq, -20)), 0, 2),
                    new STLSub(
                            new STLEventually(new STLOutputAtomic(1, eq, -20)), 10, 20),
                    new STLGlobally(new STLImply(
                            new STLAnd(
                                    new STLOutputAtomic(2, ne, 4.0),
                                    new STLNext(new STLOutputAtomic(2, eq, 4.0), false)),
                            new STLSub(new STLGlobally(new STLOutputAtomic(2, eq, 4.0)), 0, 1))),
                    // S2
                    new STLGlobally(new STLImply(new STLOutputAtomic(2, STLOutputAtomic.Operation.eq, 3),
                            new STLOutputAtomic(0, STLOutputAtomic.Operation.gt, 20))),
                    // S5
                    new STLGlobally(
                            new STLOr(
                                    new STLOutputAtomic(1, STLOutputAtomic.Operation.lt, 4770),
                                    new STLNext(new STLOutputAtomic(1, STLOutputAtomic.Operation.gt, 600.0), true))),
                    new STLUntil(new STLOutputAtomic(0, gt, 100), new STLOutputAtomic(1, lt, 20)),
                    new STLRelease(new STLOutputAtomic(0, gt, 100), new STLOutputAtomic(1, lt, 20)),
                    new STLOr(new STLNot(new STLOutputAtomic(0, gt, 0)), new STLSub(new STLGlobally(new STLOutputAtomic(1, lt, 15)), 0, 5))
            );

            assert inputs.size() == expectedList.size();
        }

        @Test
        void withoutMapper() {
            STLFactory factory = new STLFactory();
            for (int i = 0; i < inputs.size(); i++) {
                STLCost result = factory.parse(inputs.get(i));
                assertFalse(result.isInitialized());
                assertThrows(IllegalStateException.class, result::toAbstractString);
                assertEquals(expectedList.get(i).toString(), result.toString());
            }
        }

        @Test
        void withMapper() {
            STLFactory factory = new STLFactory();
            Map<Character, Double> velocityMap = new HashMap<>();
            velocityMap.put('a', -1.0);
            velocityMap.put('b', 10.0);

            Map<Character, Double> rotationMap = new HashMap<>();
            rotationMap.put('a', -20.0);
            rotationMap.put('b', 2.2);
            rotationMap.put('c', 4.2);

            Map<Character, Double> gearMap = new HashMap<>();

            List<Map<Character, Double>> inputMapper = Collections.emptyList();
            List<Map<Character, Double>> outputMapper = Arrays.asList(velocityMap, rotationMap, gearMap);
            List<Character> largest = Arrays.asList('c', 'd', 'a');

            for (int i = 0; i < inputs.size(); i++) {
                STLCost result = factory.parse(inputs.get(i), inputMapper, outputMapper, largest);
                assertTrue(result.isInitialized());
                Assertions.assertThat(result.toAbstractString()).contains("output == ");
                assertEquals(expectedList.get(i).toString(), result.toString());
            }
        }
    }

    @Test
    void Derivative(){
        List<String> inputs = Arrays.asList(
        "signal(0) < 10.0",
            "signal(1) > 4.2",
            "signal(1) == -20"
        );

        STLCost f = new STLConst(false);
        STLCost t = new STLConst(true);
        List<STLCost> expectedList = Arrays.asList(
            t, t, t, t, t, t, t, t, f, f, f, f,
            f, f, f, t, f, f, f, t, f, f, f, t,
            t, f, f, f, t, f, f, f, t, f, f, f
        );

        STLFactory factory = new STLFactory();
        Map<Character, Double> velocityMap = new HashMap<>();
        velocityMap.put('a', -1.0);
        velocityMap.put('b', 10.0);

        Map<Character, Double> rotationMap = new HashMap<>();
        rotationMap.put('a', -20.0);
        rotationMap.put('b', 2.2);
        rotationMap.put('c', 4.2);

        Map<Character, Double> gearMap = new HashMap<>();

        List<Map<Character, Double>> inputMapper = Collections.emptyList();
        List<Map<Character, Double>> outputMapper = Arrays.asList(velocityMap, rotationMap, gearMap);
        List<Character> largest = Arrays.asList('c', 'd', 'a');

        List<String> inputStrings = Arrays.asList("aaa", "aba", "aca", "ada", "baa", "bba", "bca", "bda", "caa", "cba", "cca", "cda");
        for (int i = 0; i < inputs.size(); i++) {
            int j = 0;
            STLCost result = factory.parse(inputs.get(i), inputMapper, outputMapper, largest);
            for (String input: inputStrings){
                STLCost expected = expectedList.get(12*i+j);
                assert(result.derivativeOn(input).semanticallyEquals(expected));
                j++;
            }
        }
    }

    @Test
    void Mapper(){
        List<String> formulas = Arrays.asList(
        "signal(0) < 10.0",
            "signal(1) > 4.2",
            "signal(1) == -20",
            "(signal(0) < 10.0) || (signal(1) == 2.2)",
            "(signal(0) < -1.0) -> (signal(1) == 2.2)",
            "X (signal(1) == -20)",
            "[] (signal(1) == -20)",
            "<> (signal(1) == -20)",
            "alw_[0,2] (signal(1) == -20)",
            "ev_[10,20] (signal(1) == -20)",
            "[] ((signal(2) != 4 && X (signal(2) == 4)) -> []_[0,1] (signal(2) == 4))",
                "[] ((signal(2) == 4) -> signal(0) > 10)", // S2
            "alw((signal(1) < 4.2) || (X (signal(1) > 4.2)))", // S5
            "(signal(0) > 10) U (signal(1) < 4.2)", // Until
            "(signal(0) > 10) R (signal(1) < 4.2)", // Release
            "(!(signal(0) > -1.0)) || (alw_[0,5] signal(1) < 4.2)"
        );

        STLFactory factory = new STLFactory();
        Map<Character, Double> velocityMap = new HashMap<>();
        velocityMap.put('a', -1.0);
        velocityMap.put('b', 10.0);

        Map<Character, Double> rotationMap = new HashMap<>();
        rotationMap.put('a', -20.0);
        rotationMap.put('b', 2.2);
        rotationMap.put('c', 4.2);

        Map<Character, Double> gearMap = new HashMap<>();
        gearMap.put('a', 4.0);

        List<Map<Character, Double>> inputMapper = Collections.emptyList();
        List<Map<Character, Double>> outputMapper = Arrays.asList(velocityMap, rotationMap, gearMap);
        List<Character> largest = Arrays.asList('c', 'd', 'b');
        SignalMapper sigMap = new ExtendedSignalMapper();

        List<List<String>> expectedList = Arrays.asList(
            Arrays.asList("aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "caa", "caa", "caa", "caa",
                               "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "caa", "caa", "caa", "caa"),
            Arrays.asList("aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada",
                               "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada"),
            Arrays.asList("aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba",
                               "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba"),
            Arrays.asList("aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "caa", "aaa", "caa", "caa",
                               "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "caa", "aaa", "caa", "caa"),
            Arrays.asList("aaa", "aba", "aaa", "aaa", "aba", "aba", "aba", "aba", "aba", "aba", "aba", "aba",
                               "aaa", "aba", "aaa", "aaa", "aba", "aba", "aba", "aba", "aba", "aba", "aba", "aba"),
            Arrays.asList("aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba",
                               "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba"),
            Arrays.asList("aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba",
                               "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba"),
            Arrays.asList("aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba",
                               "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba"),
            Arrays.asList("aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba",
                               "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba"),
            Arrays.asList("aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba",
                               "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba", "aaa", "aba", "aba", "aba"),
            Arrays.asList("aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa",
                               "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab"),
            Arrays.asList("aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aaa", "aab", "aab", "aab", "aab",
                               "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab", "aab"),
            Arrays.asList("aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada",
                               "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada"),
            Arrays.asList("aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "cda",
                               "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "cda"),
            Arrays.asList("aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "caa", "caa", "caa", "ada",
                               "aaa", "aaa", "aaa", "ada", "aaa", "aaa", "aaa", "ada", "caa", "caa", "caa", "ada"),
            Arrays.asList("aaa", "aaa", "aaa", "ada", "baa", "baa", "baa", "bda", "baa", "baa", "baa", "bda",
                               "aaa", "aaa", "aaa", "ada", "baa", "baa", "baa", "bda", "baa", "baa", "baa", "bda")
        );

        List<String> inputStrings = Arrays.asList("aaa", "aba", "aca", "ada", "baa", "bba", "bca", "bda", "caa", "cba", "cca", "cda");
        List<IOSignalPiece<List<Double>>> inputs = Arrays.asList(
                new IOSignalPiece(null, Arrays.asList(-1.0, -20.0, 0.)),
                new IOSignalPiece(null, Arrays.asList(-1.0, 2.2, 0.)),
                new IOSignalPiece(null, Arrays.asList(-1.0, 4.2, 0.)),
                new IOSignalPiece(null, Arrays.asList(-1.0, 20.0, 0.)),
                new IOSignalPiece(null, Arrays.asList(10.0, -20.0, 0.)),
                new IOSignalPiece(null, Arrays.asList(10.0, 2.2, 0.)),
                new IOSignalPiece(null, Arrays.asList(10.0, 4.2, 0.)),
                new IOSignalPiece(null, Arrays.asList(10.0, 20.0, 0.)),
                new IOSignalPiece(null, Arrays.asList(20.0, -20.0, 0.)),
                new IOSignalPiece(null, Arrays.asList(20.0, 2.2, 0.)),
                new IOSignalPiece(null, Arrays.asList(20.0, 4.2, 0.)),
                new IOSignalPiece(null, Arrays.asList(20.0, 20.0, 0.)),
                new IOSignalPiece(null, Arrays.asList(-1.0, -20.0, 5.)),
                new IOSignalPiece(null, Arrays.asList(-1.0, 2.2, 5.)),
                new IOSignalPiece(null, Arrays.asList(-1.0, 4.2, 5.)),
                new IOSignalPiece(null, Arrays.asList(-1.0, 20.0, 5.)),
                new IOSignalPiece(null, Arrays.asList(10.0, -20.0, 5.)),
                new IOSignalPiece(null, Arrays.asList(10.0, 2.2, 5.)),
                new IOSignalPiece(null, Arrays.asList(10.0, 4.2, 5.)),
                new IOSignalPiece(null, Arrays.asList(10.0, 20.0, 5.)),
                new IOSignalPiece(null, Arrays.asList(20.0, -20.0, 5.)),
                new IOSignalPiece(null, Arrays.asList(20.0, 2.2, 5.)),
                new IOSignalPiece(null, Arrays.asList(20.0, 4.2, 5.)),
                new IOSignalPiece(null, Arrays.asList(20.0, 20.0, 5.))
        );
        for (int i = 0; i < formulas.size(); i++) {
            System.out.println(i);
            STLCost formula = factory.parse(formulas.get(i), inputMapper, outputMapper, largest);
            NumericOutputMapper nom = new NumericOutputMapper(inputMapper, largest, outputMapper, sigMap, Arrays.asList(formula));
            OutputEquivalence oe = new OutputEquivalence(inputMapper, largest, outputMapper, sigMap, Arrays.asList(formula), false);
            List<String> expected = expectedList.get(i);
            for (int j = 0; j < expected.size(); j++){
                assertEquals(expected.get(j), nom.mapOutput(inputs.get(j)));
                assertEquals(expected.get(j), oe.mapOutput(inputs.get(j)));
            }
        }
    }
}
