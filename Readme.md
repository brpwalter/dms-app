# 📄 Dokumentenmanagementsystem (DMS)

Ein modernes, leichtgewichtiges Dokumentenmanagementsystem mit automatischer **Versionierung**, **Kategorie-Zuordnung**, **Volltextsuche** und Funktionen wie **Vorschau (Preview)** und **Download**.

## 🚀 Technologie-Stack

*   **Frontend:** React (JavaScript, Axios, HTML5/CSS3)
*   **Backend:** Java Spring Boot 3.x (Spring Web, Spring Data JPA)
*   **Datenbank:** PostgreSQL
*   **Speicher:** Lokales Dateisystem (erweiterbar auf Amazon S3)

---

## 🛠️ Features

*   📂 **Kategorie-Verwaltung:** Dokumente logisch in flexible Kategorien/Ordner einsortieren.
*   🔄 **Hierarchische Versionierung:** Zu jedem Dokument können neue Versionen (`v2`, `v3` etc.) hochgeladen werden, die visuell eingerückt unter dem Originaldokument erscheinen.
*   🔍 **Echtzeit-Suche & Filter:** Blitzschnelles Filtern nach Dateinamen oder Kategorien direkt im Frontend.
*   👁️ **Preview & Download:** PDFs und Bilder direkt im Browser-Tab ansehen oder mit Originalnamen herunterladen.
*   📦 **Verschieben & Löschen:** Dokumente nachträglich in andere Kategorien verschieben (`Move`) oder physisch sowie aus der Datenbank löschen.
*   📦 **Volltextsuche:** Dokumente nach Schlagwörtern durchsuchen.
---

## 📋 Voraussetzungen

Stelle sicher, dass die folgenden Tools auf deinem System installiert sind:
*   Java JDK 17 oder höher
*   Node.js (v18+) & npm
*   PostgreSQL-Datenbank server

---

## 🔧 Installations- & Startanleitung

### 1. Datenbank vorbereiten (PostgreSQL)
Erstelle eine neue Datenbank in deinem PostgreSQL-System (z. B. via pgAdmin oder psql):
```sql
CREATE DATABASE dms_db;
```

### 2. Backend starten (Spring Boot)
1. Navigiere in den Backend-Ordner.
2. Öffne die Datei `src/main/resources/application.properties` (oder `application.yml`) und passe deine PostgreSQL-Zugangsdaten an:
   ```properties
   spring.application.name=dms
   spring.datasource.url=jdbc:postgresql://localhost:5463/dms_db
   spring.datasource.username=dein_username
   spring.datasource.password=dein_passwort
   spring.jpa.hibernate.ddl-auto=update
   ```
3. Starte die App über deine IDE (IntelliJ/Eclipse) oder über das Terminal:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Das Backend läuft standardmäßig unter `http://localhost:8080`.*

### 3. Frontend starten (React)
1. Navigiere in den Frontend-Ordner.
2. Installiere die benötigten Abhängigkeiten:
   ```bash
   npm install
   ```
3. Starte den Entwicklungsserver:
   ```bash
   npm start
   ```
   *Das Frontend öffnet sich automatisch unter `http://localhost:3000`.*

---

## 📂 Ordnerstruktur & Speicherort

*   **Metadaten:** Alle strukturierten Daten (Titel, Versionen, Pfade, Kategorien) werden in den PostgreSQL-Tabellen `documents` und `categories` verwaltet.
*   **Physische Dateien:** Hochgeladene Dateien werden im Stammverzeichnis des Backends im automatisch generierten Ordner `/uploaded-files` abgelegt.

---

## 🔌 API-Endpunkte (REST)

### Dokumente (`/api/documents`)
*   `GET /api/documents` – Holt alle Dokumente inklusive Metadaten.
*   `POST /api/documents` – Lädt ein neues Hauptdokument hoch (`MultipartFile`).
*   `POST /api/documents/version` – Lädt eine neue Version zu einem bestehenden Dokument hoch.
*   `GET /api/documents/preview/{id}` – Öffnet die Datei direkt als Inline-Vorschau im Browser.
*   `GET /api/documents/download/{id}` – Initiiert den Datei-Download.
*   `PUT /api/documents/{id}/move` – Verschiebt ein Dokument in eine andere Kategorie (via Query-Parameter `categoryId`).
*   `DELETE /api/documents/{id}` – Löscht die Datei vom Server und den Eintrag aus der DB.

### Kategorien (`/api/categories`)
*   `GET /api/categories` – Listet alle Kategorien auf.
*   `POST /api/categories` – Erstellt eine neue Kategorie (`{ "name": "Ordnername" }`).
---
mvn clean test
mvn allure:serve
