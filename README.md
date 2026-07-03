# ☁️ Drive Clone (File Management System)

This project is a web-based clone of a cloud file management system similar to Google Drive. It allows users to upload files and folders, create new folders, view their data in a hierarchical structure, and perform safe deletion operations using a trash bin mechanism.

The project was developed in accordance with modern REST API architecture (DTO, `@RequestBody` usage) and "Separation of Concerns" principles on the backend.

## 🚀 Features

* **📁 Advanced Upload:** Support for single/multiple file and full folder uploads.
* **📂 Folder Management:** Creating nested folder hierarchies and navigating between these folders.
* **🗑️ Trash Bin Mechanism:** Temporarily deleting files and folders (Soft Delete), restoring them when desired, or permanently destroying them (Hard Delete).
* **📄 Preview:** Viewing supported files (PDFs, Images, etc.) directly in the browser.
* **🕰️ Date and Size Tracking:** Real-time tracking of size and upload/deletion dates of uploaded items.
* **🗂️ Safe Conflict Control:** Automatic renaming to prevent overwriting when a file or folder with the same name is uploaded (e.g., `file(1).txt`).

## 🛠️ Technologies Used

**Backend (Server):**
* Java 
* Spring Boot
* Spring Data JPA
* Modern RESTful API Architecture (JSON / DTO pattern)
* Multipart File Handling (FormData)

**Frontend (Client):**
* HTML5, CSS3
* Vanilla JavaScript (Fetch API)
* **Libraries:** SweetAlert2 (Modern alert modals), FontAwesome (Icons)

## 🏗️ Architectural Structure (Backend)
The project is built with a layered architecture in accordance with clean code principles:
* **`FileController` (The Gateway):** Handles HTTP requests (`GET`, `POST`, `PUT`, `DELETE`). Reads incoming JSON or FormData payloads (via DTOs) and passes them to the Service layer.
* **`FileService` (The Core Logic):** Where all business logic operates. Physical file writing/deleting, conflict checks, and database calculations are handled here.
* **`FileMetadata` (Entity):** The Java representation of the `files` table in the database.
* **DTO (Data Transfer Object):** Special carrier classes (e.g., `FolderRequest`, `RestoreRequest`) used to handle incoming requests from the outside world safely and lightly.

## ⚙️ Setup and Installation

To run the project in your local environment (localhost), you can follow the steps below:

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/YOUR_USERNAME/drive-clone.git](https://github.com/YOUR_USERNAME/drive-clone.git)
   ```

2. **Check the Physical File Path:**
   For the application to work properly, make sure the physical save path defined in `FileService.java` exists on your computer or update it according to your system:
   ```java
   private final String FOLDER_PATH = "C:\\drive_uploads\\";
   ```
   *(If a folder named `drive_uploads` does not exist on your `C:` drive, the application will automatically create it upon the first file upload.)*

3. **Run the Application:**
   Run the `DriveCloneApplication.java` class via your preferred IDE (IntelliJ IDEA, VS Code, Eclipse, etc.).

4. **Open in Browser:**
   Once the application is up and running, navigate to the following address in your browser:
   `http://localhost:8080/` (or your specified port)
