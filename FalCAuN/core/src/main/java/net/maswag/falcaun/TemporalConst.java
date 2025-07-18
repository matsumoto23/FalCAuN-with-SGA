package net.maswag.falcaun;

import java.util.*;

public class TemporalConst<I> extends AbstractTemporalLogic<I>{
    private final boolean condition;
    TemporalConst(boolean b){
        this.condition = b;
        this.iOType = IOType.BOTH;

    }

    public String toAbstractString(){
        if (condition) { return "true";}
        else {return "false";}
    };

    public String toAbstractString(OutputEquivalence mapper){
        if (condition) { return "true";}
        else {return "false";}
    }

    public String toString(){
        return toAbstractString();
    }

    public String toOwlString(){
        if (condition) { return "true"; }
        else { return "false"; }
    }

    /**
     * <p>getRoSI.</p>
     *
     * @return a {@link RoSI} object.
     */
    public RoSI getRoSI(IOSignal<I> signal){
        if (condition) { return new RoSI(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY); }
        else { return new RoSI(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY); }
    };

    public Set<String> getAllAPs(){
        return new HashSet<String>();
    };

    public TemporalLogic<I> derivativeOn(String c){
        return this;
    };

    public TemporalLogic<I> toNnf(boolean negate){
        if (negate){
            return new TemporalConst<I>(!condition);
        } else {
            return new TemporalConst<I>(condition);
        }
    }

    public TemporalLogic<I> toDisjunctiveForm(){
        return this;
    }

    public List<TemporalLogic<I>> getAllConjunctions(){
        return new ArrayList<>();
    }

    static class STLConst extends TemporalConst<List<Double>> implements STLCost {
        STLConst(boolean b) {
            super(b);
        }
    }
    
    static class LTLConst extends TemporalConst<String> implements LTLFormula {
        LTLConst(boolean b) {
            super(b);
        }
    }
}