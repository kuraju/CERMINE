/**
 * This file is part of CERMINE project.
 * Copyright (c) 2011-2013 ICM-UW
 *
 * CERMINE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CERMINE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with CERMINE. If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.icm.cermine.structure;

import java.util.*;
import pl.edu.icm.cermine.structure.model.*;
import pl.edu.icm.cermine.structure.tools.Direction;
import pl.edu.icm.cermine.structure.tools.Range;
import pl.edu.icm.cermine.structure.tools.Valley;

/**
 * XY-cut-based document segmenter.
 * 
 * @author estocka
 */
public class XYCutSegmenter implements DocumentSegmenter {

    private double pageHeight;
    private double threshold = 0.025;

    @Override
    public BxDocument segmentDocument(BxDocument bd) {

        List<BxPage> pages = bd.getPages();
        for (BxPage page : pages) {

            pageHeight = page.getBounds().getHeight();

            if (page.getChunks() != null) {
                List<BxZone> xySegmentation = xySegmentation(page);


                page.setZones(xySegmentation);
            }
        }
        return bd;
    }

    List<BxZone> xySegmentation(BxPage page) {
        List<BxZone> zonesList = new ArrayList<BxZone>();
        BxZone zone = new BxZone().setBounds(page.getBounds()).setChunks(page.getChunks());
        xyCut(cutZoneIn2(zone), zonesList);
        return zonesList;
    }

    void xyCut(List<BxZone> zones, List<BxZone> zonesList) {

        if (zones.size() < 2) {
            zonesList.addAll(zones);

        } else {
            for (BxZone bz : zones) {
                xyCut(cutZoneIn2(bz), zonesList);

            }
        }

    }

    List<BxZone> cutZoneIn2(BxZone currentZone) {
        List<BxChunk> chunks = currentZone.getChunks();

        SortedSet<Range> sameYRangeList = toRanges(chunks, Direction.Y);
        SortedSet<Range> sameXRangeList = toRanges(chunks, Direction.X);
        List<Valley> xValleys = toValleys(sameXRangeList, Direction.X);
        List<Valley> yValleys = toValleys(sameYRangeList, Direction.Y);

        double th = computeThreshold();
        Valley maxValley = maxValley(xValleys, yValleys);

        List<BxZone> newZones = new ArrayList<BxZone>();

        if (maxValley.getLength() > th) {
           

            List<List<BxChunk>> dividedChunks = null;
            switch (maxValley.getDirection()) {
                case Y:
                    dividedChunks = divideChunks(sameYRangeList, maxValley);
                    break;
                case X:
                    dividedChunks = divideChunks(sameXRangeList, maxValley);
                    break;
            }


            for (int i = 0; i < 2; i++) {
                if (!dividedChunks.get(i).isEmpty()) {
                    newZones.add(new BxZone().setBounds(computeChunksListBounds(dividedChunks.get(i))).setChunks(dividedChunks.get(i)));
                }
            }
        } else {
            newZones.add(currentZone);
        }
        return newZones;
    }

    double computeThreshold() {

        return threshold * pageHeight;
    }

    BxBounds computeChunksListBounds(List<BxChunk> chunksList) {

        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        if (chunksList != null && !chunksList.isEmpty()) {
            minX = chunksList.get(0).getBounds().getX();
            maxX = chunksList.get(0).getBounds().getX();
            minY = chunksList.get(0).getBounds().getY();
            maxY = chunksList.get(0).getBounds().getY();
            for (BxChunk bc : chunksList) {
                if (bc.getBounds().getX() < minX) {
                    minX = bc.getBounds().getX();
                }
                if (bc.getBounds().getX() + bc.getBounds().getWidth() > maxX) {
                    maxX = bc.getBounds().getX() + bc.getBounds().getWidth();
                }
                if (bc.getBounds().getY() < minY) {
                    minY = bc.getBounds().getY();
                }
                if (bc.getBounds().getY() + bc.getBounds().getHeight() > maxY) {
                    maxY = bc.getBounds().getY() + bc.getBounds().getHeight();
                }
            }
        }
        return new BxBounds(minX, minY, maxX - minX, maxY - minY);
    }

    List<List<BxChunk>> divideChunks(SortedSet<Range> notDivided, Valley maxValley) {
        Range boundRange = new Range();
        boundRange.setRangeStart(maxValley.getValleyStart());
        SortedSet<Range> headSet = notDivided.headSet(boundRange);
        List<BxChunk> headList = new ArrayList<BxChunk>();
        for (Range sr : headSet) {
            headList.addAll(sr.getChunksList());
        }
        SortedSet<Range> tailSet = notDivided.tailSet(boundRange);
        List<BxChunk> tailList = new ArrayList<BxChunk>();
        for (Range sr : tailSet) {
            tailList.addAll(sr.getChunksList());
        }

        List<List<BxChunk>> tsl = new ArrayList<List<BxChunk>>();
        tsl.add(headList);
        tsl.add(tailList);
        return tsl;
    }

    Valley maxValley(List<Valley> xValleys, List<Valley> yValleys) {
        Valley max;
        Valley xMax = new Valley();
        Valley yMax = new Valley();

        if (xValleys.size() > 0) {
            xMax = Collections.max(xValleys);
        }
        if (yValleys.size() > 0) {
            yMax = Collections.max(yValleys);
        }
        if (xMax.compareTo(yMax) >= 0) {
            max = xMax;

        } else {
            max = yMax;
        }
        return max;
    }

    List<Valley> toValleys(SortedSet<Range> sr, Direction direction) {
        Range[] tab = new Range[sr.size()];
        List<Valley> valleysList = new ArrayList<Valley>();
        sr.toArray(tab);
        for (int i = 0; i < tab.length - 1; i++) {
            Valley v = new Valley();
            v.setValleyStart(tab[i].getRangeEnd());
            v.setValleyEnd(tab[i + 1].getRangeStart());
            v.setDirection(direction);
            valleysList.add(v);
        }
        return valleysList;
    }

    SortedSet<Range> toRanges(List<BxChunk> chunkList, Direction d) {
        TreeSet<Range> sameRangeSet = new TreeSet<Range>();
        for (BxChunk chunk : chunkList) {
            boolean classified = false;
            double start = 0;
            double end = 0;
            switch (d) {
                case Y:
                    start = chunk.getBounds().getY();
                    end = chunk.getBounds().getY() + chunk.getBounds().getHeight();
                    break;
                case X:
                    start = chunk.getBounds().getX();
                    end = chunk.getBounds().getX() + chunk.getBounds().getWidth();
                    break;
            }
            for (Range sameRange : sameRangeSet) {

                if (!classified) {
                    boolean inRange = sameRange.inRange(start, end);
                    if (inRange) {
                        sameRange.addChunk(chunk);

                        classified = true;
                    }
                }
            }
            if (!classified) {
                Range newSameRange = new Range();
                newSameRange.setRangeStart(start);
                newSameRange.setRangeEnd(end);
                newSameRange.addChunk(chunk);
                sameRangeSet.add(newSameRange);
            }

        }

        return sameRangeSet;
    }

    public void setPageHeight(double pageHeight) {
        this.pageHeight = pageHeight;
    }

    public double getPageHeight() {
        return pageHeight;
    }
    
}
