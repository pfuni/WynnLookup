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
    private boolean isDraggingScrollbar = false;
    private int dragStartY;
    private int dragStartScroll;

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

        // Jednolite tło
        context.fill(startX, startY, startX + guiWidth, startY + guiHeight, 0xFF1F1F1F);

        // Obszar listy postaci
        int listStartY = startY + 50;
        int listEndY = startY + guiHeight - 60;
        int visibleHeight = listEndY - listStartY;
        int lineHeight = 12;
        int totalHeight = characters.size() * lineHeight;

        // Maksymalny scroll
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        // Rysowanie przyciemnionego tła pod scrollbarem
        if (maxScroll > 0) {
            int scrollbarWidth = 6;
            int scrollbarX = startX + guiWidth - scrollbarWidth - 2;

            // Ciemniejsze tło pod scrollbarem
            context.fill(scrollbarX - 2, listStartY, scrollbarX + scrollbarWidth + 2, listEndY, 0x66000000);

            int scrollbarHeight = Math.max(32, visibleHeight * visibleHeight / totalHeight);
            int scrollbarY = listStartY + (visibleHeight - scrollbarHeight) * scrollOffset / maxScroll;

            // Tło scrollbara (półprzezroczyste)
            context.fill(scrollbarX, listStartY, scrollbarX + scrollbarWidth, listEndY, 0x33FFFFFF);

            // Scrollbar
            int scrollColor = isMouseOverScrollbar(mouseX, mouseY, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight) ? 0x99FFFFFF : 0x66FFFFFF;
            context.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, scrollColor);
        }

        // Draw all text last to ensure it's on top
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

        String warning = "MOŻE NIE POKAZAĆ WSZYSTKICH";
        context.drawTextWithShadow(this.textRenderer,
                warning,
                this.width / 2 - this.textRenderer.getWidth(warning) / 2,
                startY + 33,
                0xFF0000);

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

        super.render(context, mouseX, mouseY, delta);
    }

    private boolean isMouseOverScrollbar(int mouseX, int mouseY, int scrollbarX, int scrollbarY, int scrollbarWidth, int scrollbarHeight) {
        return mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth &&
                mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int guiWidth = 250;
            int guiHeight = 220;
            int startX = (this.width - guiWidth) / 2;
            int startY = (this.height - guiHeight) / 2;
            int listStartY = startY + 50;
            int listEndY = startY + guiHeight - 60;
            int visibleHeight = listEndY - listStartY;
            int scrollbarWidth = 6;
            int scrollbarX = startX + guiWidth - scrollbarWidth - 2;
            int totalHeight = characters.size() * 12;
            int scrollbarHeight = Math.max(32, visibleHeight * visibleHeight / totalHeight);
            int scrollbarY = listStartY + (visibleHeight - scrollbarHeight) * scrollOffset / Math.max(1, totalHeight - visibleHeight);

            if (isMouseOverScrollbar((int)mouseX, (int)mouseY, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight)) {
                isDraggingScrollbar = true;
                dragStartY = (int)mouseY;
                dragStartScroll = scrollOffset;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDraggingScrollbar) {
            int guiHeight = 220;
            int startY = (this.height - guiHeight) / 2;
            int listStartY = startY + 50;
            int listEndY = startY + guiHeight - 60;
            int visibleHeight = listEndY - listStartY;
            int totalHeight = characters.size() * 12;
            int maxScroll = Math.max(0, totalHeight - visibleHeight);

            double deltaMove = mouseY - dragStartY;
            double scrollFactor = (double)maxScroll / (visibleHeight - Math.max(32, visibleHeight * visibleHeight / totalHeight));
            scrollOffset = (int)Math.max(0, Math.min(maxScroll, dragStartScroll + deltaMove * scrollFactor));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int guiHeight = 220;
        int startY = (this.height - guiHeight) / 2;
        int listStartY = startY + 50;
        int listEndY = startY + guiHeight - 60;
        int visibleHeight = listEndY - listStartY;
        int totalHeight = characters.size() * 12;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);

        scrollOffset -= amount * 12;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }
}
