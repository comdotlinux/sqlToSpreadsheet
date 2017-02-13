/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linux.sql.to.spreadsheet.batch;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.SuffixRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = DefaultBatchConfigurer.class)
@EnableBatchProcessing
@PropertySource("classpath:com/linux/application.properties")
public class Batch {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Batch.class);
    
    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    
    ;
    
    @Bean(name = "dataInputSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource getDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.batch.datasource")
    public DataSource getBatchDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "mysqlJdbcTemplate")
    protected JdbcTemplate mysqlJdbcTemplate(@Qualifier("dataInputSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    @StepScope
    protected FlatFileItemReader<String> itemReader(@Value("#{jobParameters['input.file']}") String inputFile) {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        final SuffixRecordSeparatorPolicy sepPolicy = new SuffixRecordSeparatorPolicy();
        sepPolicy.setSuffix(";");
        reader.setRecordSeparatorPolicy(sepPolicy);
        final DefaultLineMapper<String> lineMapper = new DefaultLineMapper<>();
        lineMapper.setFieldSetMapper((FieldSet fs) -> fs.getValues()[0]);
        lineMapper.setLineTokenizer((String line) -> new DefaultFieldSet(new String[]{line}));
        reader.setLineMapper(lineMapper);
        String fi = inputFile;
        LOG.info("job parameter input : {}", inputFile);
        if (null == inputFile || inputFile.isEmpty()) {
            fi = "src/main/resources/com/linux/sql/input.sql";
        }
        LOG.info("input file is now set to {}", fi);
        reader.setResource(new FileSystemResource(Paths.get(fi).toFile()));
        return reader;
    }

    @Bean
    public Job job() throws Exception {
        return this.jobs
                .get("job")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    @StepScope
    protected ItemProcessor<String, List<Map<String, Object>>> processor() {
        return new DatabaseQueryRunProcessor();
    }

    private static final String OVERRIDDEN_BY_EXPRESSION = null;
    @Bean
    protected Step step1() throws Exception {
        
        return this.steps
                .get("step1")
                .<String, List<Map<String, Object>>>chunk(1)
                .reader(itemReader(OVERRIDDEN_BY_EXPRESSION))
                .processor(processor())
                .writer(new SpreadsheetWriter())
                .build();
    }

    public static void main(String[] args) throws Exception {
        // System.exit is common for Batch applications since the exit code can be used to
        // drive a workflow
        System.exit(SpringApplication.exit(SpringApplication.run(Batch.class, args)));
    }
}
