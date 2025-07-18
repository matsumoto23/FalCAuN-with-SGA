package net.maswag.falcaun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>STLAtomic class.</p>
 *
 * @author Masaki Waga {@literal <masakiwaga@gmail.com>}
 */
public class STLOutputAtomic extends STLAbstractAtomic {
    private final List<List<Character>> abstractOutputs = new ArrayList<>();
    private final List<List<Double>> concreteOutputs = new ArrayList<>();
    private List<Character> largest;
    private List<Map<Character, Double>> outputMapper;

    /**
     * <p>Constructor for STLAtomic.</p>
     *
     * @param sigIndex   a int.
     * @param op         a {@link Operation} object.
     * @param comparator a double.
     */
    public STLOutputAtomic(int sigIndex, Operation op, double comparator) {
        super(sigIndex, op, comparator);
        iOType = IOType.OUTPUT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllAPs() {
        return getAllAPs(abstractOutputs, largest);
    }

    @Override
    public void constructSatisfyingAtomicPropositions() {
        super.constructSatisfyingAtomicPropositions();
        constructAtomicStrings(concreteOutputs, abstractOutputs, largest);
    }

    private void setOutputMaps() {
        STLAbstractAtomic.decomposeMapList(outputMapper, abstractOutputs, concreteOutputs);
        initialized = true;
    }

    void setAtomic(List<Map<Character, Double>> outputMapper, List<Character> largest) {
        this.outputMapper = outputMapper;
        this.largest = largest;
        setOutputMaps();
    }

    void setOutputMapper(List<Map<Character, Double>> outputMapper) {
        this.outputMapper = outputMapper;
        setOutputMaps();
    }

    void setLargest(List<Character> largest) {
        this.largest = largest;
        setOutputMaps();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoSI getRoSI(IOSignal<List<Double>> signal) {
        return getRoSISingle(signal.getOutputSignal());
    }

    @Override
    protected String getSignalName() {
        return "output";
    }

    public STLCost derivativeOn(String s){
        char ap = s.toCharArray()[sigIndex];
        if (ap != largest.get(sigIndex)){
            Map<Character, Double> map = outputMapper.get(sigIndex);
            Double signal = map.get(ap);
            switch (op) {
                case lt:
                    return new TemporalConst.STLConst(signal <= comparator);
                case gt:
                    return new TemporalConst.STLConst(signal > comparator);
                case eq:
                    return new TemporalConst.STLConst(signal == comparator);
                case ne:
                    return new TemporalConst.STLConst(signal != comparator);
                default:
                    return null;
            }
        } else {
            switch (op) {
                case lt:
                    return new TemporalConst.STLConst(false);
                case gt:
                    return new TemporalConst.STLConst(true);
                case eq:
                    return new TemporalConst.STLConst(false);
                case ne:
                    return new TemporalConst.STLConst(true);
                default:
                    return null;
            }
        }
    }

    public STLCost toNnf(boolean negate){
        if (negate){
            return new TemporalNot.STLNot(this);
        } else {
            return this;
        }
    }

    public STLCost toDisjunctiveForm(){
        return this;
    }

    public List<TemporalLogic<List<Double>>> getAllConjunctions(){
        return new ArrayList<>();
    }
}
