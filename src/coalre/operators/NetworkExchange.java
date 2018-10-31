package coalre.operators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import beast.core.Input;
import beast.util.Randomizer;
import coalre.network.Network;
import coalre.network.NetworkEdge;
import coalre.network.NetworkNode;

public class NetworkExchange extends DivertSegmentOperator {
	final public Input<Boolean> isNarrowInput = new Input<>("isNarrow",
			"if true (default) a narrow exchange is performed, "
					+ "otherwise a wide exchange", true);

	//	TODO Clean up: make segment exchange part of exchange method.

	private boolean isNarrow;

	@Override
	public void initAndValidate() {
		super.initAndValidate();

		isNarrow = isNarrowInput.get();
	}

	@Override
	public double networkProposal() {

		double logHR;
		network.startEditing(this);

		if (isNarrow) {
			logHR = narrow(network);
		} else {
			logHR = wide(network);
		}

		return logHR;
	}

	private int kidsNotLeaves(final NetworkNode n) {
		return (n.getChildEdges().get(0).isLeafEdge() &&
				n.getChildEdges().get(1).isLeafEdge()) ? 0:1;
	}

	private int meAndKidsNotLeaves(final NetworkNode n) {
		return (n.isLeaf() ? 0 : kidsNotLeaves(n));
	}



	/**
	 * Perform equivalent of narrow tree exchange on a network.
	 *
	 * @param	network
	 * @return	log of Hastings Ratio, or Double.NEGATIVE_INFINITY
	 * 			if proposal should not be accepted
	 */
	public double narrow(final Network network) {
		double logHR = 0.0;

		final List<NetworkEdge> networkEdges = new ArrayList<>(network.getEdges());

		final List<NetworkEdge> possibleGrandParentEdges = networkEdges.stream()
				.filter(e -> !e.isLeafEdge())
				.filter(e -> e.childNode.isCoalescence())
				.filter(e -> !e.childNode.getChildEdges().get(0).childNode.isReassortment())
				.filter(e -> !e.childNode.getChildEdges().get(1).childNode.isReassortment())
				.collect(Collectors.toList());

		final int possibleGrandParents = possibleGrandParentEdges.size();
        if (possibleGrandParents < 1) {
            return Double.NEGATIVE_INFINITY;
        }

		final NetworkEdge grandParentEdge = possibleGrandParentEdges.
				get(Randomizer.nextInt(possibleGrandParents));
		final NetworkNode grandParent = grandParentEdge.childNode;

		final List<NetworkEdge> possibleParentEdges = grandParent.getChildEdges();
		NetworkEdge parentEdge = possibleParentEdges.get(0);
		NetworkEdge auntEdge = possibleParentEdges.get(1);

		NetworkNode parent = parentEdge.childNode;
		NetworkNode aunt = auntEdge.childNode;


		if (parent.getHeight() < aunt.getHeight()) {
			auntEdge = possibleParentEdges.get(0);
			parentEdge = possibleParentEdges.get(1);

			parent = parentEdge.childNode;
			aunt = auntEdge.childNode;
		}

		if( parent.isLeaf() ) {
			return Double.NEGATIVE_INFINITY;
		}

		final List<NetworkEdge> possibleChildEdges = parent.getChildEdges();
		final int childId = Randomizer.nextInt(possibleChildEdges.size());
		final NetworkEdge childEdge = possibleChildEdges.get(childId);

		// After the exchange we want to add segments to the new ancestors
		// and remove from the old. Have to be careful not to remove segments
		// of siblings.
		final BitSet childSegs = childEdge.hasSegments;
		final BitSet childSegsToRemove = (BitSet)childSegs.clone();
		childSegsToRemove.andNot(getSisterEdge(childEdge).hasSegments);

		final BitSet auntSegs = auntEdge.hasSegments;
		final BitSet auntSegsToRemove = (BitSet)auntSegs.clone();
		auntSegsToRemove.andNot(getSisterEdge(auntEdge).hasSegments);
		
		int validGrandParents = 0;
		for (int i=0; i < possibleGrandParents; i++) {
			validGrandParents += kidsNotLeaves(possibleGrandParentEdges.get(i).childNode);
		}
		logHR -= Math.log(1.0/validGrandParents);
		
//		final int c2 = meAndKidsNotLeaves(parent) + meAndKidsNotLeaves(aunt);

		exchangeEdges(childEdge, auntEdge, parent, grandParent);

		logHR += removeSegmentsFromAncestors(grandParentEdge, auntSegsToRemove);
		logHR += removeSegmentsFromAncestors(parentEdge, childSegsToRemove);

		logHR -= addSegmentsToAncestors(parentEdge, auntSegs);
		logHR -= addSegmentsToAncestors(grandParentEdge, childSegs);
		
		final List<NetworkEdge> possibleGrandParentEdgesAfter = networkEdges.stream()
				.filter(e -> !e.isLeafEdge())
				.filter(e -> e.childNode.isCoalescence())
				.filter(e -> !e.childNode.getChildEdges().get(0).childNode.isReassortment())
				.filter(e -> !e.childNode.getChildEdges().get(1).childNode.isReassortment())
				.collect(Collectors.toList());
		
		final int possibleGrandParentsAfetr = possibleGrandParentEdgesAfter.size();
		
		int validGrandParentsAfter = 0;
		for (int i=0; i < possibleGrandParentsAfetr; i++) {
			validGrandParentsAfter += kidsNotLeaves(possibleGrandParentEdgesAfter.get(i).childNode);
		}
		
//		final int possibleGrandParentsAfter = validGrandParentsAfter //- c2 +
//				meAndKidsNotLeaves(parent) + meAndKidsNotLeaves(aunt);
		
//		logHR += Math.log(1.0/possibleGrandParentsAfter);
		logHR += Math.log(1.0/validGrandParentsAfter);

		return logHR;
	}

