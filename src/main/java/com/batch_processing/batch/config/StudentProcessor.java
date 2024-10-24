package com.batch_processing.batch.config;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.batch_processing.batch.student.Student;

public class StudentProcessor implements ItemProcessor<Student, Student> {

    @Override
    @Nullable
    public Student process(@NonNull Student student) throws Exception {

        System.out.println("Processing student: " + student);
        return student;
    }

}
