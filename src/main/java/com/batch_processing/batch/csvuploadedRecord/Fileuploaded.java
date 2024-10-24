package com.batch_processing.batch.csvuploadedRecord;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "FileUpload")
@EntityListeners(AuditingEntityListener.class)
public class Fileuploaded {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "file should not be null")
    private String file;

    // @CreatedDate
    // @Column(insertable = false, nullable = false)
    // private LocalDateTime cratedDate;

    // @LastModifiedDate
    // @Column(insertable = false)
    // private LocalDateTime updatedDate;

}
