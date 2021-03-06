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

package pl.edu.icm.cermine;

import java.io.InputStream;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.*;
import pl.edu.icm.cermine.structure.model.BxDocument;

/**
 * Text extractor from PDF files. Extracted text includes 
 * all text string found in the document in correct reading order.
 *
 * @author Paweł Szostek
 * @author Dominika Tkaczyk
 */
public class PdfRawTextExtractor implements DocumentTextExtractor<String> {
    /** individual character extractor */
    private CharacterExtractor characterExtractor;
    
    /** document object segmenter */
    private DocumentSegmenter documentSegmenter;
    
    /** reading order resolver */
    private ReadingOrderResolver roResolver;
    
    public PdfRawTextExtractor() throws AnalysisException {
        characterExtractor = new ITextCharacterExtractor();
        documentSegmenter = new ParallelDocstrumSegmenter();
        roResolver = new HierarchicalReadingOrderResolver();
    }
    
    public PdfRawTextExtractor(CharacterExtractor glyphExtractor, DocumentSegmenter pageSegmenter, ReadingOrderResolver roResolver) {
        this.characterExtractor = glyphExtractor;
        this.documentSegmenter = pageSegmenter;
        this.roResolver = roResolver;
    }
    
    /**
     * Extracts content of a pdf to a plain text.
     * 
     * @param stream
     * @return pdf's content as plain text
     * @throws AnalysisException 
     */
    @Override
    public String extractText(InputStream stream) throws AnalysisException {
        BxDocument doc = characterExtractor.extractCharacters(stream);
        doc = documentSegmenter.segmentDocument(doc);
        doc = roResolver.resolve(doc);
        return extractText(doc);
    }
    
    /**
     * Extracts content of a pdf to a plain text.
     * 
     * @param document
     * @return pdf's content as plain text
     * @throws AnalysisException 
     */
    @Override
    public String extractText(BxDocument document) throws AnalysisException {
        return document.toText();
    }

    public void setGlyphExtractor(CharacterExtractor glyphExtractor) {
        this.characterExtractor = glyphExtractor;
    }

    public void setPageSegmenter(DocumentSegmenter pageSegmenter) {
        this.documentSegmenter = pageSegmenter;
    }

    public void setRoResolver(ReadingOrderResolver roResolver) {
        this.roResolver = roResolver;
    }
    
}
