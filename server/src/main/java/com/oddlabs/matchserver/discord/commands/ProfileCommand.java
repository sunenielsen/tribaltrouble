package com.oddlabs.matchserver.discord.commands;

import com.oddlabs.matchmaking.RankingEntry;
import com.oddlabs.matchserver.DBInterface;
import com.oddlabs.matchserver.MatchmakingServer;
import com.oddlabs.matchserver.WebsiteLinkHelper;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;

import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class ProfileCommand extends DiscordCommand {
    /**
     * Indexed like RacesResources.RACE_NATIVES and RACE_VIKINGS in the client.
     */
    private static final String[] RACE_NAMES = {"Natives", "Vikings"};

    private String command_name = "profile";
    private String command_description = "Shows the rating, record, streaks and favorite tribe of a user";
    private String command_option_lookup_name = "tt_user";

    @Override
    public String getCommandName() {
        return command_name;
    }

    @Override
    public Mono<Void> executeCommand(ChatInputInteractionEvent event) {
        String lookup = getStringOption(event, command_option_lookup_name);
        RankingEntry[] ranking = lookup == null ? new RankingEntry[0] : DBInterface.getRankings(lookup, 0);
        if (ranking.length == 0) {
            return event.reply("Could not find profile: " + lookup);
        }
        RankingEntry entry = ranking[0];
        String nick = entry.getName().trim();

        int[] streaks;
        try {
            streaks = DBInterface.getStreaks(nick);
        } catch (SQLException e) {
            MatchmakingServer.getLogger().throwing(ProfileCommand.class.getName(), "executeCommand", e);
            return event.reply("Could not read the profile of " + nick).withEphemeral(true);
        }

        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder().color(Color.BLUE).title(nick).url(
                WebsiteLinkHelper.getPlayerHighscoreUrl(nick));

        builder.addField("Rating", String.format("%d (#%d)", entry.getRating(), entry.getRanking()), true);
        boolean online = Arrays.stream(DBInterface.getOnlineProfiles()).anyMatch(nick::equalsIgnoreCase);
        builder.addField("Status", online ? "Online" : "Offline", true);

        String record = String.format("%d W / %d L", entry.getWins(), entry.getLosses());
        int decidedGames = entry.getWins() + entry.getLosses();
        if (decidedGames > 0) {
            record += String.format(" (%.1f%%)", 100f * entry.getWins() / decidedGames);
        }
        builder.addField("Record", record, true);
        builder.addField("Win streak", String.format("%d (best %d)", streaks[0], streaks[1]), true);

        Map<Integer, Integer> raceGames = DBInterface.getRaceGameCounts(nick);
        int totalGames = raceGames.values().stream().mapToInt(Integer::intValue).sum();
        builder.addField("Games played", Integer.toString(totalGames), true);
        raceGames.entrySet().stream().findFirst().ifPresent(
                favorite -> builder.addField(
                        "Favorite tribe",
                        String.format(
                                "%s (%d%%)",
                                getRaceName(favorite.getKey()),
                                Math.round(100f * favorite.getValue() / totalGames)),
                        true));

        long discordUserId = DBInterface.getDiscordUserIdForProfile(nick);
        if (discordUserId != -1) {
            builder.addField("Discord", "<@" + discordUserId + ">", true);
        }

        return event.reply().withEmbeds(builder.build());
    }

    private static String getRaceName(int race) {
        return race >= 0 && race < RACE_NAMES.length ? RACE_NAMES[race] : "Unknown";
    }

    @Override
    public ApplicationCommandRequest getCommand() {
        return ApplicationCommandRequest.builder().name(command_name).description(command_description).addOption(
                ApplicationCommandOptionData.builder().name(command_option_lookup_name).description(
                        "The tribal trouble profile name to lookup").type(
                                ApplicationCommandOption.Type.STRING.getValue()).required(true).build()).build();
    }
}
