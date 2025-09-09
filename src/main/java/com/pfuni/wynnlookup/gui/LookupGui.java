package com.pfuni.wynnlookup.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class LookupGui extends Screen {

    private final String playerName;
    private final int numberOfCharacters;
    private final List<String> characters;
    private int scrollOffset = 0;

    private long lastTime = System.currentTimeMillis();
    private boolean showArrows = true;

    public LookupGui(String playerName, int numberOfCharacters, List<String> characters) {
        super(Text.literal("Lookup: " + playerName));
        this.playerName = playerName;
        this.numberOfCharacters = numberOfCharacters;
        this.characters = characters;
    }

    @Override
    protected void init() {
        int guiWidth = 250;
        int guiHeight = 220;
        int startX = (this.width - guiWidth) / 2;
        int startY = (this.height - guiHeight) / 2;

        // Przycisk wysyłający wiadomość do gracza
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("💬 Wyślij wiadomość"),
                button -> {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.networkHandler.sendCommand(
                                "msg " + playerName + " hej"
                        );
                    }
                }
        ).dimensions(startX + 25, startY + guiHeight - 50, guiWidth - 50, 20).build());

        // Przycisk zamknięcia GUI
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("❌ Zamknij"),
                button -> MinecraftClient.getInstance().setScreen(null)
        ).dimensions(startX + 25, startY + guiHeight - 25, guiWidth - 50, 20).build());
    }

    private void drawScrollArrows(DrawContext context, int x, int y, int width, int visibleHeight, int totalHeight) {
        // Update blink state every 500ms
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 500) {
            showArrows = !showArrows;
            lastTime = currentTime;
        }

        if (!showArrows) return;

        // Only draw arrows if we have content to scroll
        if (totalHeight > visibleHeight) {
            String upArrow = "⬆";
            String downArrow = "⬇";

            // Up arrow when we're not at the top
            if (scrollOffset > 0) {
                context.drawTextWithShadow(this.textRenderer,
                        upArrow,
                        x + width - 15,
                        y + 2,
                        0xFFFFFF);
            }

            // Down arrow when we're not at the bottom
            if (scrollOffset < totalHeight - visibleHeight) {
                context.drawTextWithShadow(this.textRenderer,
                        downArrow,
                        x + width - 15,
                        y + visibleHeight - 10,
                        0xFFFFFF);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int guiWidth = 250;
        int guiHeight = 220;
        int startX = (this.width - guiWidth) / 2;
        int startY = (this.height - guiHeight) / 2;

        // Jednolite tło
        context.fill(startX, startY, startX + guiWidth, startY + guiHeight, 0xFF1F1F1F);

        // Draw all text
        // Header text
        String header = "⏳ Szukam gracza: " + playerName;
        context.drawTextWithShadow(this.textRenderer,
                header,
                this.width / 2 - this.textRenderer.getWidth(header) / 2,
                startY + 10,
                0xFFFFFF);

        String characterCount = "📊 Liczba postaci: " + numberOfCharacters;
        context.drawTextWithShadow(this.textRenderer,
                characterCount,
                this.width / 2 - this.textRenderer.getWidth(characterCount) / 2,
                startY + 23,
                0x55FF55);

        // Lista postaci
        int listStartY = startY + 50;
        int listEndY = startY + guiHeight - 60;
        int visibleHeight = listEndY - listStartY;
        int lineHeight = 12;
        int totalHeight = characters.size() * lineHeight;

        // Maksymalny scroll
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        // Draw character list text
        int y = listStartY - scrollOffset;
        for (String character : characters) {
            if (y + lineHeight >= listStartY && y <= listEndY) {
                context.drawTextWithShadow(this.textRenderer,
                        character,
                        this.width / 2 - this.textRenderer.getWidth(character) / 2,
                        y,
                        0xFFD700);
            }
            y += lineHeight;
        }

        // Draw scroll indicators if needed
        drawScrollArrows(context, startX, listStartY, guiWidth, visibleHeight, totalHeight);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int guiHeight = 220;
        int startY = (this.height - guiHeight) / 2;
        int listStartY = startY + 50;
        int listEndY = startY + guiHeight - 60;
        int visibleHeight = listEndY - listStartY;
        int totalHeight = characters.size() * 12;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);

        scrollOffset -= verticalAmount * 12;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }
}
