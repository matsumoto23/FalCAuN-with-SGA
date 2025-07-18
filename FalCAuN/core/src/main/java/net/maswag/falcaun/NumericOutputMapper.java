package net.maswag.falcaun;

import java.util.Map;
import java.util.stream.Collectors;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;


public class NumericOutputMapper extends NumericSULMapper {
    private Map<String, String> postOutputMapper;
    private List<TemporalLogic.STLCost> formulaList;

    public NumericOutputMapper(List<Map<Character, Double>> inputMapper,
                                List<Character> largestOutputs, List<Map<Character, Double>> outputMapper,
                                SignalMapper sigMap,    List<TemporalLogic.STLCost> formulaList){
        super(inputMapper, largestOutputs, outputMapper, sigMap);
        this.formulaList = formulaList;
        this.postOutputMapper = getOutputMapper(formulaList);
    }

    public void changeOutputMapper(){
        this.postOutputMapper = getMedOutputMapper(formulaList);
        return;
    }

    private Map<String, String> getOutputMapper(List<TemporalLogic.STLCost> formulaList){
        Alphabet<String> gamma = constructAbstractAPs(abstractOutputs);
        List<TemporalLogic<List<Double>>> derivatives = new ArrayList<>();
        for (TemporalLogic<List<Double>> formula: formulaList){
            derivatives.addAll(Derivatives.getAllDerivatives(formula, gamma));
        }
        // System.out.println(derivatives.size());
        Map<String, String> mapper = new HashMap<>();
        for (int i = 0; i < gamma.size(); i++){
            String o1 = gamma.getSymbol(i);
            for (int j = 0; j < i; j++){
                String o2 = gamma.getSymbol(j);
                boolean equal = true; // check if o1 is equivalent to o2
                for (TemporalLogic<List<Double>> formula : derivatives){ // for each phi in derivatives, check D_{o1}(phi) is equivalent to D_{o2}(phi)
                    TemporalLogic<List<Double>> derivative1 = formula.derivativeOn(o1);
                    TemporalLogic<List<Double>> derivative2 = formula.derivativeOn(o2);
                    equal = equal &&  derivative1.semanticallyEquals(derivative2);
                    if (!equal){ break; }
                }
                if(equal){ // if o1 and o2 are equivalent, map o1 to o2 
                    mapper.put(o1, o2);
                    System.out.println(o1 + " is mapped to " + o2);
                    break;
                }
            }
            mapper.putIfAbsent(o1, o1);
        }
        return mapper;
    }

    private Map<String, String> getMedOutputMapper(List<TemporalLogic.STLCost> formulaList){
        Alphabet<String> gamma = constructAbstractAPs(abstractOutputs);
        List<TemporalLogic<List<Double>>> derivatives = new ArrayList<>();
        for (TemporalLogic<List<Double>> formula: formulaList){
            List<TemporalLogic<List<Double>>> conjunctions = formula.toNnf(false).toDisjunctiveForm().getAllConjunctions();
            for (TemporalLogic<List<Double>> conjunction: conjunctions){
                derivatives.addAll(Derivatives.getAllDerivatives(conjunction, gamma));
            }
        }
        Map<String, String> mapper = new HashMap<>();
        for (int i = 0; i < gamma.size(); i++){
            String o1 = gamma.getSymbol(i);
            for (int j = 0; j < i; j++){
                String o2 = gamma.getSymbol(j);
                boolean equal = true; // check if o1 is equivalent to o2
                for (TemporalLogic<List<Double>> formula : derivatives){ // for each phi in derivatives, check D_{o1}(phi) is equivalent to D_{o2}(phi)
                    TemporalLogic<List<Double>> derivative1 = formula.derivativeOn(o1);
                    TemporalLogic<List<Double>> derivative2 = formula.derivativeOn(o2);
                    equal = equal &&  derivative1.semanticallyEquals(derivative2);
                    if (!equal){ break; }
                }
                if(equal){ // if o1 and o2 are equivalent, map o1 to o2 
                    mapper.put(o1, o2);
                    System.out.println(o1 + " is mapped to " + o2);
                    break;
                }
            }
            mapper.putIfAbsent(o1, o1);
        }
        return mapper;
    }    

    public String mapOutput(IOSignalPiece<List<Double>> concreteIO) {
        String mappedOutput = super.mapOutput(concreteIO);
        return postOutputMapper.get(mappedOutput);
    }

    private Alphabet<String> constructAbstractAPs(List<List<Character>> abstractOutputs){
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < abstractOutputs.size(); i++){
            List<Character> abstractOutputi = new ArrayList<Character>(abstractOutputs.get(i));
            abstractOutputi.add(largestOutputs.get(i));
            List<String> tmpList = new ArrayList<String>();
            if (result.isEmpty()){
                tmpList = abstractOutputi.stream().map(c -> String.valueOf(c)).collect(Collectors.toList());
            } else {
                for (String s: result){
                    for ( Character c: abstractOutputi){
                        tmpList.add(s + c);
                    }
                }
            }
            result = tmpList;
        }
        return Alphabets.fromList(result);
    }
}
