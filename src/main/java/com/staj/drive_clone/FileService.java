package com.staj.drive_clone;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final String FOLDER_PATH = "C:\\drive_uploads\\";
    private final FileMetadataRepository fileRepository;

    public FileService(FileMetadataRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public String uploadFile(MultipartFile file, String klasorYolu) throws IOException {

        File directory = new File(FOLDER_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String mainFilename = file.getOriginalFilename();

        // sadece dosya adını al
        if (mainFilename != null && mainFilename.contains("/")) {
            mainFilename = mainFilename.substring(mainFilename.lastIndexOf("/") + 1);
        }
        if (mainFilename != null && mainFilename.contains("\\")) {
            mainFilename = mainFilename.substring(mainFilename.lastIndexOf("\\") + 1);
        }

        if (mainFilename == null) {
            mainFilename = "bilinmeyen_dosya";
        }

        String baseName = mainFilename;
        String extension = "";
        int dotIndex = mainFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            baseName = mainFilename.substring(0, dotIndex);
            extension = mainFilename.substring(dotIndex);
        }

        // dosyalar sadece driveuploadsa düştüğü için ezilmemesi adına fiziksel isim
        int diskCounter = 1;
        String physicalName = mainFilename;
        File checkFile = new File(FOLDER_PATH + physicalName);

        while (checkFile.exists()) {
            physicalName = baseName + "_" + diskCounter + extension; // _1
            checkFile = new File(FOLDER_PATH + physicalName);
            diskCounter++;
        }

        String filePath = FOLDER_PATH + physicalName;
        Path targetLocation = Paths.get(filePath);
        Files.copy(file.getInputStream(), targetLocation);

        // dosyayı diske yazma
        int virtualCounter = 1;
        String displayFilename = mainFilename;
        boolean nameExists = true;
        List<FileMetadata> mevcutDosyalar = fileRepository.findByDurum(1);

        String guvenliKlasorYolu = (klasorYolu == null) ? "" : klasorYolu;

        while (nameExists) {
            nameExists = false;
            for (FileMetadata fm : mevcutDosyalar) {
                String dbKlasorYolu = (fm.getKlasoryolu() == null) ? "" : fm.getKlasoryolu();

                // klasör içi iki aynı ad
                if (dbKlasorYolu.equals(guvenliKlasorYolu) && fm.getFileName().equals(displayFilename)) {
                    nameExists = true;
                    break;
                }
            }
            if (nameExists) {
                // sadece aynı klasörde varsa (1) ekle ve tekrar kontrol
                displayFilename = baseName + "(" + virtualCounter + ")" + extension;
                virtualCounter++;
            }
        }

        FileMetadata metadata = new FileMetadata(
                displayFilename,
                filePath,
                file.getSize(),
                new Date());
        metadata.setKlasoryolu(klasorYolu);

        fileRepository.save(metadata);// veritabanına kaydetme
        return "Dosya başarıyla yüklendi: " + displayFilename;
    }

    public List<FileMetadata> getAlldurumaktif() {
        return fileRepository.findByDurum(1);
    }

    public List<FileMetadata> getAlldurumpasif() {
        return fileRepository.findByDurum(0);
    }

    public String dosyaCope(Long id) {
        Optional<FileMetadata> fileOptional = fileRepository.findById(id);

        if (fileOptional.isPresent()) {
            FileMetadata fileMetadata = fileOptional.get();
            fileMetadata.setDurum(0);
            fileMetadata.setDeleteDate(new Date());
            fileRepository.save(fileMetadata);

            return "Dosya başarıyla çöp kutusuna taşındı.";
        }

        return "Hata: Dosya veritabanında bulunamadı!";
    }

    public String klasorCope(String klasorYolu) {
        List<FileMetadata> aktifDosyalar = fileRepository.findByDurum(1);
        boolean keepDosyasiVar = false;

        for (FileMetadata fm : aktifDosyalar) {
            String dbYol = fm.getKlasoryolu();

            if (dbYol != null && (dbYol.equals(klasorYolu) || dbYol.startsWith(klasorYolu + "/"))) {
                fm.setDurum(0);
                fm.setDeleteDate(new Date());
                fileRepository.save(fm);

                if (fm.getFileName().equals(".keep") && dbYol.equals(klasorYolu + "/.keep")) {
                    keepDosyasiVar = true;
                }
            }
        }

        // bilgisayardaki yüklenen klasör çöpe atılırsa çöp sekmesinde .keep atıyor
        if (!keepDosyasiVar) {
            FileMetadata dummy = new FileMetadata(
                    ".keep",
                    "VIRTUAL_FILE",
                    0L,
                    new Date());
            dummy.setKlasoryolu(klasorYolu + "/.keep");
            dummy.setDurum(0);
            dummy.setDeleteDate(new Date());
            fileRepository.save(dummy);
        }

        return "Klasör başarıyla çöp kutusuna taşındı.";
    }

    public String dosyaGeriYukle(Long id) {
        Optional<FileMetadata> fileOptional = fileRepository.findById(id);

        if (fileOptional.isPresent()) {
            FileMetadata fileMetadata = fileOptional.get();
            fileMetadata.setDurum(1);
            fileRepository.save(fileMetadata);

            return "Dosya başarıyla geri yüklendi.";
        }

        return "Hata: Dosya veritabanında bulunamadı!";
    }

    public String klasorGeriYukle(String klasorYolu) {
        List<FileMetadata> pasifDosyalar = fileRepository.findByDurum(0);
        int kurtarilanSayisi = 0;

        for (FileMetadata fm : pasifDosyalar) {
            String dbYol = fm.getKlasoryolu();
            if (dbYol != null && (dbYol.equals(klasorYolu) || dbYol.startsWith(klasorYolu + "/"))) {
                fm.setDurum(1);
                fm.setDeleteDate(null);
                fileRepository.save(fm);
                kurtarilanSayisi++;
            }
        }
        return kurtarilanSayisi + " adet öge başarıyla Dosyalarım'a geri yüklendi.";
    }

    public String klasorkalicisil(String klasorYolu) {
        List<FileMetadata> pasifDosyalar = fileRepository.findByDurum(0);

        for (FileMetadata fm : pasifDosyalar) {
            String dbYol = fm.getKlasoryolu();
            if (dbYol != null && (dbYol.equals(klasorYolu) || dbYol.startsWith(klasorYolu + "/"))) {

                // eğer ghost file değilse sil
                if (!fm.getFileName().equals(".keep")) {
                    File physicalFile = new File(fm.getFilePath());
                    if (physicalFile.exists()) {
                        physicalFile.delete();
                    }
                }

                // veritabanından sil
                fileRepository.delete(fm);
            }
        }
        return "Klasör başarıyla silindi.";
    }

    public String kalicisil(Long id) {
        Optional<FileMetadata> fileOptional = fileRepository.findById(id);

        if (fileOptional.isPresent()) {
            FileMetadata fileMetadata = fileOptional.get();

            File physicalFile = new File(fileMetadata.getFilePath());
            if (physicalFile.exists()) {
                physicalFile.delete();
            }

            fileRepository.deleteById(id);

            return "Dosya başarıyla silindi.";
        }

        return "Hata: Dosya veritabanında bulunamadı!";
    }

    public FileMetadata dosyainfosu(Long id) {
        Optional<FileMetadata> fileOptional = fileRepository.findById(id);
        if (fileOptional.isPresent()) {
            return fileOptional.get();
        }
        throw new RuntimeException("Hata: Dosya veritabanında bulunamadı!");
    }

    public String bosklasor(String klasorAdi, String mevcutKlasor) {
        // olduğumuz klasörün tam yolu
        String tamYol = (mevcutKlasor == null || mevcutKlasor.isEmpty())
                ? klasorAdi
                : mevcutKlasor + "/" + klasorAdi;

        FileMetadata dummy = new FileMetadata(
                ".keep", // gizli isim
                "VIRTUAL_FILE",
                0L,
                new Date());

        // klasör/.keep olarak kaydediyoruz
        dummy.setKlasoryolu(tamYol + "/.keep");

        fileRepository.save(dummy);

        return "Klasör başarıyla oluşturuldu.";
    }

}