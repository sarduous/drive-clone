package com.staj.drive_clone;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "files")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String filePath;
    private Long fileSize;
    private Date uploadDate;
    private Date deleteDate;

    public FileMetadata() {
    }

    public FileMetadata(String fileName, String filePath, Long fileSize, Date uploadDate) {
        this.id = null;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.deleteDate = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    private int durum = 1;

    public int getDurum() {
        return durum;
    }

    public void setDurum(int durum) {
        this.durum = durum;
    }

    private String klasoryolu;

    public String getKlasoryolu() {
        return klasoryolu;
    }

    public void setKlasoryolu(String klasorYolu) {
        this.klasoryolu = klasorYolu;
    }

}