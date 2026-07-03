package com.staj.drive_clone;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "paths", required = false) String[] paths,
            @RequestParam(value = "mevcutKlasoreEkle", defaultValue = "false") boolean mevcutKlasoreEkle) {// ana
                                                                                                           // sayfada mı
                                                                                                           // yükleme
                                                                                                           // yaptım
                                                                                                           // yoksa bir
                                                                                                           // klasörün
                                                                                                           // içinde mi

        if (files.length == 0 || files[0].isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hata: Lütfen en az bir dosya seçin!");
        }

        try {
            // yeni klasör yükleniyorsa çakışma kontrolü
            if (!mevcutKlasoreEkle && paths != null && paths.length > 0) {
                String originalRoot = null;
                for (String p : paths) {
                    if (p != null && !p.isEmpty()) {
                        originalRoot = p.split("/")[0];
                        break;
                    }
                }

                if (originalRoot != null) {
                    List<FileMetadata> mevcutDosyalar = fileService.getAlldurumaktif();
                    String newRoot = originalRoot;
                    int counter = 1;
                    boolean nameExists = true;

                    while (nameExists) {
                        nameExists = false;
                        for (FileMetadata fm : mevcutDosyalar) {
                            String dbYol = fm.getKlasoryolu();
                            if (dbYol != null && (dbYol.equals(newRoot) || dbYol.startsWith(newRoot + "/"))) {
                                nameExists = true;
                                break;
                            }
                        }
                        if (nameExists) {
                            newRoot = originalRoot + "(" + counter + ")";
                            counter++;
                        }
                    }

                    if (!newRoot.equals(originalRoot)) {
                        for (int i = 0; i < paths.length; i++) {
                            if (paths[i] != null && paths[i].startsWith(originalRoot)) {
                                paths[i] = newRoot + paths[i].substring(originalRoot.length());
                            }
                        }
                    }
                }
            }
            // aynı klasör kontrol bitiş

            int yuklenendosyasayisi = 0;
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (!file.isEmpty()) {

                    String klasorYolu = (paths != null && paths.length > i && paths[i] != null && !paths[i].isEmpty())
                            ? paths[i]
                            : "";

                    // çakışma varsa (1)li hali
                    fileService.uploadFile(file, klasorYolu);
                    yuklenendosyasayisi++;
                }
            }

            return ResponseEntity.ok(yuklenendosyasayisi + " adet dosya başarıyla yüklendi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Dosyalar yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileMetadata>> listAllFiles() {
        List<FileMetadata> files = fileService.getAlldurumaktif();
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable("id") Long id) {
        try {
            String result = fileService.dosyaCope(id);
            if (result.startsWith("Hata")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hata: " + e.getMessage());
        }
    }

    @DeleteMapping("/force-delete/{id}")
    public ResponseEntity<String> forceDeleteFile(@PathVariable("id") Long id) {
        try {
            String result = fileService.kalicisil(id);

            if (result.startsWith("Hata")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Silme işlemi sırasında hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/bin-list")
    public ResponseEntity<List<FileMetadata>> listBinFiles() {
        List<FileMetadata> binFiles = fileService.getAlldurumpasif();
        return ResponseEntity.ok(binFiles);
    }

    @PutMapping("/restore") // artık id kısmı yok
    public ResponseEntity<String> restoreFile(@RequestBody RestoreRequest request) {
        try {
            // id bilgisini artık url den değil json dan al
            fileService.dosyaGeriYukle(request.getId());
            return ResponseEntity.ok("Dosya başarıyla geri yüklendi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Geri yükleme sırasında hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> openFile(@PathVariable Long id) {
        try {
            FileMetadata dosyaBilgisi = fileService.dosyainfosu(id);

            Path dosyaYolu = Paths.get(dosyaBilgisi.getFilePath());
            Resource resource = new UrlResource(dosyaYolu.toUri());

            String dosyaTuru = Files.probeContentType(dosyaYolu);
            if (dosyaTuru == null) {
                dosyaTuru = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(dosyaTuru))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dosyaBilgisi.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create-folder")
    public ResponseEntity<String> createfolder(
            @RequestParam("folderName") String folderName,
            @RequestParam(value = "currentPath", defaultValue = "") String currentPath) {
        try {
            String mesaj = fileService.bosklasor(folderName, currentPath);
            return ResponseEntity.ok(mesaj);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Klasör oluşturulamadı: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-folder")
    public ResponseEntity<String> deleteFolder(@RequestParam("folderPath") String folderPath) {
        try {
            String result = fileService.klasorCope(folderPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Klasör silinirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PutMapping("/restore-folder")
    public ResponseEntity<String> restoreFolder(@RequestParam("folderPath") String folderPath) {
        try {
            String result = fileService.klasorGeriYukle(folderPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Klasör geri yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    @DeleteMapping("/force-delete-folder")
    public ResponseEntity<String> forceDeleteFolder(@RequestParam("folderPath") String folderPath) {
        try {
            String result = fileService.klasorkalicisil(folderPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Klasör kalıcı olarak silinirken hata oluştu: " + e.getMessage());
        }

    }

    public static class RestoreRequest {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

}