package net.minecraftforge.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.util.EnumChatFormatting;
import static net.minecraft.util.EnumChatFormatting.*;

/**
 * The class that handles client-side chat commands. You should register any
 * commands that you want handled on the client with this command handler.
 * 
 * If there is a command with the same name registered both on the server and
 * client, the client takes precedence!
 * 
 */
public class ClientCommandHandler extends CommandHandler
{
    public static final ClientCommandHandler instance = new ClientCommandHandler();

    public String[] latestAutoComplete = null;

    /**
     * @return 1 if successfully executed, 0 if wrong usage, it doesn't exist or
     *         it was canceled.
     */
    @Override
    public int executeCommand(ICommandSender sender, String message)
    {
        message = message.trim();

        if (message.startsWith("/"))
        {
            message = message.substring(1);
        }

        String[] temp = message.split(" ");
        String[] args = new String[temp.length - 1];
        String commandName = temp[0];
        System.arraycopy(temp, 1, args, 0, args.length);
        ICommand icommand = (ICommand) getCommands().get(commandName);

        try
        {
            if (icommand == null)
            {
                return 0;
            }

            if (icommand.canCommandSenderUseCommand(sender))
            {
                CommandEvent event = new CommandEvent(icommand, sender, args);
                if (MinecraftForge.EVENT_BUS.post(event))
                {
                    if (event.exception != null)
                    {
                        throw event.exception;
                    }
                    return 0;
                }

                icommand.processCommand(sender, args);
                return 1;
            }
            else
            {
                sender.func_145747_a(format(RED, "commands.generic.permission"));
            }
        }
        catch (WrongUsageException wue)
        {
            sender.func_145747_a(format(RED, "commands.generic.usage", format(RED, wue.getMessage(), wue.getErrorOjbects())));
        }
        catch (CommandException ce)
        {
            sender.func_145747_a(format(RED, ce.getMessage(), ce.getErrorOjbects()));
        }
        catch (Throwable t)
        {
            sender.func_145747_a(format(RED, "commands.generic.exception"));
            t.printStackTrace();
        }

        return 0;
    }

    //Couple of helpers because the mcp names are stupid and long...
    private ChatComponentTranslation format(EnumChatFormatting color, String str, Object... args)
    {
        ChatComponentTranslation ret = new ChatComponentTranslation(str, args);
        ret.func_150256_b().func_150238_a(color);
        return ret;
    }

    public void autoComplete(String leftOfCursor, String full)
    {
        latestAutoComplete = null;

        if (leftOfCursor.charAt(0) == '/')
        {
            leftOfCursor = leftOfCursor.substring(1);

            Minecraft mc = FMLClientHandler.instance().getClient();
            if (mc.currentScreen instanceof GuiChat)
            {
                @SuppressWarnings("unchecked")
                List<String> commands = getPossibleCommands(mc.thePlayer, leftOfCursor);
                if (commands != null && !commands.isEmpty())
                {
                    if (leftOfCursor.indexOf(' ') == -1)
                    {
                        for (int i = 0; i < commands.size(); i++)
                        {
                            commands.set(i, GRAY + "/" + commands.get(i) + RESET);
                        }
                    }
                    else
                    {
                        for (int i = 0; i < commands.size(); i++)
                        {
                            commands.set(i, GRAY + commands.get(i) + RESET);
                        }
                    }

                    latestAutoComplete = commands.toArray(new String[commands.size()]);
                }
            }
        }
    }
}