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

import java.io.*;
import java.util.Collection;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.exception.TransformationException;
import pl.edu.icm.cermine.structure.SVMAlternativeMetadataZoneClassifier;
import pl.edu.icm.cermine.structure.ZoneClassifier;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.transformers.BxDocumentToTrueVizWriter;

/**
 * NLM-based content extractor from PDF files.
 *
 * @author Dominika Tkaczyk
 */
public class PdfNLMContentExtractor implements DocumentContentExtractor<Element> {

    /** geometric structure extractor */
    private DocumentStructureExtractor structureExtractor;
    
    /** document metadata extractor from geometric structure */
    private DocumentMetadataExtractor<Element> metadataExtractor;
    
    /** parsed references extractor from geometric structure */
    private DocumentReferencesExtractor<Element> referencesExtractor;
    
    /** logical content extractor */
    private DocumentTextExtractor<Element> textExtractor;
    
    private boolean extractMetadata = true;
    
    private boolean extractReferences = true;
    
    private boolean extractText = true;
    
    public static int THREADS_NUMBER = 3;

    public PdfNLMContentExtractor() throws AnalysisException {
        structureExtractor = new PdfBxStructureExtractor();
        metadataExtractor = new PdfNLMMetadataExtractor();
        referencesExtractor = new PdfNLMReferencesExtractor();
        textExtractor = new PdfNLMTextExtractor();
    }

    public PdfNLMContentExtractor(DocumentStructureExtractor structureExtractor, DocumentMetadataExtractor<Element> metadataExtractor, 
            DocumentReferencesExtractor<Element> referencesExtractor, DocumentTextExtractor<Element> textExtractor) {
        this.structureExtractor = structureExtractor;
        this.metadataExtractor = metadataExtractor;
        this.referencesExtractor = referencesExtractor;
        this.textExtractor = textExtractor;
    }
    
    
    /**
     * Extracts content from PDF file and stores it in NLM format.
     * 
     * @param stream
     * @return extracted content in NLM format
     * @throws AnalysisException 
     */
    @Override
    public Element extractContent(InputStream stream) throws AnalysisException {
        BxDocument document = structureExtractor.extractStructure(stream);
        return extractContent(document);
    }

    /**
     * Extracts content from a BxDocument and stores it in NLM format.
     * 
     * @param document
     * @return extracted content in NLM format
     * @throws AnalysisException 
     */
    @Override
    public Element extractContent(BxDocument document) throws AnalysisException {
        Element content = new Element("article");
        
        Element metadata = new Element("front");
        if (extractMetadata) {
            metadata = (Element) metadataExtractor.extractMetadata(document).getChild("front").clone();
        }
        content.addContent(metadata);
        
        Element text = new Element("body");
        if (extractText) {
            text = textExtractor.extractText(document);
        }
        content.addContent(text);
        
        Element back = new Element("back");
        Element refList = new Element("ref-list");
        if (extractReferences) {
            Element[] references = referencesExtractor.extractReferences(document);
            for (Element ref : references) {
                Element r = new Element("ref");
                r.addContent(ref);
                refList.addContent(r);
            }
        }
        back.addContent(refList);
        content.addContent(back);

        return content;
    }
    

    public void buildStructureExtractor(InputStream initialModel, InputStream initialRange) throws AnalysisException {
        structureExtractor = new PdfBxStructureExtractor(initialModel, initialRange);
    }
    
    public void buildMetadataExtractor(InputStream metadataModel, InputStream metadataRange) throws AnalysisException {
        metadataExtractor = new PdfNLMMetadataExtractor(metadataModel, metadataRange);
    }
    
    public void buildMetadataExtractor(ZoneClassifier zoneClassifier) throws AnalysisException {
        PdfNLMMetadataExtractor extractor = new PdfNLMMetadataExtractor();
        extractor.setMetadataClassifier(zoneClassifier);
        metadataExtractor = extractor;
    }
    
