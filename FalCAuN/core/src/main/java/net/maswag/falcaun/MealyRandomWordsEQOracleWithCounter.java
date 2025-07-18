package net.maswag.falcaun;

import java.util.Collection;
import java.util.Random;

import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.oracle.equivalence.MealyRandomWordsEQOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class  MealyRandomWordsEQOracleWithCounter<I, O> extends MealyRandomWordsEQOracle<I, O> {
  private long eqQueryCount;
  private long memQueryCount;
  private final MealyCounterOracle<I, O> memOracle;

  public MealyRandomWordsEQOracleWithCounter(MealyCounterOracle<I, O> memOracle,
      int minLength, int maxLength, int maxTests, Random random, int batchSize) {
        super(memOracle, minLength, maxLength, maxTests, random, batchSize);
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