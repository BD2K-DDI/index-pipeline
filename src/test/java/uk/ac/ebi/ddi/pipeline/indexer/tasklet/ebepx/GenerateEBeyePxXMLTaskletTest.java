package uk.ac.ebi.ddi.pipeline.indexer.tasklet.ebepx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:ddi-indexer-pipeline-test-context.xml"})
public class GenerateEBeyePxXMLTaskletTest {

    public static final String INDEXER_PARAMETER = "inderxer.param";
    public static final String TEST_MODE = "test.mode";


    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;


    private JobParameters jobParameters;


    @Before
    public void setUp() throws Exception {
        initJobParameters();
    }


    private void initJobParameters() {
        this.jobParameters =  new JobParametersBuilder().addString(INDEXER_PARAMETER,INDEXER_PARAMETER)
                .addString(TEST_MODE, "true")
                .toJobParameters();
    }

    @Test
    public void testLaunchJobWithJobLauncher() throws Exception {
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}