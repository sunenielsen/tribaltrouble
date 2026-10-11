package com.oddlabs.matchserver.discord;

import com.oddlabs.matchserver.ServerConfiguration;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.emoji.Emoji;

import reactor.core.publisher.Mono;

import java.util.Map;

public class ReactionRoleService {

    private final GatewayDiscordClient gateway;
    private final Map<String, String> emojiRoleMappings;
    private final long serverId;
    private final String messageId;

    public ReactionRoleService(GatewayDiscordClient gateway, long serverId) {
        this.gateway = gateway;
        this.serverId = serverId;
        this.emojiRoleMappings = ServerConfiguration.getInstance().getEmojiRoleMappings();
        this.messageId = ServerConfiguration.getInstance().get(ServerConfiguration.REACTION_ROLE_MESSAGE_ID);
        setupReactionListeners();
    }

    private void setupReactionListeners() {
        gateway.on(ReactionAddEvent.class, this::handleReactionAdd).subscribe();
        gateway.on(ReactionRemoveEvent.class, this::handleReactionRemove).subscribe();
    }

    private Mono<Void> handleReactionAdd(ReactionAddEvent event) {
        return processReaction(
                event.getUserId(),
                event.getEmoji(),
                event.getGuildId().orElse(null),
                event.getMessageId(),
                true);
    }

    private Mono<Void> handleReactionRemove(ReactionRemoveEvent event) {
        return processReaction(
                event.getUserId(),
                event.getEmoji(),
                event.getGuildId().orElse(null),
                event.getMessageId(),
                false);
    }

    private Mono<Void> processReaction(
            Snowflake userId,
            Emoji emoji,
            Snowflake guildId,
            Snowflake eventMessageId,
            boolean adding) {
        if (guildId == null || !guildId.equals(Snowflake.of(serverId))) {
            return Mono.empty();
        }

        // Check if this reaction is on the specific message we're watching
        if (messageId == null || !eventMessageId.asString().equals(messageId)) {
            return Mono.empty();
        }

        String emojiIdentifier = getEmojiIdentifier(emoji);
        String roleId = emojiRoleMappings.get(emojiIdentifier);

        if (roleId == null) {
            return Mono.empty();
        }

        return gateway.getGuildById(guildId).flatMap(
                guild -> guild.getMemberById(userId).flatMap(
                        member -> {
                            Snowflake roleSnowflake = Snowflake.of(roleId);
                            if (adding) {
                                return addRoleToMember(
                                        member, roleSnowflake, guild);
                            } else {
                                return removeRoleFromMember(
                                        member, roleSnowflake, guild);
                            }
                        })).onErrorResume(
                                error -> {
                                    System.err.println(
                                            "Error processing reaction role: " + error.getMessage());
                                    return Mono.empty();
                                });
    }

    private Mono<Void> addRoleToMember(Member member, Snowflake roleId, Guild guild) {
        return guild.getRoleById(roleId).flatMap(
                role -> {
                    if (member.getRoleIds().contains(roleId)) {
                        return Mono.empty();
                    }
                    return member.addRole(roleId).doOnSuccess(
                            v -> System.out.println(
                                    "Added role " + role.getName() + " to user " + member.getDisplayName())).doOnError(
                                            error -> System.err.println(
                                                    "Failed to add role: " + error.getMessage()));
                }).onErrorResume(
                        error -> {
                            System.err.println(
                                    "Role not found or error adding role: " + error.getMessage());
                            return Mono.empty();
                        });
    }

    private Mono<Void> removeRoleFromMember(Member member, Snowflake roleId, Guild guild) {
        return guild.getRoleById(roleId).flatMap(
                role -> {
                    if (!member.getRoleIds().contains(roleId)) {
                        return Mono.empty();
                    }
                    return member.removeRole(roleId).doOnSuccess(
                            v -> System.out.println(
                                    "Removed role " + role.getName() + " from user " + member.getDisplayName())).doOnError(
                                            error -> System.err.println(
                                                    "Failed to remove role: " + error.getMessage()));
                }).onErrorResume(
                        error -> {
                            System.err.println(
                                    "Role not found or error removing role: " + error.getMessage());
                            return Mono.empty();
                        });
    }

    private String getEmojiIdentifier(Emoji emoji) {
        return emoji.asCustomEmoji().map(customEmoji -> customEmoji.getId().asString()).orElse(
                emoji.asUnicodeEmoji().map(unicode -> unicode.getRaw()).orElse(""));
    }
}
