package com.batch_processing.batch.student;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.batch_processing.batch.File.FileStorageService;
import com.batch_processing.batch.csvuploadedRecord.FileuploadRespository;
import com.batch_processing.batch.csvuploadedRecord.Fileuploaded;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/student")
@Slf4j
@RequiredArgsConstructor
public class StudentController {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Autowired
    private final FileuploadRespository fileuploadRespository;
    @Autowired
    private final FileStorageService fileStorageService;

    // @PostMapping("/")
    // public void importCsvToDBJob() {
    // JobParameters jobParameters = new JobParametersBuilder()
    // .addLong("startAt", System.currentTimeMillis())
    // .toJobParameters();

    // try {
    // jobLauncher.run(job, jobParameters);
    // } catch (JobExecutionAlreadyRunningException | JobRestartException |
    // JobInstanceAlreadyCompleteException
    // | JobParametersInvalidException e) {
    // e.printStackTrace();
    // }

    // }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public String importCsvToDBJob(@RequestParam("file") MultipartFile file) {

        // fileStorageService.saveFile(file, "Student");

        Fileuploaded fileuploaded = new Fileuploaded();
        fileuploaded.setFile(fileStorageService.saveFile(file, "Student").replace("\\", "/"));

        fileuploadRespository.save(fileuploaded);

        String filepathofcsv = fileuploaded.getFile();
        log.info("file path of file of csv" + filepathofcsv);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fullPathFileName", filepathofcsv)
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        log.info("file passd to Jobparameter successfully");

        try {
            jobLauncher.run(job, jobParameters);
            log.info("job Launch Successfully");
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            e.printStackTrace();

        }

        return "saved";
    }

    @PostMapping(value = "/addData")
    public String importAllCsvToDBJob() {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fullPathFileName", "Data/Student/Data/Student/1729798084935.csv")
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        log.info("file passd to Jobparameter successfully");

        try {
            jobLauncher.run(job, jobParameters);
            log.info("job Launch Successfully");
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            log.error("print error message" + e);

        }

        return "saved";
    }

}
