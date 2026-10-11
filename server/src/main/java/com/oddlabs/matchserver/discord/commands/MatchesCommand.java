package com.oddlabs.matchserver.discord.commands;

import com.oddlabs.matchserver.DBInterface;
import com.oddlabs.matchserver.WebsiteLinkHelper;
import com.oddlabs.matchserver.models.GameDataModel;
import com.oddlabs.matchserver.models.GamePlayerModel;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

public class MatchesCommand extends DiscordCommand {
    private static final int MAX_EMBED_FIELDS = 25;

    private String command_name = "matches";
    private String command_description = "Displays tribal trouble matches being played now";

    @Override
    public String getCommandName() {
        return command_name;
    }

    @Override
    public Mono<Void> executeCommand(ChatInputInteractionEvent event) {
        List<GameDataModel> games = DBInterface.getLiveGames();
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder().color(Color.BLUE).title(
                games.size() + (games.size() == 1 ? " match" : " matches") + " in progress");

        for (GameDataModel game : games.subList(0, Math.min(games.size(), MAX_EMBED_FIELDS))) {
            Map<Integer, StringJoiner> teams = new TreeMap<>();
            for (GamePlayerModel player : game.getPlayers()) {
                teams.computeIfAbsent(player.getPlayerTeam(), team -> new StringJoiner(", ")).add(
                        player.getPlayerName());
            }
            StringJoiner lineup = new StringJoiner(" vs ");
            teams.values().forEach(team -> lineup.add(team.toString()));

            StringBuilder fieldValue = new StringBuilder(lineup.toString());
            if (game.getTimeStart() != null) {
                fieldValue.append(String.format("\nStarted <t:%d:R>", game.getTimeStart().getTime() / 1000));
            }
            String replayUrl = WebsiteLinkHelper.getReplayUrl(game.getId());
            if (replayUrl != null) {
                fieldValue.append(String.format("\n[Watch](%s)", replayUrl));
            }
            String fieldName = game.getName() + ("Y".equals(game.getRated()) ? " (rated)" : "");
            builder.addField(fieldName, fieldValue.toString(), false);
        }

        return event.reply().withEmbeds(builder.build());
    }

    @Override
    public ApplicationCommandRequest getCommand() {
        return ApplicationCommandRequest.builder().name(command_name).description(command_description).build();
    }
}
