package net.maswag.falcaun;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.maswag.falcaun.TemporalAnd.LTLAnd;
import net.maswag.falcaun.TemporalConst.LTLConst;
import net.maswag.falcaun.TemporalEventually.LTLEventually;
import net.maswag.falcaun.TemporalGlobally.LTLGlobally;
import net.maswag.falcaun.TemporalImply.LTLImply;
import net.maswag.falcaun.TemporalLogic.LTLFormula;
import net.maswag.falcaun.TemporalNext.LTLNext;
import net.maswag.falcaun.TemporalNot.LTLNot;
import net.maswag.falcaun.TemporalOr.LTLOr;
import net.maswag.falcaun.TemporalRelease.LTLRelease;
import net.maswag.falcaun.TemporalSub.LTLSub;
import net.maswag.falcaun.TemporalUntil.LTLUntil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LTLParserTest {
    LTLFactory factory = new LTLFactory();

    @Test
    void atomic() {
        List<String> inputs = Arrays.asList(
                "input == a", "(input == b)",
                "output == p", "output == q");
        List<LTLAtomic> expectedList = Arrays.asList(
                new LTLAtomic(Optional.of("a"), Optional.empty()),
                new LTLAtomic(Optional.of("b"), Optional.empty()),
                new LTLAtomic(Optional.empty(), Optional.of("p")),
                new LTLAtomic(Optional.empty(), Optional.of("q")));

        for (int i = 0; i < inputs.size(); i++) {
            LTLFormula result = factory.parse(inputs.get(i));

            assertEquals(expectedList.get(i).toString(), result.toString());
        }
    }

    @Test
    void expr() {
        List<String> inputs = Arrays.asList(
                "input == a",
                "output == p",
                "input == a || output == p",
                "input == a -> output == p",
                "input == a && output == p",
                "X (input == a)",
                "[] (output == p)",
                "<> (input == a)",
                "alw_[0,2] (output == p)",
                "ev_[10,20] (input == a)",
                "(input == a) U (output == p)", // Until
                "(input == a) R (output == p)" // Release
        );
        LTLAtomic left = new LTLAtomic(Optional.of("a"), Optional.empty());
        LTLAtomic right = new LTLAtomic(Optional.empty(), Optional.of("p"));
        List<LTLFormula> expectedList = Arrays.asList(
                left, right,
                new LTLOr(left, right),
                new LTLImply(left, right),
                new LTLAnd(left, right),
                new LTLNext(left, true),
                new LTLGlobally(right),
                new LTLEventually(left),
                new LTLSub(new LTLGlobally(right), 0, 2),
                new LTLSub(new LTLEventually(left), 10, 20),
                new LTLUntil(left, right),
                new LTLRelease(left, right)
        );

        assert inputs.size() == expectedList.size();

        for (int i = 0; i < inputs.size(); i++) {
            LTLFormula result = factory.parse(inputs.get(i));
            assertEquals(expectedList.get(i).toString(), result.toString());
        }
    }

    @Test
    void toStringTest() {
        List<String> inputs = Arrays.asList(
                "input == a ",
                "output == p ",
                "input == a  || output == p ",
                "input == a  || input == a ",
                "( input == a  ) -> ( output == p  )",
                "input == a  && output == p ",
                "X ( input == a  )",
                "[] ( output == p  )",
                "<> ( input == a  )",
                " []_[0, 2] ( output == p  )",
                " <>_[10, 20] ( input == a  )",
                "input == a  U output == p ", // Until
                "input == a  R output == p " // Release
        );
        LTLAtomic left = new LTLAtomic(Optional.of("a"), Optional.empty());
        LTLAtomic right = new LTLAtomic(Optional.empty(), Optional.of("p"));
        List<LTLFormula> expectedList = Arrays.asList(
                left, right,
                new LTLOr(left, right),
                new LTLOr(left, left),
                new LTLImply(left, right),
                new LTLAnd(left, right),
                new LTLNext(left, true),
                new LTLGlobally(right),
                new LTLEventually(left),
                new LTLSub(new LTLGlobally(right), 0, 2),
                new LTLSub(new LTLEventually(left), 10, 20),
                new LTLUntil(left, right),
                new LTLRelease(left, right)
        );

        assert inputs.size() == expectedList.size();

        for (String input : inputs) {
            LTLFormula result = factory.parse(input);
            assertEquals(input, result.toString().replaceAll("\"", ""));
        }
    }

    @Test
    void toAbstractStringTest() {
        List<String> inputs = Arrays.asList(
                "input == a ",
                "output == p ",
                "( input == a  ) || ( output == p  )",
                "input == a ",
                "( input == a  ) -> ( output == p  )",
                "( input == a  ) && ( output == p  )",
                "X ( input == a  )",
                "[] ( output == p  )",
                "<> ( input == a  )",
                //" []_[0, 2] ( output == p  )",
                //" <>_[1, 3] ( input == a  )",
                "( input == a  ) U ( output == p  )", // Until
                "( input == a  ) R ( output == p  )" // Release
        );
        LTLAtomic left = new LTLAtomic(Optional.of("a"), Optional.empty());
        LTLAtomic right = new LTLAtomic(Optional.empty(), Optional.of("p"));
        List<LTLFormula> expectedList = Arrays.asList(
                left, right,
                new LTLOr(left, right),
                new LTLOr(left, left),
                new LTLImply(left, right),
                new LTLAnd(left, right),
                new LTLNext(left, true),
                new LTLGlobally(right),
                new LTLEventually(left),
                //new LTLSub(new LTLGlobally(right), 0, 2),
                //new LTLSub(new LTLEventually(left), 10, 20),
                new LTLUntil(left, right),
                new LTLRelease(left, right)
        );

        assert inputs.size() == expectedList.size();

        for (String input : inputs) {
            LTLFormula result = factory.parse(input);
            assertEquals(input, result.toAbstractString().replaceAll("\"", ""));
        }
    }

    @Test
    void derivative(){
        LTLConst top = new LTLConst(true);
        LTLConst bot = new LTLConst(false);
        LTLAtomic left = new LTLAtomic(Optional.of("a"), Optional.empty());
        LTLAtomic right = new LTLAtomic(Optional.empty(), Optional.of("p"));
        List<LTLFormula> inputs = Arrays.asList(
                top, bot, left, right,
                new LTLOr(left, right),
                new LTLImply(left, right),
                new LTLAnd(left, right),
                new LTLNext(left, true),
                new LTLGlobally(right),
                new LTLEventually(left),
                new LTLSub(new LTLGlobally(right), 0, 2),
                new LTLSub(new LTLEventually(left), 10, 20),
                new LTLUntil(left, right),
                new LTLRelease(left, right)
        );
        List<LTLFormula> expectedList = Arrays.asList( //derivatives of inputs on "p"
                top, bot, bot, top,
                new LTLOr(bot, top),
                new LTLOr(new LTLNot(bot), top),
                new LTLAnd(bot, top),
                left,
                new LTLAnd(top, new LTLGlobally(right)),
                new LTLOr(bot, new LTLEventually(left)),
                new LTLAnd (top, new LTLSub(new LTLGlobally(right), 0, 1)), 
                new LTLSub(new LTLEventually(left), 9, 19),
                new LTLOr(top, new LTLAnd(bot, new LTLUntil(left, right))),
                new LTLOr(new LTLAnd(bot, top), new LTLAnd(top, new LTLRelease(left, right)))
        );
        List<TemporalLogic<String>> derivatives = inputs.stream().map(f -> f.derivativeOn("p")).collect(Collectors.toList());
        for (int i = 0; i < derivatives.size(); i++) {
            TemporalLogic<String> result = derivatives.get(i);
            assertEquals(expectedList.get(i).toString(), result.toString());
        }
    }

    @Test
    void all_derivatives(){
        List<String> inputs = Arrays.asList(
                "output == p",
                "output == p || output == q",
                "output == p -> output == q",
                "output == p && output == q",
                "X (output == p)",
                "[] (output == p)",
                "<> (output == p)",
                "alw_[0,2] (output == p)",
                "ev_[3,5] (output == p)",
                "(output == p) U (output == q)", // Until
                "(output == p) R (output == q)" // Release 
        );
        LTLFormula top = new LTLConst(true);
        LTLFormula bot = new LTLConst(false);
        LTLFormula p = new LTLAtomic(Optional.empty(), Optional.of("p"));
        LTLFormula q = new LTLAtomic(Optional.empty(), Optional.of("q"));
        TemporalGlobally<String> Gp = new LTLGlobally(p);
        TemporalEventually<String> Fp = new LTLEventually(p);


        List<List<LTLFormula>> expectedList = Arrays.asList(
            Arrays.asList(p, top, bot),
            Arrays.asList(new LTLOr(p, q), top, bot),
            Arrays.asList(new LTLImply(p, q), bot, top),
            Arrays.asList(new LTLAnd(p, q), bot),
            Arrays.asList(new LTLNext(p, true), p, top, bot),
            Arrays.asList(new LTLGlobally(p), bot),
            Arrays.asList(new LTLEventually(p), top),
            Arrays.asList(new LTLSub(Gp, 0, 2),
                          new LTLSub(Gp, 0, 1),
                          bot,
                          new LTLSub(Gp, 0, 0),
                          top),
            Arrays.asList(new LTLSub(Fp, 3, 5),
                          new LTLSub(Fp, 2, 4),
                          new LTLSub(Fp, 1, 3),
                          new LTLSub(Fp, 0, 2),
                          top,
                          new LTLSub(Fp, 0, 1),
                          new LTLSub(Fp, 0, 0),
                          bot),
            Arrays.asList(new LTLUntil(p, q), top, bot),
            Arrays.asList(new LTLRelease(p, q),
                          bot)
        );
        
        Derivatives deriv = new Derivatives();
        Alphabet<String> gamma = Alphabets.fromList(Arrays.asList("p", "q", "r"));
        List<List<TemporalLogic<String>>> derivatives = 
            inputs.stream().map(f -> deriv.getAllDerivatives(factory.parse(f), gamma)).collect(Collectors.toList());
        
        assert derivatives.size() == expectedList.size();
        for (int i = 0; i < derivatives.size(); i++){
            for (int j = 0; j < derivatives.get(i).size(); j++){
                assert (derivatives.get(i).get(j).semanticallyEquals(expectedList.get(i).get(j)));
            }
        }
    }
}
