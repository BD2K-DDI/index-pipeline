package uk.ac.ebi.ddi.pipeline.indexer.tasklet.annotation;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.Assert;
import uk.ac.ebi.ddi.annotation.service.dataset.DDIDatasetAnnotationService;
import uk.ac.ebi.ddi.pipeline.indexer.tasklet.AbstractTasklet;
import uk.ac.ebi.ddi.service.db.model.dataset.DatasetSimilars;
import uk.ac.ebi.ddi.service.db.model.dataset.SimilarDataset;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 *  ==Overview==
 *
 *  This class
 *
 * Created by ypriverol (ypriverol@gmail.com) on 18/07/2016.
 */

/** If a similar datasets is removed from the Sdataset Table it should be updated in the
 *  similars dataset Table.
 **/

public class CleanEmptySimilarTasklet extends AbstractTasklet{

    DDIDatasetAnnotationService datasetAnnotationService;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        List<DatasetSimilars> datasetSimilars = datasetAnnotationService.getDatasetSimilars();
        if(datasetSimilars != null && !datasetSimilars.isEmpty()){
            for (DatasetSimilars dataset : datasetSimilars) {
                Set<SimilarDataset> toRemove = new HashSet<>();
                Set<SimilarDataset> newSimilars = new HashSet<>();
                for (SimilarDataset datasetSimilar : dataset.getSimilars()) {
                    if (datasetSimilar.getSimilarDataset() == null)
                        toRemove.add(datasetSimilar);
                    else
                        newSimilars.add(datasetSimilar);
                }
                if (toRemove.size() == dataset.getSimilars().size()) {
                    datasetAnnotationService.removeSimilar(dataset);
                } else if (!toRemove.isEmpty()) {
                    datasetAnnotationService.updateDatasetSimilars(dataset.getAccession(), dataset.getDatabase(), newSimilars);
                }
            }
        }
        return RepeatStatus.FINISHED;
    }



    public DDIDatasetAnnotationService getDatasetAnnotationService() {
        return datasetAnnotationService;
    }

    public void setDatasetAnnotationService(DDIDatasetAnnotationService datasetAnnotationService) {
        this.datasetAnnotationService = datasetAnnotationService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(datasetAnnotationService, "The dataset annotation object can't be null");
    }
}
