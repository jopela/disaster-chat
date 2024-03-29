package il.technion.ewolf.kbr.openkad.op;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;
import il.technion.ewolf.kbr.KeyColorComparator;
import il.technion.ewolf.kbr.KeyComparator;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.bucket.KBuckets;
import il.technion.ewolf.kbr.openkad.cache.KadCache;
import il.technion.ewolf.kbr.openkad.msg.ForwardMessage;
import il.technion.ewolf.kbr.openkad.msg.ForwardRequest;
import il.technion.ewolf.kbr.openkad.msg.ForwardResponse;
import il.technion.ewolf.kbr.openkad.msg.KadMessage;
import il.technion.ewolf.kbr.openkad.net.MessageDispatcher;
import il.technion.ewolf.kbr.openkad.net.filter.IdMessageFilter;
import il.technion.ewolf.kbr.openkad.net.filter.TypeMessageFilter;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class ForwardFindValueOperation extends FindValueOperation {

	// state
	private int nrQueried = 0;
	private List<Node> bootstrap;

	// dependencies
	private final int kBucketSize;
	private final int nrCandidates;
	private final int nrColors;
	private final int myColor;
	// private final int nrShare;
	private final long timeout;
	private final Node localNode;

	private final Provider<ForwardRequest> forwardRequestProvider;
	private final Provider<MessageDispatcher<Void>> msgDispatcherProvider;
	private final Provider<FindValueOperation> findValueOperationProvider;
	// private final Provider<StoreMessage> storeMessageProvider;

	private final KBuckets kBuckets;
	private final KadCache cache;
	// private final KadServer kadServer;

	// testing
	private final AtomicInteger nrLongTimeouts;
	private final List<Integer> hopsToResultHistogram;
	private final List<Integer> findNodeHopsHistogram;
	private final AtomicInteger maxHopsToResult;
	private final AtomicInteger remoteCacheHits;
	private final AtomicInteger localCacheHits;
	private final AtomicInteger nrNacks;
	private final AtomicInteger nrFindNodesWithWrongColor;

	@Inject
	ForwardFindValueOperation(
			@Named("openkad.bucket.kbuckets.maxsize") int kBucketSize,
			@Named("openkad.color.candidates") int nrCandidates,
			@Named("openkad.color.nrcolors") int nrColors,
			@Named("openkad.local.color") int myColor,
			// @Named("openkad.cache.share") int nrShare,
			@Named("openkad.net.forwarded.timeout") long timeout,
			@Named("openkad.local.node") Node localNode,

			Provider<ForwardRequest> forwardRequestProvider,
			Provider<MessageDispatcher<Void>> msgDispatcherProvider,
			@Named("openkad.op.lastFindValue") Provider<FindValueOperation> findValueOperationProvider,
			// Provider<StoreMessage> storeMessageProvider,

			KBuckets kBuckets,
			KadCache cache,
			// KadServer kadServer,

			// testing
			@Named("openkad.testing.nrLongTimeouts") AtomicInteger nrLongTimeouts,
			@Named("openkad.testing.hopsToResultHistogram") List<Integer> hopsToResultHistogram,
			@Named("openkad.testing.findNodeHopsHistogram") List<Integer> findNodeHopsHistogram,
			@Named("openkad.testing.maxHopsToResult") AtomicInteger maxHopsToResult,
			@Named("openkad.testing.remoteCacheHits") AtomicInteger remoteCacheHits,
			@Named("openkad.testing.remoteCacheHits") AtomicInteger localCacheHits,
			@Named("openkad.testing.nrNacks") AtomicInteger nrNacks,
			@Named("openkad.testing.nrFindNodesWithWrongColor") AtomicInteger nrFindNodesWithWrongColor) {

		this.kBucketSize = kBucketSize;
		this.nrCandidates = nrCandidates;
		this.nrColors = nrColors;
		this.myColor = myColor;
		// this.nrShare = nrShare;
		this.timeout = timeout;
		this.localNode = localNode;

		this.forwardRequestProvider = forwardRequestProvider;
		this.msgDispatcherProvider = msgDispatcherProvider;
		this.findValueOperationProvider = findValueOperationProvider;
		// this.storeMessageProvider = storeMessageProvider;

		this.kBuckets = kBuckets;
		this.cache = cache;
		// this.kadServer = kadServer;

		this.nrLongTimeouts = nrLongTimeouts;
		this.hopsToResultHistogram = hopsToResultHistogram;
		this.findNodeHopsHistogram = findNodeHopsHistogram;
		this.maxHopsToResult = maxHopsToResult;
		this.remoteCacheHits = remoteCacheHits;
		this.localCacheHits = localCacheHits;
		this.nrNacks = nrNacks;
		this.nrFindNodesWithWrongColor = nrFindNodesWithWrongColor;
	}

	public int getNrQueried() {
		return nrQueried;
	}

	private List<Node> sendForwardRequest(Node to, ForwardRequest req)
			throws CancellationException, InterruptedException,
			ExecutionException {
		System.out.println(localNode + ": forwarding to " + to);

		Future<KadMessage> requestFuture = msgDispatcherProvider.get()
				.setConsumable(true)
				.addFilter(new IdMessageFilter(req.getId()))
				.addFilter(new TypeMessageFilter(ForwardResponse.class))
				.futureSend(to, req);

		ForwardResponse res = (ForwardResponse) requestFuture.get();
		if (res.isAck()) {
			System.out.println(localNode + ": remote node return ack");

		} else if (res.isNack()) {
			System.out.println(localNode + ": remote node return nack");

			nrNacks.incrementAndGet();
			bootstrap.addAll(res.getNodes());

			// try to avoid sending more messages to this node
			kBuckets.markAsDead(res.getSrc());

			// Logical throw to indicate result was a nack.
			// will be caught outside to cancel the expect.
			throw new CancellationException("nack");

		} else {
			assert (res.getNodes() != null);
			remoteCacheHits.incrementAndGet();
			System.out.println(localNode + ": cache hit");

			// we had a cache hit !
			// no need to wait for future messages
			int hopsToResult = 1;

			if (hopsToResult > maxHopsToResult.get())
				maxHopsToResult.set(hopsToResult);
			hopsToResultHistogram.add(hopsToResult);
			return res.getNodes();

		}

		return null;
	}

	private List<Node> waitForResults(Future<KadMessage> expectMessage)
			throws InterruptedException, ExecutionException,
			CancellationException {
		ForwardMessage msg = (ForwardMessage) expectMessage.get();

		if (msg.isNack()) {
			nrNacks.incrementAndGet();
			bootstrap.addAll(msg.getNodes());
			kBuckets.markAsDead(msg.getSrc());

			throw new CancellationException("nack");

		} else if (msg.getNodes() != null) {
			// remote node has calculated the results for me
			int hopsToResult = 1 + msg.getPathLength();

			if (hopsToResult > maxHopsToResult.get())
				maxHopsToResult.set(hopsToResult);

			hopsToResultHistogram.add(hopsToResult);

			if (msg.getFindNodeHops() != 0) {
				findNodeHopsHistogram.add(msg.getFindNodeHops());
			} else {
				remoteCacheHits.incrementAndGet();
			}
			System.out.println(localNode
					+ ": remote node has calculated the results for me");
			return msg.getNodes();

		} else {
			// remote node has returned null, move on to the
			// next candidate
			System.out
					.println(localNode
							+ ": remote node has returned null, move on to the next candidate");
			nrNacks.incrementAndGet();
		}

		return null;
	}

	@Override
	public List<Node> doFindValue() {

		List<Node> nodes = cache.search(key);
		if (nodes != null) {
			return nodes;
		}

		bootstrap = kBuckets.getClosestNodesByKey(key, kBucketSize);
		bootstrap.add(localNode);
		List<Node> candidates = sort(bootstrap, on(Node.class).getKey(),
				new KeyColorComparator(key, nrColors));

		if (myColor == key.getColor(nrColors)) {
			hopsToResultHistogram.add(0);
			return doFindNode();
		}

		// List<Node> colorCandidates = kBuckets.getNodesFromColorBucket(key);
		// candidates.removeAll(colorCandidates);
		// candidates.addAll(0, colorCandidates);
		if (candidates.size() > nrCandidates)
			candidates.subList(nrCandidates, candidates.size()).clear();

		do {
			if (candidates.isEmpty()) {
				hopsToResultHistogram.add(0);
				return doFindNode();
			}

			Node n = candidates.remove(0);
			++nrQueried;

			if (n.equals(localNode)) {
				hopsToResultHistogram.add(0);
				return doFindNode();
			}

			// sort and cut the bootstrap
			bootstrap = sort(bootstrap, on(Node.class).getKey(),
					new KeyComparator(key));
			if (bootstrap.size() > kBucketSize)
				bootstrap.subList(kBucketSize, bootstrap.size()).clear();

			ForwardRequest req = forwardRequestProvider.get().setInitiator() // TODO:
																				// remove
																				// b4
																				// publish
					.setBootstrap(bootstrap).setKey(key);

			Future<KadMessage> expectMessage = msgDispatcherProvider.get()
					.setConsumable(true)
					.addFilter(new IdMessageFilter(req.getId()))
					.addFilter(new TypeMessageFilter(ForwardMessage.class))
					.setTimeout(timeout, TimeUnit.MILLISECONDS)
					.futureRegister();

			// send the forward request and wait for result/ack/nack
			try {
				List<Node> results = sendForwardRequest(n, req);
				if (results != null) {
					expectMessage.cancel(false);
					// shareResults(colorCandidates, results);
					return results;
				}
				// results is null, that means the remote node will
				// calculate the result for me

			} catch (Exception e) {
				System.out.println(localNode
						+ ": failed recv ack or nack from remote node");
				expectMessage.cancel(false);
				kBuckets.markAsDead(n);
				continue;
			}

			// wait for the result to arrive
			try {
				List<Node> results = waitForResults(expectMessage);
				if (results != null) {
					// shareResults(colorCandidates, results);
					return results;
				}

			} catch (Exception e) {
				System.out.println(localNode
						+ ": failed to recv expected message");
				kBuckets.markAsDead(n);
				nrLongTimeouts.incrementAndGet();
			}

		} while (true);
	}

	/*
	 * private void shareResults(List<Node> toShareWith, List<Node> results) {
	 * if (toShareWith.size() > nrShare) toShareWith.subList(nrShare,
	 * toShareWith.size()).clear();
	 * 
	 * StoreMessage storeMessage = storeMessageProvider.get() .setKey(key)
	 * .setNodes(results); for (Node n : toShareWith) { // dont send if the
	 * remote node has a different color if (n.getKey().getColor(nrColors) !=
	 * key.getColor(nrColors)) continue; try { kadServer.send(n, storeMessage);
	 * } catch (Exception e) {} } }
	 */

	private List<Node> doFindNode() {

		if (myColor != key.getColor(nrColors)) {
			nrFindNodesWithWrongColor.incrementAndGet();
		}
		FindValueOperation op = findValueOperationProvider.get()
				.setBootstrap(bootstrap).setKey(key);

		List<Node> $ = op.doFindValue();

		nrQueried += op.getNrQueried();
		System.out.println("nrQueried: " + nrQueried);

		if (op.getNrQueried() == 0) {
			localCacheHits.incrementAndGet();
		}

		return $;
	}
}
