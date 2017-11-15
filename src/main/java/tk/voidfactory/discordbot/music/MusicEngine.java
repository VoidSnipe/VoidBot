package tk.voidfactory.discordbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.HashMap;
import java.util.Map;

public class MusicEngine extends tk.voidfactory.discordbot.CommandEngine {

    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    MusicEngine(String prefix) {
        super(prefix);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    @Override
    public void execute(String command, Member member, VoiceChannel voiceChannel, TextChannel textChannel, String[] args) {
        switch (command) {
            case "play":
            case "pause":
            case "add": case "+":
            case "shuffle":
            case "clear":
            case "skip":
            case "mode":
        }
    }

    private void play(AudioPlayer player) {
        player.setPaused(false);
    }
    private void pause(AudioPlayer player) {
        player.setPaused(true);
    }

}
