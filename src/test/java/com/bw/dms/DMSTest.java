package com.bw.dms;

// Playwright Imports
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// JUnit 5 Imports
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.qameta.allure.Attachment;
import org.junit.jupiter.api.AfterEach;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DMSTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;
    private Path tempFile;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        // setHeadless(false) sorgt dafür, dass sich ein sichtbares Browserfenster
        // öffnet
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() throws IOException {
        context = browser.newContext();
        page = context.newPage();

        // Eine temporäre Dummy-Datei für den Upload-Test erstellen
        tempFile = Paths.get("test-dokument.pdf");
        Files.writeString(tempFile, "Das ist der Inhalt eines automatisierten DMS-Testdokuments.");
    }

    @AfterEach
    void closeContext() throws IOException {
        context.close();
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("Sollte Kategorie erstellen, Dokument hochladen und Preview öffnen")
    void testFullDMSWorkflow() {
        // 1. React Frontend aufrufen
        page.navigate("http://localhost:5173");

        // Beispiel für Playwright-Screenshots bei Fehlern im Testcode
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/screenshot.png")));

        page.locator("#username").fill("admin");

        page.locator("#password").fill("password123");
        // 1. Klick ausführen (ggf. mit Force, falls der Button blockiert war)
        page.locator("#signin").click(new Locator.ClickOptions().setForce(true));

        // 2. Direkt auf das Element der Folgeseite warten – OHNE Netzwerk-Abfrage
        page.locator("#newcategory").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 2. Warten, bis das Element NACH dem Login im DOM erscheint und sichtbar ist
        // page.locator("#newcategory").waitFor(new
        // Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // page.waitForLoadState(LoadState.NETWORKIDLE);
        // 2. Kategorie anlegen

        // Locator categoryInput = page.getByPlaceholder("Kategoriename (z.B.
        // Rechnungen)...");
        page.locator("#newcategory").fill("Rechnungen_2026");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.locator("#addcategory").click();

        // Überprüfen, ob die Kategorie im Dropdown-Menü erscheint
        Locator selectDropdown = page.locator("select").first();
        selectDropdown.selectOption(new SelectOption().setLabel("Rechnungen_2026"));

        // 3. Datei hochladen (Hauptdokument)
        // Playwright füllt den Datei-Auswahldialog unsichtbar aus
        page.locator("#documentTitle").fill("Testdokument");
        page.setInputFiles("input[type='file']", tempFile);
        page.locator("#upload").click();

        // Überprüfen, ob das hochgeladene Dokument im Explorer/der Liste auftaucht
        // page.waitForCondition(() ->
        // page.locator("text=test-dokument.pdf").isVisible());
        // Alternativ als schneller Check, ob es ein Timing-Problem ist:
        page.waitForTimeout(1000);
        // Ersetze deine alte Zeile durch diese:
        // 1. Locator definieren (und das erste Element nehmen)
        Locator documentRow = page.getByText("Testdokument").first();

        // 2. KORREKTUR: Auf VISIBLE statt HIDDEN warten!
        // RICHTIG: Wartet darauf, dass es sichtbar wird
        // documentRow.waitFor(new
        // Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 3. Jetzt kannst du sicher damit interagieren
        documentRow.click();

        // documentRow.waitFor(); // Wartet darauf, dass die API antwortet und React neu
        // rendert
        // assertTrue(documentRow.isVisible(), "Das hochgeladene Dokument wird nicht in
        // der Liste angezeigt!");

        // 4. Preview-Funktionalität testen (Öffnet einen neuen Tab)
        // Da 'Preview' einen neuen Tab öffnet, fangen wir das "page"-Event ab
        // Ersetze "#preview", falls es im echten HTML z.B. eine Klasse wie
        // ".preview-btn" ist
        Locator firstPreview = page.locator("#preview").first();

        // 2. Warte, bis dieses erste Element überhaupt sichtbar/bereit ist
        firstPreview.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 3. Popup-Überwachung starten und exakt dieses erste Element anklicken
        Page popup = page.waitForPopup(() -> {
            firstPreview.click();
        });

        popup.waitForLoadState();
        // Überprüfen, ob die URL im neuen Tab auf den Spring-Boot-Vorschau-Endpunkt
        // zeigt
        assertTrue(popup.url().contains("/api/documents/preview/"), "Die Vorschau-URL ist fehlerhaft.");

        // Schließe den Preview-Tab
        popup.close();

        // 5. Dokument löschen
        // Registriere einen Listener, um das JavaScript-Bestätigungsfenster
        // (window.confirm) automatisch zu bejahen
        page.onceDialog(Dialog::accept);

        // Ersetze "#preview", falls es im echten HTML z.B. eine Klasse wie
        // ".preview-btn" ist
        Locator firstDelete = page.locator("#delete").first();

        // 2. Warte, bis dieses erste Element überhaupt sichtbar/bereit ist
        firstDelete.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 3. Popup-Überwachung starten und exakt dieses erste Element anklicken
        firstDelete.click();

        System.out.println("Starte das Löschen aller Testdokumente...");

        // 1. Wir holen uns den Locator für das Element
        Locator documentsToDelete = page.getByText("Testdokument");

        while (documentsToDelete.first().isVisible()) {
            page.waitForTimeout(1000); // 1 Sekunde Pause zum Zuschauen

            // 1. Aktuelle Anzahl der Dokumente auf der Seite merken
            int anzahlVorher = documentsToDelete.count();
            System.out.println("Dokumente vor dem Löschen: " + anzahlVorher);

            // 2. Zum passenden Lösch-Button navigieren
            Locator textElement = documentsToDelete.first();
            Locator parentContainer = textElement.locator(".. / ..");
            Locator deleteButton = parentContainer.locator("button#delete, button:has-text('Delete')").first();

            deleteButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            // 3. Dialog-Handler registrieren
            page.onceDialog(dialog -> {
                System.out.println("Browser-Dialog erfolgreich abgefangen: " + dialog.message());
                dialog.accept(); // Klickt im Browser-Popup auf "OK"
            });

            System.out.println("Klicke auf den Delete-Button...");
            deleteButton.click();

            // 4. KORREKTUR: Wir warten darauf, dass die Anzahl der Elemente schrumpft!
            // Playwright pollt hier elegant, bis die Bedingung (Anzahl verringert) erfüllt
            // ist
            page.waitForCondition(() -> documentsToDelete.count() == anzahlVorher - 1);

            System.out.println("Bestätigt: Ein Dokument wurde gelöscht. Verbleibend: " + documentsToDelete.count());
            page.waitForTimeout(1000);
        }

        System.out.println("Alle Testdokumente erfolgreich gelöscht!");

        // Sicherstellen, dass das Element wieder verschwindet
        // documentRow.waitFor(new
        // Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
        // assertFalse(documentRow.isVisible(), "Das Dokument wurde nach dem Klick auf
        // Delete nicht entfernt.");
    }

    /*
     * @AfterEach
     * void takeScreenshotOnFailure() {
     * // Pfad, wo Playwright das Bild kurzzeitig speichert
     * String screenshotPath = "target/failure-screenshot.png";
     * page.screenshot(new
     * Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
     * 
     * // Das Bild an den Allure-Bericht anhängen
     * attachScreenshot(screenshotPath);
     * }
     * 
     * @Attachment(value = "Page Screenshot", type = "image/png")
     * public byte[] attachScreenshot(String path) {
     * try {
     * return Files.readAllBytes(Paths.get(path));
     * } catch (Exception e) {
     * return new byte[0];
     * }
     * }
     */
}
