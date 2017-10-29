package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.GuildController;

import java.awt.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceQueue {
    private static Map<Channel, VoiceQueue> list = new ConcurrentHashMap<>();

    public static VoiceQueue get(VoiceChannel channel) {
        return channel==null?null:list.get(channel);
    }

    public static void create(VoiceChannel channel, TextChannel announceChannel) {
        VoiceQueue old = list.get(channel);
        if (old != null) old.delete();
        list.put(channel, new VoiceQueue(channel, announceChannel));
    }

    private Queue<Member> queue;
    private VoiceChannel channel;
    private TextChannel announceChannel;
    private Message announcement;
    private Role enqueuedRole;
    private Member enqueued;

    public VoiceQueue(VoiceChannel channel, TextChannel announceChannel) {
        this.channel = channel;
        this.announceChannel = announceChannel;
        queue = new LinkedList<>();
        announce(false);
        createRole(channel);
        enqueued = null;
    }

    private void createRole(VoiceChannel channel) {
        Guild guild = channel.getGuild();

        guild.getRolesByName("Enqueued @ " + channel.getName(), true).forEach(role -> role.delete().complete());
        enqueuedRole = guild.getPublicRole().createCopy().complete();
        enqueuedRole.getManager().setName("Enqueued @ " + channel.getName()).complete();
        channel.createPermissionOverride(enqueuedRole).complete().getManagerUpdatable().grant(Permission.VOICE_SPEAK).update().complete();
    }

    public void announce(boolean remove) {
        if (remove) try {
            announcement.delete().complete();
        } catch (Exception e) {
            announcement = null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n**Очередь для " + channel.getName() + "**\n");
        if (enqueued != null) {
            sb.append(":loud_sound: ").append("*").append(enqueued.getEffectiveName()).append("*\n");
        }
        queue.forEach(member -> sb.append(":small_blue_diamond: ").append(member.getEffectiveName()).append("\n"));
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.ORANGE);
        eb.setDescription(sb.toString());
        announcement = announceChannel.sendMessage(eb.build()).complete();

    }

    public VoiceQueue add(Member member, TextChannel replyChannel) {
        remove(member, null);
        queue.add(member);
        Actions.reply(member,replyChannel,"вы добавленны в очередь канала " + channel.getName());
        if (enqueued == null) next();
        announce(true);
        return this;
    }

    public VoiceQueue next() {
        GuildController controller = channel.getGuild().getController();
        if (enqueued != null) {
            controller.removeRolesFromMember(enqueued, enqueuedRole).complete();
            controller.moveVoiceMember(enqueued, channel).complete();
        }
        enqueued = queue.poll();
        if (enqueued != null) {
            controller.addRolesToMember(enqueued, enqueuedRole).complete();
            controller.moveVoiceMember(enqueued, channel).complete();
        }
        return this;
    }

    public VoiceQueue remove(Member member, TextChannel replyChannel) {
        queue.remove(member);
        if (enqueued == member) next();
        if (replyChannel != null) Actions.reply(member, replyChannel,"вы удаленны из очереди канала " + channel.getName());
        announce(true);
        return this;
    }

    public void delete() {
        announcement.delete().complete();
        enqueuedRole.delete().complete();
        list.remove(channel);
    }
}
