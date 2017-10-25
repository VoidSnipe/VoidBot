package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.GuildController;

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
        channel.getMembers().forEach(member -> updateMember(member));
    }

    public static void moveAll(VoiceChannel channel) {
        if (channel == null) return;
        GuildController controller = channel.getGuild().getController();

        channel
                .getGuild()
                .getVoiceChannels()
                .forEach(chan -> {
                    if (true) {
                        chan
                                .getMembers()
                                .forEach(member -> controller
                                        .moveVoiceMember(member, channel).complete());
                    }
                });
    }

    public static void updateMember(Member member) {
        member.getGuild().getController().moveVoiceMember(member,member.getVoiceState().getChannel()).complete();
    }

}
