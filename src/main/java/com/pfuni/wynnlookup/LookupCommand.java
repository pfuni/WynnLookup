package com.pfuni.wynnlookup;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.pfuni.wynnlookup.gui.LookupGui;

public class LookupCommand {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("lookup")
                .then(ClientCommandManager.argument("player", StringArgumentType.word())
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");

                            // Uruchamiamy pobieranie danych w tle
                            new Thread(() -> fetchPlayerData(context.getSource(), playerName)).start();
                            return 1;
                        })
                )
        );
    }

    private static void fetchPlayerData(FabricClientCommandSource source, String playerName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.wynncraft.com/v3/player/" + playerName + "/characters"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                source.getPlayer().sendMessage(
                        net.minecraft.text.Text.literal("❌ Błąd: API zwróciło kod " + response.statusCode())
                                .styled(s -> s.withColor(0xFF5555)),
                        false
                );
                return;
            }

            Map<String, Map<String, Object>> data = gson.fromJson(response.body(), Map.class);

            // Przygotowujemy dane do GUI
            List<String> characterStrings = new ArrayList<>();
            if (data != null && !data.isEmpty()) {
                data.entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(
                                ((Number)e2.getValue().get("level")).intValue(),
                                ((Number)e1.getValue().get("level")).intValue()
                        ))
                        .forEach(entry -> {
                            Map<String,Object> c = entry.getValue();
                            String type = (String)c.get("type");
                            String reskin = (String)c.get("reskin");
                            int level = ((Number)c.get("level")).intValue();
                            String icon = switch (type.toUpperCase()) {
                                case "ARCHER" -> "🏹";
                                case "WARRIOR" -> "⚔️";
                                case "ASSASSIN" -> "🗡️";
                                case "MAGE" -> "🪄";
                                case "SHAMAN" -> "🔮";
                                default -> "❓";
                            };
                            characterStrings.add(icon + " " + type + " Lv." + level + (reskin != null ? " (" + reskin + ")" : ""));
                        });
            }

            // Otwieramy GUI na wątku klienta
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> client.setScreen(
                    new LookupGui(playerName, data != null ? data.size() : 0, characterStrings)
            ));

        } catch (Exception e) {
            source.getPlayer().sendMessage(
                    net.minecraft.text.Text.literal("❌ Wystąpił błąd: " + e.getMessage())
                            .styled(s -> s.withColor(0xFF5555)),
                    false
            );
            e.printStackTrace();
        }
    }
}
