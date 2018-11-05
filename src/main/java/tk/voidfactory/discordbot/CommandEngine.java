package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

public  class CommandEngine extends ListenerAdapter {

    private final String prefix;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getGuild() == null) return;
        String text = event.getMessage().getContentRaw();
        if (text.startsWith(prefix)) {
            LinkedList<String> argList = new LinkedList<>(Arrays.asList(text.split("\\s+")));
            String command = Objects.requireNonNull(argList.poll()).substring(prefix.length());
            execute(command,event.getMember(),event.getMember().getVoiceState().getChannel(),event.getTextChannel(),argList.toArray(new String[0]));
            event.getMessage().delete().queue();
        }
    }

    protected void execute(String command, Member member, VoiceChannel voiceChannel, TextChannel textChannel, String[] args) {

    }

    public CommandEngine(String prefix) {
        this.prefix = prefix;
    }
}
