package de.kp.mahout.recommender;

import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.MostSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.common.LongPair;

public class ItemRecommender  extends GenericItemBasedRecommender {

	public ItemRecommender(DataModel dataModel, ItemSimilarity similarity, CandidateItemsStrategy candidateItemsStrategy, MostSimilarItemsCandidateItemsStrategy mostSimilarItemsCandidateItemsStrategy) {
		super(dataModel, similarity, candidateItemsStrategy, mostSimilarItemsCandidateItemsStrategy);
	}

	public List<RecommendedItem> recommendByItems(long[] itemIDs, int howMany, IDRescorer idRescorer) throws TasteException {

		Rescorer<LongPair> rescorer = (idRescorer != null) ? new LongPairRescorer(idRescorer) : null;
		return mostSimilarItems(itemIDs, howMany, rescorer, false);
	}
	
}
