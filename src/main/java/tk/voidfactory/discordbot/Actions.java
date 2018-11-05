package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.TimeUnit;

class Actions {
    static void muteChannel(VoiceChannel channel, boolean mute) {
        if (channel == null) return;
        PermissionOverride override = channel.getPermissionOverride(channel.getGuild().getPublicRole());
        if (override == null)
            override = channel.createPermissionOverride(channel.getGuild().getPublicRole()).complete();
        if (mute) {
            override.getManager().deny(Permission.VOICE_SPEAK).complete();

        } else {
            override.getManager().clear(Permission.VOICE_SPEAK).complete();
        }
        channel.getMembers().forEach(member -> autoMove(member, null));
    }

    static void moveAll(VoiceChannel channel) {
        if (channel == null) return;

        channel
                .getGuild()
                .getVoiceChannels()
                .forEach(chan -> chan
                        .getMembers()
                        .forEach(member -> autoMove(member, channel)));
    }

    private static void autoMove(Member member, VoiceChannel channel) {
        if (channel == null) channel = member.getVoiceState().getChannel();
        RestAction action = member.getGuild().getController().moveVoiceMember(member,channel);
        try {
            action.complete(false);
        } catch (RateLimitedException e) {
            action.completeAfter(e.getRetryAfter(), TimeUnit.MILLISECONDS);
        }

    }

    static void reply(Member member, TextChannel textChannel, String text) {
        textChannel.sendMessage(member.getAsMention() + ", " + text).queue(reply ->  reply.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