	/**
	 * Perform equivalent of narrow tree exchange on a network.
	 *
	 * @param	network
	 * @return	log of Hastings Ratio, or Double.NEGATIVE_INFINITY
	 * 			if proposal should not be accepted
	 */
	public double wide(final Network network) {
		double logHR = 0.0;

		List<NetworkEdge> networkEdges = new ArrayList<>(network.getEdges());

		final List<NetworkEdge> possibleEdges = networkEdges.stream()
				.filter(e -> !e.isRootEdge())
				.filter(e -> !e.childNode.isReassortment())
				.filter(e -> !e.parentNode.isReassortment())
				.collect(Collectors.toList());
		
		final int nPossibleEdges = possibleEdges.size();
		logHR -= Math.log(1.0/(double)nPossibleEdges);

		final NetworkEdge iEdge = possibleEdges.
				get(Randomizer.nextInt(possibleEdges.size()));
		final NetworkNode i = iEdge.childNode;

		NetworkEdge jEdge = iEdge;

		while(jEdge == iEdge) {
			jEdge = possibleEdges.
					get(Randomizer.nextInt(possibleEdges.size()));
		}
		final NetworkNode j = jEdge.childNode;

		final NetworkNode p = iEdge.parentNode;
		final NetworkNode jP = jEdge.parentNode;
		final NetworkEdge pEdge = p.getParentEdges().get(0);
		final NetworkEdge jPEdge = jP.getParentEdges().get(0);


		if ((p != jP) && (i !=jP) && (j != p)
				&& (j.getHeight() < p.getHeight())
				&& (i.getHeight() < jP.getHeight())) {

			final BitSet iSegs = iEdge.hasSegments;
			final BitSet jSegs = jEdge.hasSegments;

			final BitSet iSegsToRemove = (BitSet)iSegs.clone();
			iSegsToRemove.andNot(getSisterEdge(iEdge).hasSegments);

			final BitSet jSegsToRemove = (BitSet)jSegs.clone();
			jSegsToRemove.andNot(getSisterEdge(jEdge).hasSegments);

			exchangeEdges(iEdge, jEdge, p, jP);

			logHR += removeSegmentsFromAncestors(jPEdge, jSegsToRemove);
			logHR += removeSegmentsFromAncestors(pEdge, iSegsToRemove);

			logHR -= addSegmentsToAncestors(pEdge, jSegs);
			logHR -= addSegmentsToAncestors(jPEdge, iSegs);
			
			networkEdges = new ArrayList<>(network.getEdges());
			
			final List<NetworkEdge> possibleEdgesAfter = networkEdges.stream()
					.filter(e -> !e.isRootEdge())
					.filter(e -> !e.childNode.isReassortment())
					.filter(e -> !e.parentNode.isReassortment())
					.collect(Collectors.toList());
			
			final int nPossibleEdgesAfter = possibleEdgesAfter.size();
			logHR += Math.log(1.0/(double)nPossibleEdgesAfter);


			return logHR;
		}
		else {
			return Double.NEGATIVE_INFINITY;
		}
	}


	/* exchange sub-nets whose root are i and j */
	protected void exchangeEdges(NetworkEdge i, NetworkEdge j,
			NetworkNode p, NetworkNode jP) {
		p.removeChildEdge(i);
		jP.removeChildEdge(j);
		p.addChildEdge(j);
		jP.addChildEdge(i);
	}


	/**
	 * Check that each edge is ancestral to at least one segment.
	 *
	 * @return true if all edges are ancestral.
	 */
	@Override
	public boolean allEdgesAncestral() {
		final Set<NetworkNode> nodeList = networkInput.get().getNodes();
		for (final NetworkNode node : nodeList) {
			for (final NetworkEdge parentEdge : node.getParentEdges()) {
				if (parentEdge.hasSegments.isEmpty())
					return false;
			}
		}

		return true;
	}

}
