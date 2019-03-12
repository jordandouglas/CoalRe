/*
 * Copyright (C) 2015 Tim Vaughan <tgvaughan@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package coalre.networkannotator;

import beast.app.treeannotator.CladeSystem;
import coalre.network.Network;
import coalre.network.NetworkNode;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Adds conversion summary tools to CladeSystem.
 *
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
public class HardwiredClusters extends NetworkCladeSystem {

//    protected Map<BitSetPair, Map<Locus, List<Conversion>>> conversionLists = new HashMap<>();
//    protected Map<BitSetPair, List<Conversion>> conversionListsTemp = new HashMap<>();
    protected List<Map<BitSet, Map<BitSet, Long>>> geneFlow = new ArrayList<>();
    protected BitSet[] bitSets;
    

    protected int acgIndex = 1;

    public HardwiredClusters() { }

    public HardwiredClusters(Network network) {
        add(network, true);
    }
    

    /**
     * Assemble list of bitSets for this ACG.
     */
    public BitSet[] getBitSets(Network network) {

        if (bitSets == null)
            bitSets = new BitSet[network.getNodeCount()];

        applyToClades(network.getRoot(), (cladeNode, bits) -> {
            bitSets[cladeNode.getNr()] = bits;
            return null;
        });

        return bitSets;
    }

//    /**
//     * Add conversions described on provided acg to the internal list
//     * for later summary.
//     *
//     * @param acg conversion graph from which to extract conversions
//     */
//    public void collectConversions(Network acg) {
//        getBitSets(acg);
//
//        Map<BitSet,Map<BitSet,Long>> geneFlowTemp = new HashMap<>();
//
//        // Assemble list of conversions for each pair of clades on each locus
//        for (Locus locus : acg.getConvertibleLoci()) {
//
//            conversionListsTemp.clear();
//            for (Conversion conv : acg.getConversions(locus))  {
//                conv.acgIndex = acgIndex;
//                BitSetPair bsPair = new BitSetPair(conv);
//
//                if (!conversionListsTemp.containsKey(bsPair))
//                    conversionListsTemp.put(bsPair, new ArrayList<>());
//
//                conversionListsTemp.get(bsPair).add(conv);
//
//                // Record gene flow
//                if (!geneFlowTemp.containsKey(bsPair.from))
//                    geneFlowTemp.put(bsPair.from, new HashMap<>());
//
//                long oldFlow = 0;
//                if (geneFlowTemp.get(bsPair.from).containsKey(bsPair.to))
//                    oldFlow = geneFlowTemp.get(bsPair.from).get(bsPair.to);
//
//                geneFlowTemp.get(bsPair.from).put(bsPair.to, oldFlow + conv.getSiteCount());
//            }
//
//            // Merge overlapping conversions:
//            for (BitSetPair bsPair : conversionListsTemp.keySet()) {
//                List<Conversion> merged = mergeOverlappingConvs(
//                            conversionListsTemp.get(bsPair));
//
//                if (!conversionLists.containsKey(bsPair))
//                    conversionLists.put(bsPair, new HashMap<>());
//                if (!conversionLists.get(bsPair).containsKey(locus))
//                    conversionLists.get(bsPair).put(locus, new ArrayList<>());
//
//                conversionLists.get(bsPair).get(locus).addAll(merged);
//
//            }
//        }
//
//        geneFlow.add(geneFlowTemp);
//
//        acgIndex += 1;
//    }

