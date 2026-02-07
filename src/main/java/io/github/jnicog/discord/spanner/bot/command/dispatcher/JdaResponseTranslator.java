package io.github.jnicog.discord.spanner.bot.command.dispatcher;

import io.github.jnicog.discord.spanner.bot.command.InteractionResponse;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardPage;
import io.github.jnicog.discord.spanner.bot.leaderboard.LeaderboardSessionManager;
import io.github.jnicog.discord.spanner.bot.notification.MessageFormatterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Translates domain-agnostic InteractionResponse objects into JDA API calls.
 * This is the single point where JDA coupling exists for responses.
 */
@Component
public class JdaResponseTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdaResponseTranslator.class);
    private static final Color LEADERBOARD_COLOUR = new Color(0x549159);

    // Reaction emojis for leaderboard pagination
    public static final String FIRST_PAGE = "⏮";
    public static final String PREVIOUS_PAGE = "◀";
    public static final String NEXT_PAGE = "▶";
    public static final String LAST_PAGE = "⏭";

    private final MessageFormatterService messageFormatter;
    private final LeaderboardSessionManager sessionManager;

    public JdaResponseTranslator(MessageFormatterService messageFormatter,
                                  LeaderboardSessionManager sessionManager) {
        this.messageFormatter = messageFormatter;
        this.sessionManager = sessionManager;
    }

    /**
     * Handles responses for ButtonInteractionEvent specifically.
     * Supports button-specific response types like EditButtonMessageAndClearComponents.
     */
    public void send(ButtonInteractionEvent event, InteractionResponse response) {
        switch (response) {
            case InteractionResponse.EditButtonMessage(String content) ->
                event.editMessage(content)
                    .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to edit button message: {}", error.getMessage())
                    );

            case InteractionResponse.EditButtonMessageAndClearComponents(String content) ->
                event.editMessage(content)
                    .setComponents()  // Clear all components (buttons, etc.)
                    .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to edit button message and clear components: {}", error.getMessage())
                    );

            // Delegate other response types to the generic handler
            default -> send((IReplyCallback) event, response);
        }
    }

    public void send(IReplyCallback interaction, InteractionResponse response) {
        switch (response) {
            case InteractionResponse.EphemeralReply(String content) ->
                interaction.reply(content).setEphemeral(true).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to send ephemeral reply: {}", error.getMessage())
                );

            case InteractionResponse.PublicReply(String content) ->
                interaction.reply(content).setEphemeral(false).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to send public reply: {}", error.getMessage())
                );

            case InteractionResponse.EditMessage(long messageId, String content) ->
                interaction.getHook().editMessageById(messageId, content).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to edit message {}: {}", messageId, error.getMessage())
                );

            case InteractionResponse.UpdateOriginalMessage(String content) ->
                interaction.getHook().editOriginal(content).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to update original message: {}", error.getMessage())
                );

            case InteractionResponse.UpdateOriginalMessageAndClearComponents(String content) ->
                interaction.getHook().editOriginal(content)
                    .setComponents()  // Clear all components (buttons, etc.)
                    .queue(
                        __ -> {},
                        error -> LOGGER.error("Failed to update original message and clear components: {}", error.getMessage())
                    );

            case InteractionResponse.EditButtonMessage(String content) ->
                // This should only be used with ButtonInteractionEvent
                LOGGER.error("EditButtonMessage used with non-button interaction");

            case InteractionResponse.EditButtonMessageAndClearComponents(String content) ->
                // This should only be used with ButtonInteractionEvent
                LOGGER.error("EditButtonMessageAndClearComponents used with non-button interaction");

            case InteractionResponse.LeaderboardEmbed(LeaderboardPage page, long ownerId) ->
                sendLeaderboardEmbed(interaction, page, ownerId);

            case InteractionResponse.DeferReply(boolean ephemeral) ->
                interaction.deferReply(ephemeral).queue(
                    __ -> {},
                    error -> LOGGER.error("Failed to defer reply: {}", error.getMessage())
                );

            case InteractionResponse.NoReply() -> {
                // Intentionally do nothing
            }
        }
    }

    private void sendLeaderboardEmbed(IReplyCallback interaction, LeaderboardPage page, long ownerId) {
        MessageEmbed embed = buildLeaderboardEmbed(page);

        interaction.replyEmbeds(embed).queue(
            hook -> hook.retrieveOriginal().queue(
                message -> {
                    long messageId = message.getIdLong();
                    long channelId = message.getChannelIdLong();

                    // Register the session for pagination tracking
                    sessionManager.createSession(messageId, channelId, ownerId);

                    // Add reaction emojis for pagination
                    message.addReaction(Emoji.fromUnicode(FIRST_PAGE)).queue();
                    message.addReaction(Emoji.fromUnicode(PREVIOUS_PAGE)).queue();
                    message.addReaction(Emoji.fromUnicode(NEXT_PAGE)).queue();
                    message.addReaction(Emoji.fromUnicode(LAST_PAGE)).queue();

                    LOGGER.debug("Created leaderboard message {} with pagination reactions", messageId);
                },
                error -> LOGGER.error("Failed to retrieve leaderboard message: {}", error.getMessage())
            ),
            error -> LOGGER.error("Failed to send leaderboard embed: {}", error.getMessage())
        );
    }

    /**
     * Builds a MessageEmbed for the leaderboard.
     */
    public MessageEmbed buildLeaderboardEmbed(LeaderboardPage page) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Spanner Leaderboard")
                .setColor(LEADERBOARD_COLOUR)
                .setFooter(messageFormatter.formatLeaderboardFooter(page.currentPage(), page.totalPages()))
                .setTimestamp(OffsetDateTime.now());

        if (page.entries().isEmpty()) {
            builder.setDescription(messageFormatter.formatEmptyLeaderboard());
        } else {
            String description = page.entries().stream()
                    .map(entry -> messageFormatter.formatLeaderboardEntry(
                            entry.rank(), entry.userId(), entry.spannerCount()))
                    .collect(Collectors.joining("\n"));
            builder.setDescription(description);
        }

        return builder.build();
    }
}