    public void buildReferencesExtractor(InputStream refModel) throws AnalysisException {
        referencesExtractor = new PdfNLMReferencesExtractor(refModel);
    }
    
    public void buildTextExtractor(InputStream filteringModel, InputStream filteringRange, 
            InputStream headerModel, InputStream headerRange) throws AnalysisException {
        textExtractor = new PdfNLMTextExtractor(filteringModel, filteringRange, headerModel, headerRange);
    }
    
    public PdfNLMContentExtractor(InputStream initialModel, InputStream initialRange, InputStream metadataModel, 
            InputStream metadataRange, InputStream refModel, InputStream filteringModel, InputStream filteringRange, 
            InputStream headerModel, InputStream headerRange) throws AnalysisException {
        structureExtractor = new PdfBxStructureExtractor(initialModel, initialRange);
        metadataExtractor = new PdfNLMMetadataExtractor(metadataModel, metadataRange);
        referencesExtractor = new PdfNLMReferencesExtractor(refModel);
        textExtractor = new PdfNLMTextExtractor(filteringModel, filteringRange, headerModel, headerRange);
    }

    public boolean isExtractMetadata() {
        return extractMetadata;
    }

    public void setExtractMetadata(boolean extractMetadata) {
        this.extractMetadata = extractMetadata;
    }

    public boolean isExtractReferences() {
        return extractReferences;
    }

    public void setExtractReferences(boolean extractReferences) {
        this.extractReferences = extractReferences;
    }

    public boolean isExtractText() {
        return extractText;
    }

    public void setExtractText(boolean extractText) {
        this.extractText = extractText;
    }

    public DocumentMetadataExtractor<Element> getMetadataExtractor() {
        return metadataExtractor;
    }

    public void setMetadataExtractor(DocumentMetadataExtractor<Element> metadataExtractor) {
        this.metadataExtractor = metadataExtractor;
    }

    public DocumentReferencesExtractor<Element> getReferencesExtractor() {
        return referencesExtractor;
    }

    public void setReferencesExtractor(DocumentReferencesExtractor<Element> referencesExtractor) {
        this.referencesExtractor = referencesExtractor;
    }

    public DocumentStructureExtractor getStructureExtractor() {
        return structureExtractor;
    }

    public void setStructureExtractor(DocumentStructureExtractor structureExtractor) {
        this.structureExtractor = structureExtractor;
    }

    public DocumentTextExtractor<Element> getTextExtractor() {
        return textExtractor;
    }

    public void setTextExtractor(DocumentTextExtractor<Element> textExtractor) {
        this.textExtractor = textExtractor;
    }
    
