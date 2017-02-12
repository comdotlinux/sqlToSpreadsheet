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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.SuffixRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableBatchProcessing
public class Batch {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Bean
    @ConfigurationProperties(prefix = "spring.batch.datasource")
    public DataSource getBatchDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource getDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    protected ItemReader<String> itemReader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        final SuffixRecordSeparatorPolicy sepPolicy = new SuffixRecordSeparatorPolicy();
        sepPolicy.setSuffix(";");
        reader.setRecordSeparatorPolicy(sepPolicy);
        final DefaultLineMapper<String> lineMapper = new DefaultLineMapper<>();
        lineMapper.setFieldSetMapper((FieldSet fs) -> fs.getValues()[0]);
        lineMapper.setLineTokenizer(new LineTokenizer() {
            @Override
            public FieldSet tokenize(String line) {
                return new DefaultFieldSet(new String[]{line});
            }

        });
        reader.setLineMapper(lineMapper);
        reader.setResource(new FileSystemResource(Paths.get("src/main/resources/com/linux/sql/input.sql").toFile()));
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
    protected ItemProcessor<String, List<Map<String, Object>>> processor() {
        return new DatabaseQueryRunProcessor();
    }

    @Bean
    protected Step step1() throws Exception {
        return this.steps
                .get("step1")
                .<String, List<Map<String, Object>>>chunk(1)
                .reader(itemReader())
                .processor(processor())
                .writer(new SpreadsheetWriter())
                .build();
    }

    public static void main(String[] args) throws Exception {
        // System.exit is common for Batch applications since the exit code can be used to
        // drive a workflow
        System.exit(SpringApplication.exit(SpringApplication.run(Batch.class, args)));
    }

//    @Bean
//    protected ItemWriter<List<Map<String, Object>>> writer() {
//        final FlatFileItemWriter<List<Map<String, Object>>> writer = new FlatFileItemWriter<>();
//        writer.setResource(new ClassPathResource("target/output.csv"));
//        writer.setShouldDeleteIfExists(true);
//        writer.setLineAggregator(lineAggregator());
//        return writer;
//    }
//    
//    @Bean
//    protected LineAggregator<List<Map<String, Object>>> lineAggregator() {
//        final DelimitedLineAggregator<List<Map<String, Object>>> lineAggregator = new DelimitedLineAggregator<>();
//        lineAggregator.setDelimiter(",");
//        lineAggregator.setFieldExtractor(fieldExtractor());
//        return lineAggregator;
//    }
//    
//    @Bean
//    protected FieldExtractor<List<Map<String, Object>>> fieldExtractor() {
//        return new FieldExtractor<List<Map<String, Object>>>(){
//            @Override
//            public Object[] extract(List<Map<String, Object>> t) {
//                Object[] 
//            }
//            
//        };
//    }
}
