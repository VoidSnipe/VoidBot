package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RestAction;


public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT)) {

            if (event.getMessage().getContent().length() == 0 || event.getMessage().getContent().charAt(0) != '!') return;
            String text = event.getMessage().getContent();
            Member member = event.getMember();
            VoiceChannel voiceChannel = member.getVoiceState().getChannel();
            VoiceQueue queue = VoiceQueue.get(voiceChannel);
            RestAction<Message> noQueue =
                    event
                            .getTextChannel()
                            .sendMessage(
                                    member.getAsMention()+", на канале "+member.getVoiceState().getChannel().getName()+
                                            " нет очереди");
            switch (text) {
                case "!join":
                    if (queue == null) noQueue.complete();
                    else queue.add(event.getMember()).announce(event.getTextChannel(),true);
                    break;
                case "!leave":
                    if (queue == null) noQueue.complete();
                    else queue.remove(event.getMember()).announce(event.getTextChannel(),true);
                    break;
                default:
                    if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) switch (text) {
                        case "!move":
                            if (voiceChannel == null) return;
                            Actions.moveAll(voiceChannel);
                            break;
                        case "!mute":
                            if (voiceChannel == null) return;
                            Actions.muteChannel(voiceChannel,true);
                            break;
                        case "!unmute":
                            if (voiceChannel == null) return;
                            Actions.muteChannel(voiceChannel,false);
                            break;
                        case "!next":
                            if (queue == null) return;
                            queue.next().announce(event.getTextChannel(),true);
                            break;
                        case "!queue":
                            if (voiceChannel == null) return;
                            VoiceQueue.create(voiceChannel, event.getTextChannel());
                            Actions.muteChannel(voiceChannel, true);
                            break;
                        case "!unqueue":
                            if (queue == null) return;
                            VoiceQueue.get(voiceChannel).delete();
                            break;
                        default: return;
                    } else {
                        return;
                    }

            }
            event.getMessage().delete().complete();
        }
    }
}
