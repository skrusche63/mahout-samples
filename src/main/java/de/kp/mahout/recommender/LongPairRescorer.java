package de.kp.mahout.recommender;

import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.common.LongPair;

public class LongPairRescorer implements Rescorer<LongPair> {

  private final IDRescorer rescorer;

  public LongPairRescorer(IDRescorer rescorer) {
    this.rescorer = rescorer;
  }

  public double rescore(LongPair thing, double originalScore) {
    return originalScore;
  }

  public boolean isFiltered(LongPair pair) {
    return rescorer.isFiltered(pair.getSecond());
  }

}