//    private List<Conversion> mergeOverlappingConvs(List<Conversion> conversions) {
//        List<Conversion> mergedList = new ArrayList<>();
//
//        List<Conversion> convOrderedByStart = new ArrayList<>(conversions);
//        convOrderedByStart.sort((o1, o2) -> o1.getStartSite() - o2.getStartSite());
//
//        List<Conversion> convOrderedByEnd = new ArrayList<>(conversions);
//        convOrderedByEnd.sort((o1, o2) -> o1.getEndSite() - o2.getEndSite());
//
//
//        int nActive = 0;
//        Conversion currentMergedConv = null;
//        int mergedConvCount = 0;
//        double mergedConvHeight1 = 0.0;
//        double mergedConvHeight2 = 0.0;
//
//        while (!convOrderedByStart.isEmpty() || !convOrderedByEnd.isEmpty()) {
//
//            int nextStart = convOrderedByStart.isEmpty()
//                    ? Integer.MAX_VALUE
//                    : convOrderedByStart.get(0).getStartSite();
//
//            int nextEnd = convOrderedByEnd.isEmpty()
//                    ? Integer.MAX_VALUE
//                    : convOrderedByEnd.get(0).getEndSite();
//
//            if (nextStart < nextEnd) {
//                nActive += 1;
//
//                if (nActive == 1) {
//                    currentMergedConv = convOrderedByStart.get(0).getCopy();
//                    currentMergedConv.acgIndex = convOrderedByStart.get(0).acgIndex;
//                    mergedConvCount = 1;
//                    mergedConvHeight1 = currentMergedConv.getHeight1();
//                    mergedConvHeight2 = currentMergedConv.getHeight2();
//                } else {
//                    mergedConvCount += 1;
//                    mergedConvHeight1 += convOrderedByStart.get(0).getHeight1();
//                    mergedConvHeight2 += convOrderedByStart.get(0).getHeight2();
//                }
//
//                convOrderedByStart.remove(0);
//
//            } else {
//                nActive -= 1;
//
//                if (nActive == 0 ) {
//                    assert currentMergedConv != null;
//                    currentMergedConv.setEndSite(nextEnd);
//                    currentMergedConv.setHeight1(mergedConvHeight1/mergedConvCount);
//                    currentMergedConv.setHeight2(mergedConvHeight2 / mergedConvCount);
//                    mergedList.add(currentMergedConv);
//                }
//
//                convOrderedByEnd.remove(0);
//            }
//        }
//
//        return mergedList;
//    }

    /**
     * Determine contiguous regions on specified locus where the fraction of
     * ACGs having a conversion active is greater than the given threshold.
     *
     * @param from BitSet representing source clade
     * @param to BitSet representing destination clade
     * @param locus locus to consider
     * @param threshold minimum fraction of sampled conversions included
     * @return List of regions
     */
