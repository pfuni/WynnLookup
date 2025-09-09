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
    private int scrollOffset = 0; // do scrollowania listy

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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int guiWidth = 250;
        int guiHeight = 220;
        int startX = (this.width - guiWidth) / 2;
        int startY = (this.height - guiHeight) / 2;

        // Tło panelu (ciemny gradient + ramka)
        fillGradient(context, startX, startY, startX + guiWidth, startY + guiHeight, 0xFF2E2E2E, 0xFF1F1F1F);
        context.fill(startX, startY, startX + guiWidth, startY + guiHeight, 0xFF000000);

        // Nagłówek - gracz
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("⏳ Szukam gracza: " + playerName),
                this.width / 2,
                startY + 10,
                0xFFFFFF);

        // Liczba postaci
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("📊 Liczba postaci: " + numberOfCharacters),
                this.width / 2,
                startY + 23,
                0x55FF55);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("MOŻE NIE POKAZAĆ WSZYSTKICH"),
                this.width / 2,
                startY + 33,
                0xff0000);

        // Lista postaci z przewijaniem
        int y = startY + 38 - scrollOffset;
        int maxY = startY + guiHeight - 60; // miejsce na przyciski
        for (String c : characters) {
            if (y >= startY + 50 && y <= maxY) { // rysuj tylko w panelu
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(c), this.width / 2, y, 0xFFD700);
            }
            y += 15;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // Obsługa scrolla myszy (usunieto @Override, kompatybilne z 1.21.4)
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollOffset -= (int)(amount * 15);
        int maxOffset = Math.max(0, characters.size() * 15 - 100);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
        return true;
    }

    // Gradient tła
    private void fillGradient(DrawContext context, int x1, int y1, int x2, int y2, int color1, int color2) {
        context.fillGradient(x1, y1, x2, y2, color1, color2);
    }
}
