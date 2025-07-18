package net.maswag.falcaun;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import owl.ltl.parser.LtlParser;
import owl.ltl.rewriter.NormalForms;

public class Derivatives {

    private static <I> boolean memberOf(List<TemporalLogic<I>> formulaList, TemporalLogic<I> formula){
        long a = System.currentTimeMillis();
        // for (int i = 0; i < formulaList.size(); i++){
        //     if (formulaList.get(i).semanticallyEquals(formula)){
        //         System.out.println(System.currentTimeMillis() - a);
        //         return true;
        //     }
        // }
        if (formulaList.stream().parallel().anyMatch(f -> f.semanticallyEquals(formula))){
            // System.out.println(System.currentTimeMillis() - a);
             return true;
        }
        // System.out.println(System.currentTimeMillis() - a);
        return false;
    }

    public static <I> List<TemporalLogic<I>> getAllDerivatives(TemporalLogic<I> formula, Alphabet<String> sigma){
        List<TemporalLogic<I>> result = new ArrayList<TemporalLogic<I>>();
        List<TemporalLogic<I>> beforeChecked = new ArrayList<TemporalLogic<I>>();
        Set<Integer> checked = new HashSet<Integer>();
        // result.add(new TemporalConst<I>(false));
        // result.add(new TemporalConst<I>(true));
        result.add(formula);
        beforeChecked.add(formula);
        int count = 0; // for debug
        while(!beforeChecked.isEmpty()){
            TemporalLogic<I> nextFormula = beforeChecked.get(0);
            for (int i = 0; i < sigma.size(); i++){
                String nextInput = sigma.getSymbol(i);
                try {
                    TemporalLogic<I> derivative = nextFormula.derivativeOn(nextInput);
                    owl.ltl.Formula dnfFormula = NormalForms.toDnfFormula(LtlParser.parse(derivative.toOwlString()).formula());
                    if (checked.contains(dnfFormula.hashCode())){
                        continue;
                    }
                    if (!memberOf(result, derivative)){
                        result.add(derivative);
                        beforeChecked.add(derivative);
                        // System.out.println(derivative.toOwlString());
                        // System.out.println(result.size());
                    }
                    count++;
                    checked.add(dnfFormula.hashCode());
                } catch (NullPointerException ex){
                    continue;
                }
            }
            beforeChecked.remove(0);
        }
        System.out.println("# of checked formulas: " + count);
        return result;
    } 

    public Alphabet<List<Double>> constructCIAlphabet(List<List<Double>> concreteInput){
        List<List<Double>> result = new ArrayList<>();
        for (int i = 0; i < concreteInput.size(); i++){
            List<List<Double>> nextList = new ArrayList<>();
            if (result.isEmpty()){
                for (int j = 0; j < concreteInput.get(i).size(); j++){
                    List<Double> tmp = new ArrayList<Double>();
                    tmp.add(concreteInput.get(i).get(j));
                    nextList.add(tmp);
                }
                
            } else {
                for (int j = 0; j < result.size(); j++){
                    for (int k = 0; k < concreteInput.get(i).size(); k++){
                        List<Double> tmp = new ArrayList<Double>(result.get(j));
                        tmp.add(concreteInput.get(i).get(k));
                        nextList.add(tmp);
                    }
                }
                
            }
            result = nextList;
        }
        return Alphabets.fromList(result);
    }

}
