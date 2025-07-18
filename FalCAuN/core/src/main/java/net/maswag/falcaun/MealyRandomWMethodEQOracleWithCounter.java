package net.maswag.falcaun;

import java.util.Collection;

import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.oracle.equivalence.MealyRandomWMethodEQOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class  MealyRandomWMethodEQOracleWithCounter<I, O> extends MealyRandomWMethodEQOracle<I, O> {
  private long eqQueryCount;
  private long memQueryCount;
  private final MealyCounterOracle<I, O> memOracle;

  public MealyRandomWMethodEQOracleWithCounter(MealyCounterOracle<I, O> memOracle, int minimalLength, int rndLength, int bound) {
        super(memOracle, minimalLength, rndLength, bound);
        eqQueryCount = 0;
        memQueryCount = 0;
        this.memOracle = memOracle;
  }

  @Override
  public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis, Collection<? extends I> inputs) {
    long oldCount = memOracle.getQueryCounter().getCount();
    DefaultQuery<I, Word<O>> query =  super.findCounterExample(hypothesis, inputs);
    long newCount = memOracle.getQueryCounter().getCount();
    eqQueryCount++;
    memQueryCount += newCount - oldCount;
    return query;
  }

  public long getEQQueryCount() {
    return eqQueryCount;
  }

  public long getMemQueryCount(){
    return memQueryCount;
  }
}