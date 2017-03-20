package com.linux.batch;

import java.util.LinkedHashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author comdotlinux
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:job.xml", "classpath*:test-context.xml"})
public class BatchTestIT {
    
    @Autowired
    private JobLauncherTestUtils jobLauncher;
    private JobParameters jobParameters;
    
    @Before
    public void setUp() {
        Map<String, JobParameter> parameters = new LinkedHashMap<String, JobParameter>();
        parameters.put("input.file.name", new JobParameter("./input.txt"));
        parameters.put("output.dir", new JobParameter("./target"));
        parameters.put("output.file.name", new JobParameter("consolidated"));
        parameters.put("timestamp.in.output.file.name", new JobParameter(1l));
        
        this.jobParameters = new JobParameters(parameters);
    }
    
    @Test
    public void testjob() throws Exception {
        JobExecution execution = jobLauncher.launchJob(jobParameters);
        BatchStatus status = execution.getStatus();
        assertThat(status, is(BatchStatus.COMPLETED));
    }
    
}
