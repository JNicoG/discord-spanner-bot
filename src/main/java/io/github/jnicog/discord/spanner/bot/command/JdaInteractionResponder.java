package io.github.jnicog.discord.spanner.bot.command;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdaInteractionResponder implements InteractionResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdaInteractionResponder.class);

    private final IReplyCallback interaction;

    public JdaInteractionResponder(IReplyCallback interaction) {
        this.interaction = interaction;
    }

    @Override
    public void replyEphemeral(String content) {
        interaction.reply(content).setEphemeral(true).queue(
                success -> {
                    // No follow-up action needed on success
                }, error -> {
                    LOGGER.error("Failed to send ephemeral reply to interaction {}: {}", interaction.getId(), error.getMessage());
                }
        );
    }

    @Override
    public void replyPublic(String content) {
        interaction.reply(content).setEphemeral(false).queue(
                success -> {
                    // No follow-up action needed on success
                }, error -> {
                    LOGGER.error("Failed to send public reply to interaction {}: {}", interaction.getId(), error.getMessage());
                }
        );
    }

//    @Override
//    public void editReply(String content) {
//        interaction.getHook().editOriginal(content).queue();
//    }

}
