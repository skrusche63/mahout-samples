package de.kp.mahout.training;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import com.google.common.collect.Lists;

public class ModelBuilder {

	/*
	 * The ModelBuilder uses a fixed similarity class, LogLikelihoodSimilarity
	 */
	private static String SIMILARITY_CLASS = "org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity";
	
	public ModelBuilder() {		
	}
	
	public void build(File modelFile, DataModel dataModel, int similarItemsPerItem, int numProcessors) throws IOException {

		BufferedWriter writer = null;
		ExecutorService executorService = Executors.newFixedThreadPool(numProcessors + 1);

		try {

			ItemSimilarity similarity = (ItemSimilarity) Class.forName(SIMILARITY_CLASS).getConstructor(DataModel.class).newInstance(dataModel);
			ItemBasedRecommender itemBasedRecommender = new GenericItemBasedRecommender(dataModel, similarity);

			writer = new BufferedWriter(new FileWriter(modelFile));

			int batchSize = 100;
			int numItems = dataModel.getNumItems();

			List<long[]> itemIDBatches = queueItemIDsInBatches(dataModel.getItemIDs(), numItems, batchSize);

			BlockingQueue<long[]> itemsIDsToProcess = new LinkedBlockingQueue<long[]>(itemIDBatches);
			BlockingQueue<String> output = new LinkedBlockingQueue<String>();

			AtomicInteger numActiveWorkers = new AtomicInteger(numProcessors);
			for (int n = 0; n < numProcessors; n++) {
				executorService.execute(new SimilarItemsWorker(n, itemsIDsToProcess, output, itemBasedRecommender, similarItemsPerItem, numActiveWorkers));
			}
			
			executorService.execute(new OutputWriter(output, writer, numActiveWorkers));

		} catch (Exception e) {
			throw new IOException(e);
			
		} finally {
			executorService.shutdown();
			try {
				executorService.awaitTermination(6, TimeUnit.HOURS);
			
			} catch (InterruptedException e) {

			}

			writer.close();
		
		}
	}

	private List<long[]> queueItemIDsInBatches(LongPrimitiveIterator itemIDs, int numItems, int batchSize) {
		
		List<long[]> itemIDBatches = Lists.newArrayListWithCapacity(numItems / batchSize);

		long[] batch = new long[batchSize];
		int pos = 0;
		
		while (itemIDs.hasNext()) {
		
			if (pos == batchSize) {
				itemIDBatches.add(batch.clone());
				pos = 0;
			}
			
			batch[pos] = itemIDs.nextLong();
			pos++;
		}
		
		int nonQueuedItemIDs = batchSize - pos;
		if (nonQueuedItemIDs > 0) {
		
			long[] lastBatch = new long[nonQueuedItemIDs];
			
			System.arraycopy(batch, 0, lastBatch, 0, nonQueuedItemIDs);
			itemIDBatches.add(lastBatch);
		}
		
		return itemIDBatches;
	
	}

	static class OutputWriter implements Runnable {

		private final BlockingQueue<String> output;
		private final BufferedWriter writer;
		
		private final AtomicInteger numActiveWorkers;

		OutputWriter(BlockingQueue<String> output, BufferedWriter writer, AtomicInteger numActiveWorkers) {

			this.output = output;
			this.writer = writer;
			
			this.numActiveWorkers = numActiveWorkers;
		
		}

		public void run() {

			while (numActiveWorkers.get() != 0) {
			
				try {
					String lines = output.poll(10, TimeUnit.MILLISECONDS);
					if (null != lines) {
						writer.write(lines);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	static class SimilarItemsWorker implements Runnable {
		
		private final BlockingQueue<long[]> itemIDBatches;
		private final BlockingQueue<String> output;
		
		private final ItemBasedRecommender itemBasedRecommender;
		
		private final int howMany;
		private final AtomicInteger numActiveWorkers;

		SimilarItemsWorker(int number, BlockingQueue<long[]> itemIDBatches, BlockingQueue<String> output, ItemBasedRecommender itemBasedRecommender, int howMany, AtomicInteger numActiveWorkers) {

			this.itemIDBatches = itemIDBatches;
			
			this.output = output;
			this.itemBasedRecommender = itemBasedRecommender;
			
			this.howMany = howMany;
			this.numActiveWorkers = numActiveWorkers;
		
		}

		public void run() {
			
			while (!itemIDBatches.isEmpty()) {
			
				try {
					
					long[] itemIDBatch = itemIDBatches.take();
					StringBuilder lines = new StringBuilder();

					for (long itemID : itemIDBatch) {
						
						Iterable<RecommendedItem> similarItems = itemBasedRecommender.mostSimilarItems(itemID, howMany);

						for (RecommendedItem similarItem : similarItems) {
							lines.append(itemID).append(',').append(similarItem.getItemID()).append(',').append(similarItem.getValue()).append('\n');
						}
						
					}

					output.offer(lines.toString());

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			numActiveWorkers.decrementAndGet();
		}
	}
}
