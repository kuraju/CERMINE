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

package pl.edu.icm.cermine.metadata.zoneclassification.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.structure.model.BxZone;
import pl.edu.icm.cermine.structure.model.BxZoneLabel;
import pl.edu.icm.cermine.tools.classification.features.FeatureVector;
import pl.edu.icm.cermine.tools.classification.features.FeatureVectorBuilder;
import pl.edu.icm.cermine.tools.classification.hmm.model.HMMTrainingSample;

/**
 * BxDocument objects to HMM training elements converter node. The observations
 * emitted by resulting training elements are vectors of features.
 *
 * @author Dominika Tkaczyk (d.tkaczyk@icm.edu.pl)
 */
public class BxDocsToHMMConverter {

    private FeatureVectorBuilder<BxZone, BxPage> featureVectorBuilder;

    private Map<BxZoneLabel, BxZoneLabel> labelMap;

    public BxDocsToHMMConverter() {}

    public BxDocsToHMMConverter(FeatureVectorBuilder<BxZone, BxPage> featureVectorBuilder, Map<BxZoneLabel, BxZoneLabel> labelMap) {
    	this.featureVectorBuilder = featureVectorBuilder;
    	this.labelMap = labelMap;
    }
    
    public List<HMMTrainingSample<BxZoneLabel>> process(List<BxDocument> documents) throws AnalysisException {
        List<HMMTrainingSample<BxZoneLabel>> trainingList =
                new ArrayList<HMMTrainingSample<BxZoneLabel>>(documents.size());
                
        for (BxDocument doc : documents) {
            ZoneClassificationUtils.correctPagesBounds(doc);
            
            if (labelMap != null) {
                ZoneClassificationUtils.mapZoneLabels(doc, labelMap);
            }

            HMMTrainingSample<BxZoneLabel> prev = null;
            for (BxPage page : doc.getPages()) {
                for (BxZone zone : page.getZones()) {
                    FeatureVector featureVector = featureVectorBuilder.getFeatureVector(zone, page);
                    HMMTrainingSample<BxZoneLabel> element =
                            new HMMTrainingSample<BxZoneLabel>(featureVector, zone.getLabel(), prev == null);
                    trainingList.add(element);

                    if (prev != null) {
                        prev.setNextLabel(zone.getLabel());
                    }
                    prev = element;
                }
            }
        }
        return trainingList;
    }

    public void setFeatureVectorBuilder(FeatureVectorBuilder<BxZone, BxPage> featureVectorBuilder) {
        this.featureVectorBuilder = featureVectorBuilder;
    }

    public void setLabelMap(Map<BxZoneLabel, BxZoneLabel> labelMap) {
        this.labelMap = labelMap;
    }
    
}
