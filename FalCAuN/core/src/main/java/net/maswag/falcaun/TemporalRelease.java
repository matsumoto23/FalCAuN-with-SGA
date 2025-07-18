package net.maswag.falcaun;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;

/**
 * <p>STLRelease class.</p>
 *
 * @author Masaki Waga {@literal <masakiwaga@gmail.com>}
 * @param <I> Type of the input at each step
 */
@Getter
public class TemporalRelease<I> extends AbstractTemporalLogic<I> {
    private final TemporalLogic<I> left, right;

    TemporalRelease(TemporalLogic<I> left, TemporalLogic<I> right) {
        this.left = left;
        this.right = right;
        this.nonTemporal = false;
        this.iOType = left.getIOType().merge(right.getIOType());
        this.initialized = left.isInitialized() && right.isInitialized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoSI getRoSI(IOSignal<I> signal) {
        return getRoSIRaw(signal).assignMax(new RoSI(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    public RoSI getRoSIRaw(IOSignal<I> signal) {
        RoSI result = new RoSI(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        for (int i = 0; i < signal.length(); i++) {
            RoSI nextRoSI = this.right.getRoSI(signal.subWord(i));
            RoSI globalRoSI = signal.prefixes(true).stream().sorted((left, right) ->
                    right.length() - left.length()).limit(i + 1).map(this.left::getRoSI).filter(Objects::nonNull).reduce(nextRoSI, RoSI::max);
            result.assignMin(globalRoSI);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.left + " R " + this.right;
    }

    public String toOwlString() {
        return "( " + this.left.toOwlString() + " ) R ( " + this.right.toOwlString() + " )";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void constructSatisfyingAtomicPropositions() {
        super.constructSatisfyingAtomicPropositions();
        this.satisfyingAtomicPropositions = null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllAPs() {
        Set<String> allAPs = this.left.getAllAPs();
        allAPs.addAll(this.right.getAllAPs());
        return allAPs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toAbstractString() {
        return "( " + this.left.toAbstractString() + " ) R ( " + this.right.toAbstractString() + " )";
    }

    @Override
    public String toAbstractString(OutputEquivalence mapper) {
        return "( " + this.left.toAbstractString(mapper) + " ) R ( " + this.right.toAbstractString(mapper) + " )";
    }

    /**
     * <p>getLeft.</p>
     *
     * @return a left {@link TemporalLogic<I>} object.
     */
    public TemporalLogic<I> getLeft() {
        return this.left;
    }

    /**
     * <p>getRight.</p>
     *
     * @return a right {@link TemporalLogic<I>} object.
     */
    public TemporalLogic<I> getRight() {
        return this.right;
    }

    @Override
    public TemporalLogic<I> derivativeOn(String a){
        TemporalLogic<I> res = new TemporalOr<I>(new TemporalAnd<I>(left.derivativeOn(a), right.derivativeOn(a)), new TemporalAnd<I>(right.derivativeOn(a), this));
        return res;
    }

    public TemporalLogic<I> toNnf(boolean negate){
        if (negate){
            return new TemporalUntil<I>(left.toNnf(negate), right.toNnf(negate));
        } else {
            return new TemporalRelease<I>(left.toNnf(negate), right.toNnf(negate));
        }
    }

    public TemporalLogic<I> toDisjunctiveForm(){
        return new TemporalRelease<I>(left.toDisjunctiveForm(), right.toDisjunctiveForm());
    }

    public List<TemporalLogic<I>> getAllConjunctions(){
        List<TemporalLogic<I>> result =  new ArrayList<>();
        result.addAll(left.getAllConjunctions());
        result.addAll(right.getAllConjunctions());
        return result;
    }

    static class STLRelease extends TemporalRelease<List<Double>> implements STLCost {
        STLRelease(STLCost left, STLCost right) {
            super(left, right);
        }
    }

    static class LTLRelease extends TemporalRelease<String> implements LTLFormula {
        LTLRelease(LTLFormula left, LTLFormula right) {
            super(left, right);
        }
    }
}