//    public List<ConversionSummary> getConversionSummaries(BitSet from, BitSet to,
//                                                          Locus locus,
//                                                          int nACGs,
//                                                          double threshold) {
//
//        BitSetPair bsPair = new BitSetPair(from, to);
//
//        List<ConversionSummary> convSummaryList = new ArrayList<>();
//
//        // Return empty list if on conversions meet the criteria.
//        if (!conversionLists.containsKey(bsPair)
//                || !conversionLists.get(bsPair).containsKey(locus))
//            return convSummaryList;
//
//        int thresholdCount = (int)Math.ceil(nACGs*threshold);
//
//        List<Conversion> convOrderedByStart = new ArrayList<>();
//        convOrderedByStart.addAll(conversionLists.get(bsPair).get(locus));
//        convOrderedByStart.sort((Conversion o1, Conversion o2) ->
//                o1.getStartSite() - o2.getStartSite());
//
//        List<Conversion> convOrderedByEnd = new ArrayList<>();
//        convOrderedByEnd.addAll(conversionLists.get(bsPair).get(locus));
//        convOrderedByEnd.sort((Conversion o1, Conversion o2) ->
//                o1.getEndSite() - o2.getEndSite());
//
//        List<Conversion> activeConversions = new ArrayList<>();
//        ConversionSummary conversionSummary = null;
//
//        BitSet includedACGindices = new BitSet();
//
//        while (!convOrderedByStart.isEmpty() || !convOrderedByEnd.isEmpty()) {
//
//            int nextStart = convOrderedByStart.isEmpty()
//                    ? Integer.MAX_VALUE
//                    : convOrderedByStart.get(0).getStartSite();
//
//            int nextEnd = convOrderedByEnd.isEmpty()
//                    ? Integer.MAX_VALUE
//                    : convOrderedByEnd.get(0).getEndSite();
//
//            if (nextStart < nextEnd) {
//                activeConversions.add(convOrderedByStart.get(0));
//
//                if (activeConversions.size() >= thresholdCount) {
//                    if ( conversionSummary == null) {
//                        conversionSummary = new ConversionSummary();
//                        convSummaryList.add(conversionSummary);
//                        conversionSummary.addConvs(activeConversions);
//
//                        includedACGindices.clear();
//                        for (Conversion conv : activeConversions)
//                            includedACGindices.set(conv.acgIndex);
//                    } else {
//                        conversionSummary.addConv(convOrderedByStart.get(0));
//                        includedACGindices.set(convOrderedByStart.get(0).acgIndex);
//                    }
//                }
//                convOrderedByStart.remove(0);
//            } else {
//                activeConversions.remove(convOrderedByEnd.get(0));
//                if (activeConversions.size() == thresholdCount-1) {
//                    assert conversionSummary != null;
//                    conversionSummary.nIncludedACGs = includedACGindices.cardinality();
//                    conversionSummary = null;
//                }
//                convOrderedByEnd.remove(0);
//            }
//        }
//
//        return convSummaryList;
//    }

    /**
     * @return list of maps specifying gene flow between clades.
     */
    public List<Map<BitSet,Map<BitSet,Long>>> getGeneFlowMaps() {
        return geneFlow;
    }

    /**
     * Apply a function to each sub-clade.
     *
     * @param node MRCA of clade
     * @param function function to apply. Given sub-clade parent node
     *                 and bitset as arguments.
     * @return BitSet representing clade.
     */
    public BitSet applyToClades(NetworkNode node, BiFunction<NetworkNode, BitSet, Void> function) {
        BitSet bits = new BitSet();

        if (node.isLeaf()) {
            bits.set(2 * getTaxonIndex(node));
        } else {
            for (NetworkNode child : node.getChildren())
                bits.or(applyToClades(child, function));
        }

        function.apply(node, bits);

        return bits;
    }


    /**
     * Class representing an ordered pair of BitSets.
     */
    protected class BitSetPair {
        public BitSet from, to;

        public BitSetPair(BitSet from, BitSet to) {
            this.from = from;
            this.to = to;
        }

//        public BitSetPair(Conversion conv) {
//            this.from = bitSets[conv.getNode1().getNr()];
//            this.to = bitSets[conv.getNode2().getNr()];
//        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BitSetPair that = (BitSetPair) o;

            return from.equals(that.from) && to.equals(that.to);

        }

        @Override
        public int hashCode() {
            int result = from.hashCode();
            result = 31 * result + to.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return from.toString() + " -> " + to.toString();
        }
    }

    /**
     * Class representing a summary of similar conversions between two
     * points in the summarized clonal frame.
     */
    public class ConversionSummary {

        List<Double> height1s = new ArrayList<>();
        List<Double> height2s = new ArrayList<>();
        List<Integer> startSites = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();

        public int nIncludedACGs = 0;

        /**
         * Add metrics associated with given conversion to summary.
         *
         * @param conv conversion
         */
//        public void addConv(Conversion conv) {
//            height1s.add(conv.getHeight1());
//            height2s.add(conv.getHeight2());
//            startSites.add(conv.getStartSite());
//            ends.add(conv.getEndSite());
//        }

        /**
         * Add metrics associated with each of the conversions in the
         * given list to the summary.
         *
         * @param convs list of conversions
         */
//        public void addConvs(List<Conversion> convs) {
//            for (Conversion conv : convs)
//                addConv(conv);
//        }

        /**
         * @return number of conversions included in summary.
         */
        public int summarizedConvCount() {
            return height1s.size();
        }
    }
}