    public static void main(String[] args) throws AnalysisException, XPathExpressionException, JDOMException, IOException, ParseException, TransformationException {
        Options options = new Options();
        options.addOption("path", true, "file or directory path");
        options.addOption("ext", true, "metadata file extension");
        options.addOption("str", false, "store structure (TrueViz) files as well");
        options.addOption("strext", true, "structure file extension");
        options.addOption("modelmeta", true, "path to metadata classifier model");
        options.addOption("modelinit", true, "path to initial classifier model");
        
        CommandLineParser clParser = new GnuParser();
        CommandLine line = clParser.parse(options, args);
        String path = line.getOptionValue("path");
        String extension = "cermxml";
        if (line.hasOption("ext")) {
            extension = line.getOptionValue("ext");
        }
        boolean extractStr = line.hasOption("str");
        String strExtension = "cxml";
        if (line.hasOption("strext")) {
            strExtension = line.getOptionValue("strext");
        }
        String modelMeta = null;
        String modelMetaRange = null;
        if (line.hasOption("modelmeta")) {
            modelMeta = line.getOptionValue("modelmeta");
            modelMetaRange = line.getOptionValue("modelmeta")+".range";
        }
        String modelInit = null;
        String modelInitRange = null;
        if (line.hasOption("modelinit")) {
            modelInit = line.getOptionValue("modelinit");
            modelInitRange = line.getOptionValue("modelinit")+".range";
        }
        if (line.hasOption("threads")) {
            PdfNLMContentExtractor.THREADS_NUMBER = Integer.valueOf(line.getOptionValue("threads"));
        }
    	if (path == null){
            System.err.println("Usage: PdfNLMContentExtractor -path <path> [optional parameters]\n\n"
                             + "Tool for extracting metadata and content from PDF files.\n\n"
                             + "Arguments:\n"
                             + "  -path <path>              path to a PDF file or directory containing PDF files\n"
                             + "  -ext <extension>          (optional) the extension of the resulting metadata file;\n"
                             + "                            default: \"cermxml\"; used only if passed path is a directory\n"
                             + "  -modelmeta <path>         (optional) the path to the metadata classifier model file\n"
                             + "  -modelinit <path>         (optional) the path to the initial classifier model file\n"
                             + "  -str                      whether to store structure (TrueViz) files as well;\n"
                             + "                            used only if passed path is a directory\n"
                             + "  -strext <extension>       (optional) the extension of the structure (TrueViz) file;\n"
                             + "                            default: \"cxml\"; used only if passed path is a directory\n"
                             + "  -threads <num>            number of threads for parallel processing\n");
    		System.exit(1);
        }
 
        File file = new File(path);
        if (file.isFile()) {
            PdfNLMContentExtractor extractor = new PdfNLMContentExtractor();
            if ("alt-humanities".equals(modelMeta)) {
                extractor.buildMetadataExtractor(SVMAlternativeMetadataZoneClassifier.getDefaultInstance());
            } else if (modelMeta != null) {
                extractor.buildMetadataExtractor(new FileInputStream(modelMeta), new FileInputStream(modelMetaRange));
            }
            if (modelInit != null) {
                extractor.buildStructureExtractor(new FileInputStream(modelInit), new FileInputStream(modelInitRange));
            }
            InputStream in = new FileInputStream(file);
            Element result = extractor.extractContent(in);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            System.out.println(outputter.outputString(result));
        } else {
        
            Collection<File> files = FileUtils.listFiles(file, new String[]{"pdf"}, true);
    
            int i = 0;
            for (File pdf : files) {
                File xmlF = new File(pdf.getPath().replaceAll("pdf$", extension));
                if (xmlF.exists()) {
                    i++;
                    continue;
                }
 
                long start = System.currentTimeMillis();
            
                System.out.println(pdf.getName());
 
                PdfNLMContentExtractor extractor = new PdfNLMContentExtractor();
                if ("alt-humanities".equals(modelMeta)) {
                    extractor.buildMetadataExtractor(SVMAlternativeMetadataZoneClassifier.getDefaultInstance());
                } else if (modelMeta != null) {
                    extractor.buildMetadataExtractor(new FileInputStream(modelMeta), new FileInputStream(modelMetaRange));
                }
                if (modelInit != null) {
                    extractor.buildStructureExtractor(new FileInputStream(modelInit), new FileInputStream(modelInitRange));
                }
                InputStream in = new FileInputStream(pdf);
                BxDocument doc = extractor.structureExtractor.extractStructure(in);
                Element result = extractor.extractContent(doc);

                long end = System.currentTimeMillis();
                float elapsed = (end - start) / 1000F;
            
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                if (!xmlF.createNewFile()) {
                    System.out.println("Cannot create new file!");
                }
                FileUtils.writeStringToFile(xmlF, outputter.outputString(result));            
                
                if (extractStr) {
                    BxDocumentToTrueVizWriter writer = new BxDocumentToTrueVizWriter();
                    File strF = new File(pdf.getPath().replaceAll("pdf$", strExtension));
                    writer.write(new FileWriter(strF), doc.getPages());
                }
                
                i++;
                int percentage = i*100/files.size();
                System.out.println("Extraction time: " + Math.round(elapsed) + "s");
                System.out.println(percentage + "% done (" + i +" out of " + files.size() + ")");
                System.out.println("");
            }
        }
    }

}
