package com.oddlabs.matchserver.discord;

import com.oddlabs.matchserver.discord.commands.BanCommand;
import com.oddlabs.matchserver.discord.commands.BannedWordsCommand;
import com.oddlabs.matchserver.discord.commands.DiscordCommand;
import com.oddlabs.matchserver.discord.commands.KickCommand;
import com.oddlabs.matchserver.discord.commands.LeaderboardsCommand;
import com.oddlabs.matchserver.discord.commands.MatchesCommand;
import com.oddlabs.matchserver.discord.commands.MatchupCommand;
import com.oddlabs.matchserver.discord.commands.OnlineCommand;
import com.oddlabs.matchserver.discord.commands.ProfileCommand;
import com.oddlabs.matchserver.discord.commands.RankCommand;
import com.oddlabs.matchserver.discord.commands.RegisterProfileToDiscordUserCommand;
import com.oddlabs.matchserver.discord.commands.StreaksCommand;
import com.oddlabs.matchserver.discord.commands.UnbanCommand;
import com.oddlabs.matchserver.discord.commands.WhoIsCommand;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DiscordBotService {

    Snowflake bot_id;
    private GatewayDiscordClient gateway;
    private static DiscordBotService instance;
    private boolean isInitialized = false;
    private long serverId = -1;
    private DiscordChatroomCoordinator chatroomCoordinator;
    private ReactionRoleService reactionRoleService;
    private final List<Disposable> disposables = new ArrayList<>();

    ArrayList<DiscordCommand> commands = new ArrayList<>();

    public static DiscordBotService getInstance() {
        if (instance == null) {
            instance = new DiscordBotService();
        }
        return instance;
    }

    public DiscordBotService() {
    }

    public void initialize(String token, long serverId) {
        if (isInitialized) return;
        this.serverId = serverId;
        DiscordClient client = DiscordClient.create(token);

        // Gateway v10 sends message text only to bots that ask for it; the chatroom relay needs it.
        var gateway_bootstrap = client.gateway().setEnabledIntents(IntentSet.nonPrivileged().or(IntentSet.of(
                Intent.MESSAGE_CONTENT)));

        Mono<Void> login = gateway_bootstrap.withGateway(
                (GatewayDiscordClient gateway) -> {
                    this.gateway = gateway;
                    // Extra discord things that can be done
                    bot_id = gateway.getSelfId();
                    setupEventHandlers(serverId);
                    commands.add(new LeaderboardsCommand());
                    commands.add(new MatchupCommand());
                    commands.add(new RegisterProfileToDiscordUserCommand());
                    commands.add(new WhoIsCommand());
                    commands.add(new OnlineCommand());
                    commands.add(new MatchesCommand());
                    commands.add(new RankCommand());
                    commands.add(new ProfileCommand());
                    commands.add(new StreaksCommand());
                    commands.add(new KickCommand());
                    commands.add(new BanCommand());
                    commands.add(new UnbanCommand());
                    commands.add(new BannedWordsCommand());
                    chatroomCoordinator = new DiscordChatroomCoordinator();
                    reactionRoleService = new ReactionRoleService(gateway, serverId);
                    registerCommands();
                    // deleteCommands();
                    return gateway.onDisconnect();
                });
        isInitialized = true;
        login.subscribe();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Disposes of all Discord subscriptions and cleans up resources. This should be called when
     * shutting down the bot service.
     */
    public void dispose() {
        // Dispose of all stored subscriptions
        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        disposables.clear();
    }

    /**
     * Gets the discord id associated with the bot user
     */
    Snowflake getBotId() {
        return bot_id;
    }

    /**
     * Gets the chatroom coordinator for managing discord to tribal trouble chatrooms. Returns
     * Optional.empty() if not initialized.
     */
    public Optional<DiscordChatroomCoordinator> getChatroomCoordinator() {
        if (!isInitialized) return Optional.empty();
        return Optional.ofNullable(chatroomCoordinator);
    }

    /**
     * Gets the channel used for game activity updates.
     *
     * @return
     */
    public Optional<TextChannel> getGameActivityChannel() {
        if (!isInitialized) return Optional.empty();
        return Optional.ofNullable(game_activity_channel);
    }

    private ArrayList<TextChannel> message_channels = new ArrayList<>();
    private TextChannel game_activity_channel;

    /**
     * Sets up event handlers for the Discord bot
     */
    private void setupEventHandlers(long serverId) {
        initTribalTroubleTextChannels(serverId);

        disposables.add(
                gateway.on(
                        ChatInputInteractionEvent.class,
                        event -> {
                            if (event.getCommandName().equals("ping")) {
                                return event.reply("Pong!");
                            }

                            return commands.stream().filter(
                                    cmd -> event.getCommandName().equals(cmd.getCommandName())).findFirst().map(
                                            cmd -> {
                                                try {
                                                    return cmd.executeCommand(event);
                                                } catch (Exception e) {
                                                    System.out.println(
                                                            "Error executing command: " + e.getMessage());
                                                    return event.reply(
                                                            "An error occurred" + " while executing" + " the command.").withEphemeral(
                                                                    true);
                                                }
                                            }).orElseGet(
                                                    () -> {
                                                        System.out.println(
                                                                "No matching command found for: " + event.getCommandName());
                                                        return event.reply("Unknown command").withEphemeral(true);
                                                    });
                        }).subscribe());
        gateway.on(ReadyEvent.class, this::handleReady).take(1).subscribe();
    }

    public GatewayDiscordClient getGateway() {
        return gateway;
    }

    /**
     * Gets the discord chatroom that is associated with the TT chat room based off the tribal
     * trouble chat room number
     */
    public TextChannel getDiscordChannelByTTRoomNumber(int roomNumber) {
        if (!isInitialized) return null;
        if (roomNumber < 1 || roomNumber > message_channels.size()) {
            return null;
        }
        return message_channels.get(roomNumber - 1);
    }

    /**
     * Initializes the message channels that can be used to relay messages to and from discord and
     * tribal trouble. Currently the discord channels must be name tt_chatroom_<number> where
     * <number> is the same as the tribal trouble chat room number
     */
    private void initTribalTroubleTextChannels(long serverId) {
        if (gateway != null) {
            message_channels.clear(); // Clear before repopulating
            gateway.getGuilds().take(1).filter(
                    guild -> {
                        return guild.getId().equals(Snowflake.of(serverId));
                    }).doOnNext(
                            guild -> System.out.printf(
                                    "Found matching guild: %s%n", guild.getName())).flatMap(
                                            guild -> guild.getChannels()).subscribe(
                                                    channel -> {
                                                        System.out.printf(
                                                                "  - %s (ID: %s, Type: %s)%n",
                                                                channel.getName(),
                                                                channel.getId().asString(),
                                                                channel.getType().name());

                                                        if (channel.getType() == Channel.Type.GUILD_TEXT) {
                                                            if (channel.getName().indexOf("tt_chatroom_") != -1) {
                                                                message_channels.add((TextChannel) channel);
                                                                System.out.println(
                                                                        "Added channel: " + channel.getName());
                                                            } else if (channel.getName().equals("game-activity")) {
                                                                game_activity_channel = (TextChannel) channel;
                                                                System.out.println(
                                                                        "Added channel: " + channel.getName());
                                                            }
                                                        }
                                                    });
        }
    }

    // Event handlers
    private Mono<Void> handleReady(ReadyEvent event) {
        final User self = event.getSelf();
        System.out.printf("Logged in as %s#%n", self.getUsername());
        return Mono.empty();
    }

    /**
     * Deletes all existing commands for the bot in the specified server.
     */
    private void deleteCommands() {
        long guildId = serverId; // Discord4J's server ID.

        // Get the commands from discord as a Map
        Map<String, ApplicationCommandData> discordCommands = gateway.getRestClient().getApplicationService().getGuildApplicationCommands(
                getBotId().asLong(), guildId).collectMap(ApplicationCommandData::name).block();

        for (ApplicationCommandData data : discordCommands.values()) {
            System.out.println("Deleting command: " + data.name());
            gateway.getRestClient().getApplicationService().deleteGuildApplicationCommand(getBotId().asLong(), guildId,
                    data.id().asLong()).subscribe();
        }
    }

    /**
     * Creates or validates that each command in {@link
     * com.oddlabs.matchserver.discord.DiscordBotService#commands} is registered with Discord.
     */
    private void registerCommands() {
        gateway.getRestClient().getApplicationService().getGuildApplicationCommands(getBotId().asLong(),
                serverId).collectList().subscribe(
                        existingCommands -> {
                            for (DiscordCommand command : commands) {
                                existingCommands.stream().filter(cmd -> cmd.name().equals(
                                        command.getCommandName())).findFirst().ifPresentOrElse(
                                                cmd -> System.out.println(
                                                        "Command already registered: " + cmd.name()),
                                                () -> {
                                                    System.out.println(
                                                            "Registering new command: " + command.getCommandName());
                                                    gateway.getRestClient().getApplicationService().createGuildApplicationCommand(
                                                            gateway.getRestClient().getApplicationId().block(),
                                                            serverId,
                                                            command.getCommand()).subscribe();
                                                });
                            }
                        });
    }

    public void createPingCommand() {
        ApplicationCommandRequest pingCommand = ApplicationCommandRequest.builder().name("ping").description(
                "Pings").build();
        // Use your gateway field
        gateway.getRestClient().getApplicationService().createGuildApplicationCommand(
                gateway.getRestClient().getApplicationId().block(), serverId, pingCommand).subscribe();
    }
}
