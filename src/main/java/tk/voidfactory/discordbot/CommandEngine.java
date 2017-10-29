package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import static net.dv8tion.jda.core.utils.Helpers.getStackTrace;


public class CommandEngine {
    private static class PermissionDeniedException extends Exception {
    }

    public static void execute(String command, Member member, VoiceChannel voiceChannel, TextChannel textChannel, String[] args) {
        try {
            VoiceQueue queue;
            switch (command) {
                /* Mute/unmute channel */
                case "mute":
                    checkRun(() -> Actions.muteChannel(voiceChannel, true), voiceChannel, member, Permission.MANAGE_PERMISSIONS);
                    break;
                case "unmute":
                    checkRun(() -> Actions.muteChannel(voiceChannel, false), voiceChannel, member, Permission.MANAGE_PERMISSIONS);
                    break;
                /* Move everybody to the channel */
                case "move":
                    checkRun(() -> Actions.moveAll(voiceChannel), voiceChannel, member, Permission.VOICE_MOVE_OTHERS);
                    break;
                /* Queue managing */
                case "queue":
                    checkRun(() -> VoiceQueue.create(voiceChannel, textChannel), voiceChannel, member, Permission.ADMINISTRATOR);
                    break;
                case "unqueue":
                    checkRun(() -> VoiceQueue.get(voiceChannel).delete(), VoiceQueue.get(voiceChannel), member, Permission.ADMINISTRATOR);
                    break;
                case "next":
                    checkRun(() -> VoiceQueue.get(voiceChannel).next(), VoiceQueue.get(voiceChannel), member, Permission.ADMINISTRATOR);
                    break;
                /* Queue using */
                case "join":
                    checkRun(() -> VoiceQueue.get(voiceChannel).add(member, textChannel), VoiceQueue.get(voiceChannel), member);
                    break;
                case "leave":
                    checkRun(() -> VoiceQueue.get(voiceChannel).remove(member, textChannel), VoiceQueue.get(voiceChannel), member);
                    break;
                default:
                    Actions.reply(member, textChannel, "нет такой команды, воспользуйтесь !help для уточнения");
            }

        } catch (PermissionDeniedException | IllegalStateException e) {
            Actions.reply(member, textChannel, "сейчас вы не можете сделать это");
        } catch (Exception e) {
            Actions.reply(member, textChannel, getStackTrace(e));
        }
    }

    private static void checkRun(Runnable runnable, Object notnull, Member member, Permission... permission) throws PermissionDeniedException, IllegalStateException {
        if (notnull == null) throw new IllegalStateException();
        if (!member.hasPermission(permission)) throw new PermissionDeniedException();
        runnable.run();
    }

    public static void sendHelp(Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Справка");
        builder.setDescription("Доступные вам комманды:");
        builder.addField("!join", "Добавляет вас в очередь голосового чата (на итогах недели)", true);
        builder.addField("!leave", "Убирает вас из очереди голосового чата (на итогах недели)", true);
        if (member.hasPermission(Permission.MANAGE_PERMISSIONS))
        {
            builder.addField("!mute","Переводит голосовой канал в режим \"только администрация\"", true);
            builder.addField("!unmute","Выводит голосовой канал из режима \"только администрация\"", true);
        }
        if (member.hasPermission(Permission.ADMINISTRATOR))
        {
            builder.addField("!queue","Переводит голосовой канал в режим очереди", true);
            builder.addField("!unqueue","Выводит голосовой канал из режима очереди", true);
            builder.addField("!next", "Выкидывает текущего пользователя из очереди", true);
        }

        member.getUser().openPrivateChannel().queue(channel -> channel.sendMessage(builder.build()).queue());
    }
}
