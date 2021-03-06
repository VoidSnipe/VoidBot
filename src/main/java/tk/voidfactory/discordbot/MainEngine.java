package tk.voidfactory.discordbot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.json.JSONException;
import tk.voidfactory.discordbot.data.PriceCheck;
import tk.voidfactory.discordbot.data.SyncChannelSet;
import tk.voidfactory.discordbot.data.WorldData;

import java.awt.*;
import java.security.AccessControlException;
import java.util.concurrent.FutureTask;

import static net.dv8tion.jda.core.utils.Helpers.getStackTrace;


public class MainEngine extends CommandEngine {
    MainEngine(String prefix) {
        super(prefix);
    }

    private class PermissionDeniedException extends Exception {
    }

    @Override
    protected void execute(String command, Member member, VoiceChannel voiceChannel, TextChannel textChannel, String[] args) {
        try {
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
                    checkRun(() -> VoiceQueue.get(voiceChannel).next().announce(true), VoiceQueue.get(voiceChannel), member, Permission.ADMINISTRATOR);
                    break;
                /* Queue using */
                case "join":
                    checkRun(() -> VoiceQueue.get(voiceChannel).add(member, textChannel), VoiceQueue.get(voiceChannel), member);
                    break;
                case "leave":
                    checkRun(() -> VoiceQueue.get(voiceChannel).remove(member, textChannel), VoiceQueue.get(voiceChannel), member);
                    break;
                case "price":
                case "цена":
                    if (!SyncChannelSet.get(textChannel))
                        Actions.reply(member, textChannel, "на этом канале нельзя смотреть цены");
                    else
                        new Thread(() -> {
                            Message reply = textChannel.sendMessage(member.getAsMention() + ", подождите, обрабатываю запрос...").complete();
                            try {
                                reply.editMessage(new PriceCheck(args).process().print()).queue();
                            } catch (JSONException | IndexOutOfBoundsException e) {
                                reply.delete().queue();
                                Actions.reply(member, textChannel, "не удалось получить данные по вашему запросу");
                            }
                        }).start();
                    break;
                case "baro":
                    if (!SyncChannelSet.get(textChannel))
                        Actions.reply(member, textChannel, "на этом канале нельзя смотреть время");
                    else
                        textChannel.sendMessage(WorldData.baro()).queue();
                    break;
                case "cycle":
                    if (!SyncChannelSet.get(textChannel))
                        Actions.reply(member, textChannel, "на этом канале нельзя смотреть время");
                    else
                        textChannel.sendMessage(WorldData.cycle()).queue();
                    break;
                case "help":
                    sendHelp(member, textChannel);
                    break;
                case "allow":
                    checkRun(() -> SyncChannelSet.add(textChannel), textChannel, member, Permission.ADMINISTRATOR);
                    break;
                case "disallow":
                    checkRun(() -> SyncChannelSet.remove(textChannel), textChannel, member, Permission.ADMINISTRATOR);
                    break;
                case "dc":
                    directControl(member, args);
                default:
                    Actions.reply(member, textChannel, "нет такой команды, воспользуйтесь !help для уточнения");
            }

        } catch (PermissionDeniedException | IllegalStateException e) {
            Actions.reply(member, textChannel, "сейчас вы не можете сделать это");
        } catch (Exception e) {
            Actions.reply(member, textChannel, getStackTrace(e));
        }
    }

    private void checkRun(Runnable runnable, Object notnull, Member member, Permission... permission) throws PermissionDeniedException, IllegalStateException {
        if (notnull == null) throw new IllegalStateException();
        if (!member.hasPermission(permission)) throw new PermissionDeniedException();
        runnable.run();
    }

    public void sendHelp(Member member, TextChannel textChannel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
                .setTitle("Справка")
                .setDescription("Доступные вам комманды:")
                .setColor(Color.green)
                .addField("!join", "Добавляет вас в очередь голосового чата (на итогах недели)", true)
                .addField("!leave", "Убирает вас из очереди голосового чата (на итогах недели)", true);
        if (member.hasPermission(Permission.MANAGE_PERMISSIONS)) {
            builder.addField("!mute", "Переводит голосовой канал в режим \"только администрация\"", true);
            builder.addField("!unmute", "Выводит голосовой канал из режима \"только администрация\"", true);
        }
        if (member.hasPermission(Permission.VOICE_MOVE_OTHERS)) {
            builder.addField("!move", "Перемещает всех остальных к вам на канал", true);
        }
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            builder.addField("!queue", "Переводит голосовой канал в режим очереди", true);
            builder.addField("!unqueue", "Выводит голосовой канал из режима очереди", true);
            builder.addField("!next", "Выкидывает текущего пользователя из очереди", true);
        }
        Actions.reply(member, textChannel, "справка была выслана вам личным сообщением");
        member.getUser().openPrivateChannel().queue(channel -> channel.sendMessage(builder.build()).queue());
    }

    private void directControl(Member member, String[] args) {
        if (member.getUser().getIdLong() != 292617833148317696L) throw new AccessControlException("Only authorized people can do this");
        if (args[0].equals("create_room")) {
            member.getGuild().getController().createVoiceChannel(args[1]).queue();
        } else
        if (args[0].equals("remove_room")) {
            member.getGuild().getVoiceChannelById(args[1]).delete().queue();
        }

    }

}
