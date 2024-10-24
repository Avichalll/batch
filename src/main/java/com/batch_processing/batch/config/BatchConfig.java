package com.batch_processing.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.batch_processing.batch.student.Student;
import com.batch_processing.batch.student.StudentRepository;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class BatchConfig {

    // @Value("${application.file.uploads.csv-file-path}")
    // private String filepath;

    public final JobRepository jobRepository;

    public final PlatformTransactionManager platformTransactionManager;
    @Autowired
    public final StudentRepository studentRepository;

    @Bean
    @StepScope
    public FlatFileItemReader<Student> itemReader(@Value("#{jobParameters['fullPathFileName']}") String dynamicPath) {

        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(dynamicPath));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setStrict(false);
        itemReader.setLineMapper(lineMapper());
        // SynchronizedItemStreamReader<Student> synchronizedReader = new
        // SynchronizedItemStreamReader<>();
        // synchronizedReader.setDelegate(reader);
        return itemReader;
    }

    @Bean
    public StudentProcessor processor() {
        return new StudentProcessor();
    }

    private LineMapper<Student> lineMapper() {
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstname", "lastname", "email");

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public RepositoryItemWriter<Student> writer() {
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(studentRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step importStep() {
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5); // Retry up to 5 times
        retryTemplate.setRetryPolicy(retryPolicy);

        return new StepBuilder("csvImport", jobRepository)
                .<Student, Student>chunk(10, platformTransactionManager)
                .reader(itemReader(null))
                .processor(processor())
                .writer(writer())
                .faultTolerant()
                .retryLimit(5) // Retry limit for the step
                .retry(CannotAcquireLockException.class)
                .taskExecutor(taskExecutor())
                .build();
    }

    // @Bean
    // public Step importStep(@Value("#{jobParameters['fullPathFileName']}") String
    // pathToFile) {
    // RetryTemplate retryTemplate = new RetryTemplate();
    // SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    // retryPolicy.setMaxAttempts(5); // Retry up to 5 times
    // retryTemplate.setRetryPolicy(retryPolicy);

    // return new StepBuilder("csvImport", jobRepository)
    // .<Student, Student>chunk(10, platformTransactionManager)
    // .reader(itemReader(pathToFile))
    // .processor(processor())
    // .writer(writer())
    // .faultTolerant()
    // .retryLimit(5) // Retry limit for the step
    // .retry(CannotAcquireLockException.class)
    // .taskExecutor(taskExecutor())
    // .build();
    // }

    @Bean
    public Job runJob() {
        return new JobBuilder("importStudents", jobRepository)
                .start(importStep())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Minimum number of threads to keep alive
        executor.setMaxPoolSize(10); // Maximum number of threads
        executor.setQueueCapacity(25); // Capacity of the queue for tasks
        executor.initialize(); // Initialize the executor
        return executor;
    }

    // @Bean
    // public TaskExecutor taskExecutor() {
    // SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
    // asyncTaskExecutor.setConcurrencyLimit(10);
    // return asyncTaskExecutor;
    // }

    // ! rendom code

    // // @Bean
    // public FlatFileItemReader<Student> itemReader(String pathToFile) {

    // FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
    // itemReader.setResource(new FileSystemResource(pathToFile));
    // itemReader.setName("csvReader");
    // itemReader.setLinesToSkip(1);
    // itemReader.setStrict(false);
    // itemReader.setLineMapper(lineMapper());
    // return itemReader;

    // }

    // @Bean
    // public StudentProcessor processor() {
    // return new StudentProcessor();
    // }

    // private LineMapper<Student> lineMapper() {

    // DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();
    // DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    // lineTokenizer.setDelimiter(",");
    // lineTokenizer.setStrict(false);
    // lineTokenizer.setNames("id", "firstname", "lastname", "email");
    // BeanWrapperFieldSetMapper<Student> fieldSetMapper = new
    // BeanWrapperFieldSetMapper<>();
    // fieldSetMapper.setTargetType(Student.class);
    // lineMapper.setLineTokenizer(lineTokenizer);
    // lineMapper.setFieldSetMapper(fieldSetMapper);
    // return lineMapper;

    // }

    // @Bean
    // public RepositoryItemWriter<Student> writer() {
    // RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
    // writer.setRepository(studentRepository);
    // writer.setMethodName("save");
    // return writer;
    // }

    // @Bean
    // public Step importStep() {
    // RetryTemplate retryTemplate = new RetryTemplate();
    // SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    // retryPolicy.setMaxAttempts(5); // Retry up to 5 times
    // retryTemplate.setRetryPolicy(retryPolicy);

    // return new StepBuilder("csvImport", jobRepository)
    // .<Student, Student>chunk(10, platformTransactionManager)
    // .reader(itemReader("")) // Pass the actual file path when executing
    // .processor(processor())
    // .writer(writer())
    // .faultTolerant()
    // .retryLimit(5) // Retry limit for the step
    // .retry(CannotAcquireLockException.class) // Retry on lock acquisition failure
    // .taskExecutor(taskExecutor())
    // .build();
    // }

    // // @Bean
    // // public Step importStep() {
    // // return new StepBuilder("csvImport", jobRepository)
    // // .<Student, Student>chunk(10, platformTransactionManager)
    // // .reader(itemReader(""))
    // // .processor(processor())
    // // .writer(writer())
    // // .taskExecutor(taskExecutor())
    // // .build();

    // // }

    // @Bean
    // public Job runJob() {
    // return new JobBuilder("importStudents", jobRepository)
    // .start(importStep())
    // .build();

    // }

    // @Bean
    // public TaskExecutor taskExecutor() {
    // SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
    // asyncTaskExecutor.setConcurrencyLimit(10);
    // return asyncTaskExecutor;
    // }

}
