package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.LinkedList;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String text = event.getMessage().getContent();
        if (text.startsWith("?mute")) Actions.autoMove(event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0)),
                event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0)).getVoiceState().getChannel());
        if (text.startsWith(Settings.PREFIX)) {
            LinkedList<String> argList = new LinkedList<>(Arrays.asList(text.split("\\s+")));
            String command = argList.poll().substring(Settings.PREFIX.length());
            CommandEngine.execute(command,event.getMember(),event.getMember().getVoiceState().getChannel(),event.getTextChannel(),argList.toArray(new String[0]));
            event.getMessage().delete().queue();
        }
    }

    MessageListener() {

    }
}
