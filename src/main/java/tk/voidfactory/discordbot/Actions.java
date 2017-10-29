package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.TimeUnit;

public class Actions {
    public static void muteChannel(VoiceChannel channel, boolean mute) {
        if (channel == null) return;
        PermissionOverride override = channel.getPermissionOverride(channel.getGuild().getPublicRole());
        if (override == null)
            override = channel.createPermissionOverride(channel.getGuild().getPublicRole()).complete();
        if (mute) {
            override.getManagerUpdatable().deny(Permission.VOICE_SPEAK).update().complete();

        } else {
            override.getManagerUpdatable().clear(Permission.VOICE_SPEAK).update().complete();
        }
        channel.getMembers().forEach(member -> autoMove(member, null));
    }

    public static void moveAll(VoiceChannel channel) {
        if (channel == null) return;

        channel
                .getGuild()
                .getVoiceChannels()
                .forEach(chan -> chan
                        .getMembers()
                        .forEach(member -> autoMove(member, channel)));
    }

    public static void autoMove(Member member ,VoiceChannel channel) {
        if (channel == null) channel = member.getVoiceState().getChannel();
        RestAction action = member.getGuild().getController().moveVoiceMember(member,channel);
        try {
            action.complete(false);
        } catch (RateLimitedException e) {
            action.completeAfter(e.getRetryAfter(), TimeUnit.MILLISECONDS);
        }

    }

    public static void reply(Member member, TextChannel textChannel, String text) {
        textChannel.sendMessage(member.getAsMention() + ", " + text).queue(reply ->  reply.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
