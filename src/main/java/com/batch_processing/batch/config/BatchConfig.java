package com.batch_processing.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
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
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.lang.NonNull;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.batch_processing.batch.student.Student;
import com.batch_processing.batch.student.StudentRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class BatchConfig {

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
                .skip(Exception.class)
                .retryLimit(10) // Retry limit for the step
                .retry(CannotAcquireLockException.class)
                // .skipLimit(10) // Add this to skip up to 10 items if necessary
                .listener(skipListener())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {
        return new JobBuilder("importStudents", jobRepository)
                .start(importStep())
                .build();
    }

    // ! till now i send data in synchornous mode;

    @Bean
    public TaskExecutor taskExecutor() {
        return new SyncTaskExecutor(); // Synchronous execution
    }

    // @Bean
    // public TaskExecutor taskExecutor() {
    // ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // executor.setCorePoolSize(5); // Minimum number of threads to keep alive
    // executor.setMaxPoolSize(10); // Maximum number of threads
    // executor.setQueueCapacity(25); // Capacity of the queue for tasks
    // executor.initialize(); // Initialize the executor
    // return executor;
    // }

    // ! due to this some data is getting lossed;
    // @Bean
    // public TaskExecutor taskExecutor() {
    // SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
    // asyncTaskExecutor.setConcurrencyLimit(500);
    // return asyncTaskExecutor;
    // }

    // ! skip listner

    @Bean
    public SkipListener<Student, Student> skipListener() {
        return new SkipListener<Student, Student>() {
            @Override
            public void onSkipInRead(@NonNull Throwable t) {
                log.error("Skipped during reading: " + t.getMessage());
            }

            @Override
            public void onSkipInWrite(@NonNull Student student, @NonNull Throwable t) {
                log.error("Skipped during writing: " + student);
            }

            @Override
            public void onSkipInProcess(@NonNull Student student, @NonNull Throwable t) {
                log.error("Skipped during processing: " + student);
            }
        };
    }

}
