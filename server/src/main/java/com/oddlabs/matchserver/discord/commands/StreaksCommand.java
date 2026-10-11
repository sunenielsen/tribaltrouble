package com.oddlabs.matchserver.discord.commands;

import com.oddlabs.matchserver.DBInterface;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.StringJoiner;

public class StreaksCommand extends DiscordCommand {
    private static final int LEADER_COUNT = 10;

    private String command_name = "streaks";
    private String command_description = "Shows the longest current and best win streaks";

    @Override
    public String getCommandName() {
        return command_name;
    }

    @Override
    public Mono<Void> executeCommand(ChatInputInteractionEvent event) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).title("Win streaks").addField(
                "Current", formatLeaders(DBInterface.getStreakLeaders(true, LEADER_COUNT)), true).addField(
                        "Best", formatLeaders(DBInterface.getStreakLeaders(false, LEADER_COUNT)), true).build();
        return event.reply().withEmbeds(embed);
    }

    private static String formatLeaders(Map<String, Integer> leaders) {
        if (leaders.isEmpty()) {
            return "None yet";
        }
        StringJoiner lines = new StringJoiner("\n");
        int place = 1;
        for (Map.Entry<String, Integer> leader : leaders.entrySet()) {
            lines.add(String.format("%d. %s: %d", place++, leader.getKey(), leader.getValue()));
        }
        return lines.toString();
    }

    @Override
    public ApplicationCommandRequest getCommand() {
        return ApplicationCommandRequest.builder().name(command_name).description(command_description).build();
    }
}
