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
import pl.edu.icm.cermine.structure.model.BxDocument;

/**
 * Document references extractor interface.
 * 
 * @author Dominika Tkaczyk
 * @param <T> a type of a reference
 */
public interface DocumentReferencesExtractor<T> {
	
    /**
     * Extracts references from a document passed as InputStream.
     * 
     * @param stream
     * @return an array of extracted references.
     * @throws AnalysisException 
     */
	T[] extractReferences(InputStream stream) throws AnalysisException;
    
    /**
     * Extracts references from a document.
     * 
     * @param document
     * @return an array of extracted references.
     * @throws AnalysisException 
     */
    T[] extractReferences(BxDocument document) throws AnalysisException;
}
