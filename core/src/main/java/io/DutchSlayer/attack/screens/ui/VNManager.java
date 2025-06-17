package io.DutchSlayer.attack.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;// Pastikan import Constant
import io.DutchSlayer.utils.Constant;

public class VNManager implements Disposable {

    private Array<VNScene> scenes; // Daftar adegan visual novel
    private int currentSceneIndex; // Indeks adegan yang sedang aktif

    private int currentTextIndex; // Indeks dialog dalam adegan saat ini
    private String currentDialogue; // Dialog lengkap yang sedang ditampilkan
    private StringBuilder displayedText; // Teks yang saat ini terlihat (untuk typewriter effect)

    private float textDisplayTimer; // Timer untuk typewriter effect
    private final float CHAR_PER_SECOND = 40.0f; // Kecepatan typewriter (karakter per detik)

    private boolean isDialogueComplete; // True jika seluruh dialog sudah ditampilkan
    private boolean isActive; // True jika VN sedang berjalan

    private Texture currentBackground; // Gambar latar belakang adegan saat ini
    private BitmapFont font; // Font untuk dialog
    private GlyphLayout layout; // Untuk mengukur teks
    private boolean active = false;
    public VNManager(BitmapFont font) {
        this.font = font;
        this.scenes = new Array<>();
        this.displayedText = new StringBuilder();
        this.layout = new GlyphLayout(); // Initialize GlyphLayout
        reset();
    }

    public void addScene(VNScene scene) {
        scenes.add(scene);
    }

    public void start() {
        if (scenes.size == 0) {
            Gdx.app.log("VNManager", "No scenes added to VNManager.");
            return;
        }
        active = true;
        isActive = true;
        currentSceneIndex = 0;
        loadCurrentScene();
    }

    public void update(float delta) {
        if (!isActive) {
            return;
        }

        // Update typewriter effect
        if (!isDialogueComplete) {
            textDisplayTimer += delta;
            int charsToDisplay = (int) (textDisplayTimer * CHAR_PER_SECOND);
            if (charsToDisplay > currentDialogue.length()) {
                charsToDisplay = currentDialogue.length();
                isDialogueComplete = true;
            }
            displayedText.setLength(0); // Clear previous text
            displayedText.append(currentDialogue.substring(0, charsToDisplay));
        }

        // Handle input for skipping/advancing
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!isDialogueComplete) {
                // Skip typewriter effect, show full text
                displayedText.setLength(0);
                displayedText.append(currentDialogue);
                isDialogueComplete = true;
            } else {
                // Advance to next dialogue or scene
                nextDialogue();
            }
        }
    }

    public int getSceneCount() {
        return scenes.size;
    }

    public void clearScenes() {
        // Menghapus semua elemen dari array 'scenes'
        scenes.clear();
        // Reset juga state internal manager
        active = false;
        currentSceneIndex = 0;
        // Jika Anda punya state lain seperti currentDialogueIndex, reset juga di sini
    }

    public void render(SpriteBatch batch) {
        if (!isActive) {
            return;
        }

        // PASTIKAN PROJECTION MATRIX SUDAH DISET UNTUK UI
        // Ini penting karena GameScreen mungkin sudah mengatur proyeksi untuk kamera game
        // VNManager perlu mengatur proyeksi untuk kamera UI (yang biasanya adalah kamera viewport standar)
        // Jika Anda menggunakan kamera lain untuk VN, pastikan itu yang digunakan di sini.
        // Asumsi di sini adalah Anda ingin VN mengisi layar penuh.
        // Jika batch.setProjectionMatrix sudah dilakukan di GameScreen untuk VNManager,
        // baris ini mungkin redundan, tapi tidak ada salahnya untuk memastikan.

        // batch.setProjectionMatrix(batch.getProjectionMatrix()); // TIDAK PERLU, SUDAH DIATUR DI GAMESCREEN

        // Render background image
        if (currentBackground != null) {
            batch.draw(currentBackground, 0, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
            Gdx.app.log("VNManager", "Drawing background texture.");
        } else {
            Gdx.app.log("VNManager", "currentBackground is NULL. Check texture loading!");
        }


        // Render dialogue box (simple rectangle, you can use a texture)
        // For simplicity, we'll draw text directly, but a proper box is better.

        // Render text with padding and word wrap
        float textX = 50;
        float textY = 200; // Position dialogue box from bottom
        float maxWidth = Constant.SCREEN_WIDTH - 100; // 50 padding on each side

        font.setColor(1, 1, 1, 1); // White color for text
        font.getData().setScale(1.2f); // Adjust font size as needed

        layout.setText(font, displayedText, font.getColor(), maxWidth, com.badlogic.gdx.utils.Align.left, true);
        // Pastikan textY yang digunakan di sini sesuai dengan offset yang Anda inginkan
        font.draw(batch, layout, textX, textY + layout.height); // draw from top-left, so add layout.height

        // Optional: Draw a prompt for next (e.g., small arrow)
        if (isDialogueComplete) {
            String prompt = "Press SPACE";
            layout.setText(font, prompt);
            font.draw(batch, prompt, Constant.SCREEN_WIDTH - layout.width - 50, 50);
        }
    }

    private void loadCurrentScene() {
        VNScene scene = scenes.get(currentSceneIndex);
        currentBackground = scene.getBackgroundTexture();
        currentTextIndex = 0;
        loadCurrentDialogue();
    }

    private void loadCurrentDialogue() {
        VNScene scene = scenes.get(currentSceneIndex);
        currentDialogue = scene.getDialogues().get(currentTextIndex);
        displayedText.setLength(0);
        textDisplayTimer = 0f;
        isDialogueComplete = false;
    }

    private void nextDialogue() {
        currentTextIndex++;
        VNScene scene = scenes.get(currentSceneIndex);
        if (currentTextIndex < scene.getDialogues().size) {
            loadCurrentDialogue();
        } else {
            // End of current scene's dialogues, move to next scene or end VN
            currentSceneIndex++;
            if (currentSceneIndex < scenes.size) {
                loadCurrentScene();
            } else {
                end(); // All scenes finished
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void end() {
        isActive = false;
        reset();
        Gdx.app.log("VNManager", "Visual Novel finished.");
    }

    private void reset() {
        currentSceneIndex = 0;
        currentTextIndex = 0;
        currentDialogue = "";
        displayedText.setLength(0);
        textDisplayTimer = 0f;
        isDialogueComplete = false;
        isActive = false;
        currentBackground = null;
    }

    @Override
    public void dispose() {
        // Textures for backgrounds should be disposed by the caller (GameScreen)
        // if they are shared resources. If they are loaded exclusively here, dispose them.
        // For now, assuming GameScreen loads and disposes them.
    }
}
