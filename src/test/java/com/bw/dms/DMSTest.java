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
        // setHeadless(false) sorgt dafür, dass sich ein sichtbares Browserfenster öffnet
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

page.waitForLoadState(LoadState.NETWORKIDLE);
        // 2. Kategorie anlegen
        Locator categoryInput = page.getByPlaceholder("Neue Kategorie...");
        categoryInput.fill("Rechnungen_2026");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Hinzufügen")).click();

        // Überprüfen, ob die Kategorie im Dropdown-Menü erscheint
        Locator selectDropdown = page.locator("select").first();
        selectDropdown.selectOption(new SelectOption().setLabel("Rechnungen_2026"));

        // 3. Datei hochladen (Hauptdokument)
        // Playwright füllt den Datei-Auswahldialog unsichtbar aus
        page.setInputFiles("input[type='file']", tempFile);

        // Überprüfen, ob das hochgeladene Dokument im Explorer/der Liste auftaucht
        Locator documentRow = page.locator("text=" + tempFile.getFileName().toString());
        documentRow.waitFor(); // Wartet darauf, dass die API antwortet und React neu rendert
        assertTrue(documentRow.isVisible(), "Das hochgeladene Dokument wird nicht in der Liste angezeigt!");

        // 4. Preview-Funktionalität testen (Öffnet einen neuen Tab)
        // Da 'Preview' einen neuen Tab öffnet, fangen wir das "page"-Event ab
        Page popup = page.waitForPopup(() -> {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Preview")).first().click();
        });

        popup.waitForLoadState();
        // Überprüfen, ob die URL im neuen Tab auf den Spring-Boot-Vorschau-Endpunkt zeigt
        assertTrue(popup.url().contains("/api/documents/preview/"), "Die Vorschau-URL ist fehlerhaft.");
        
        // Schließe den Preview-Tab
        popup.close();

        // 5. Dokument löschen
        // Registriere einen Listener, um das JavaScript-Bestätigungsfenster (window.confirm) automatisch zu bejahen
        page.onceDialog(Dialog::accept);
        
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete")).first().click();

        // Sicherstellen, dass das Element wieder verschwindet
        documentRow.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
        assertFalse(documentRow.isVisible(), "Das Dokument wurde nach dem Klick auf Delete nicht entfernt.");
    }

    @AfterEach
void takeScreenshotOnFailure() {
    // Pfad, wo Playwright das Bild kurzzeitig speichert
    String screenshotPath = "target/failure-screenshot.png";
    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
    
    // Das Bild an den Allure-Bericht anhängen
    attachScreenshot(screenshotPath);
}

@Attachment(value = "Page Screenshot", type = "image/png")
public byte[] attachScreenshot(String path) {
    try {
        return Files.readAllBytes(Paths.get(path));
    } catch (Exception e) {
        return new byte[0];
    }
}
}
