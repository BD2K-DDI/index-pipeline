package uk.ac.ebi.ddi.pipeline.indexer.tasklet.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import uk.ac.ebi.ddi.annotation.model.DatasetTobeEnriched;
import uk.ac.ebi.ddi.annotation.model.EnrichedDataset;
import uk.ac.ebi.ddi.annotation.service.DDIAnnotationService;
import uk.ac.ebi.ddi.annotation.service.DDIExpDataImportService;
import uk.ac.ebi.ddi.annotation.utils.DataType;
import uk.ac.ebi.ddi.pipeline.indexer.io.DDIFile;
import uk.ac.ebi.ddi.pipeline.indexer.tasklet.AbstractTasklet;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Reference;
import uk.ac.ebi.ddi.xml.validator.utils.Field;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 19/10/15
 */
public class AnnotationXMLTasklet extends AbstractTasklet{

    public static final Logger logger = LoggerFactory.getLogger(AnnotationXMLTasklet.class);

    Resource outputDirectory;

    Resource inputDirectory;

    DDIAnnotationService annotationService;

    int numberEntries;

    String prefixFile;

    DataType dataType;

    private DDIExpDataImportService ddiExpDataImportService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        List<Entry> listToPrint = new ArrayList<>();
        int counterFiles = 1;

        if(inputDirectory != null && inputDirectory.getFile() != null && inputDirectory.getFile().isDirectory()){
            for(File file: inputDirectory.getFile().listFiles()){
                OmicsXMLFile reader = new OmicsXMLFile(file);
                for(String id: reader.getEntryIds()){
                    logger.info("The ID: " + id + " will be enriched!!");
                    Entry dataset = reader.getEntryById(id);
                    DatasetTobeEnriched datasetTobeEnriched = new DatasetTobeEnriched(dataset.getAcc(),dataset.getAdditionalFieldValue(Field.REPOSITORY.getName()),
                            dataset.getName().getValue(), dataset.getDescription(), dataset.getAdditionalFieldValue(Field.SAMPLE.getName()),
                            dataset.getAdditionalFieldValue(Field.DATA.getName()));
                    EnrichedDataset enrichedDataset1 = annotationService.enrichment(datasetTobeEnriched);

                    String entryId = dataset.getId();
                    List<Reference> refs = dataset.getCrossReferences().getRef();
                    ddiExpDataImportService.importDataset(dataType.getName(), entryId, refs);

                    dataset.addAdditionalField(Field.ENRICH_TITLE.getName(), enrichedDataset1.getEnrichedTitle());
                    dataset.addAdditionalField(Field.ENRICH_ABSTRACT.getName(), enrichedDataset1.getEnrichedAbstractDescription());
                    dataset.addAdditionalField(Field.ENRICH_SAMPLE.getName(), enrichedDataset1.getEnrichedSampleProtocol());
                    dataset.addAdditionalField(Field.ENRICH_DATA.getName(), enrichedDataset1.getEnrichedDataProtocol());
                    logger.debug(enrichedDataset1.getEnrichedTitle());
                    logger.debug(enrichedDataset1.getEnrichedAbstractDescription());
                    logger.debug(enrichedDataset1.getEnrichedSampleProtocol());
                    logger.debug(enrichedDataset1.getEnrichedDataProtocol());

                    listToPrint.add(dataset);

                    if(listToPrint.size() == numberEntries){
                        DDIFile.writeList(reader, listToPrint, prefixFile, counterFiles, outputDirectory.getFile());
                        listToPrint.clear();
                        counterFiles++;
                    }
                }
                // This must be printed before leave because it contains the end members of the list.
                if(!listToPrint.isEmpty()){
                    DDIFile.writeList(reader, listToPrint, prefixFile, counterFiles, outputDirectory.getFile());
                    listToPrint.clear();
                    counterFiles++;
                }
            }
        }
        return RepeatStatus.FINISHED;

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(inputDirectory, "Input Directory can not be null");
        Assert.notNull(outputDirectory, "Output Directory cant be null");
    }

    public Resource getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Resource outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Resource getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(Resource inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public DDIAnnotationService getAnnotationService() {
        return annotationService;
    }

    public void setAnnotationService(DDIAnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    public int getNumberEntries() {
        return numberEntries;
    }

    public void setNumberEntries(int numberEntries) {
        this.numberEntries = numberEntries;
    }

    public String getPrefixFile() {
        return prefixFile;
    }

    public void setPrefixFile(String prefixFile) {
        this.prefixFile = prefixFile;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public DDIExpDataImportService getDdiExpDataImportService() {
        return ddiExpDataImportService;
    }

    public void setDdiExpDataImportService(DDIExpDataImportService ddiExpDataImportService) {
        this.ddiExpDataImportService = ddiExpDataImportService;
    }
}