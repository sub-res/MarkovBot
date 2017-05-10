import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MessageEventListener implements IListener<MessageReceivedEvent> {
    private int historySize = Integer.parseInt(BotProperties.instance().get("hist_size"));
    private int historyMinimum = Integer.parseInt(BotProperties.instance().get("hist_min"));
    private int bufferSize = Integer.parseInt(BotProperties.instance().get("buf_size"));
    private int recallInterval = Integer.parseInt(BotProperties.instance().get("recall_ival"));
    private int msgInterval = Integer.parseInt(BotProperties.instance().get("msg_ival"));
    private int markovOrder = Integer.parseInt(BotProperties.instance().get("markov_order"));
    private long cooldownMs = Integer.parseInt(BotProperties.instance().get("cooldown_ms"));
    private String historyFile = BotProperties.instance().get("history_file");
    private String entrysetFile = BotProperties.instance().get("entryset_file");
    private String autoChannel = BotProperties.instance().get("auto_channel");

    private MarkovChain mc;
    private List<String> history;
    private List<String> entryset;
    private List<String> adminIDs;
    private List<String> bannedIDs;
    private long lastRequest;
    private int messageCount;
    private boolean slurring = false;
    private boolean isOn = true;

    public MessageEventListener() {
        lastRequest = 0;
        messageCount = 0;

        //init ids lists
        adminIDs = new ArrayList<>();
        String adminIDsRaw = BotProperties.instance().get("admin_ids");
        for (String id : adminIDsRaw.split(",")) {
            if (id.length() > 0) {
                adminIDs.add(id);
            }
        }

        bannedIDs = new ArrayList<>();
        String bannedIDsRaw = BotProperties.instance().get("banned_ids");
        for (String id : bannedIDsRaw.split(",")) {
            if (id.length() > 0) {
                bannedIDs.add(id);
            }
        }

        //init markov chain
        mc = new MarkovChain(markovOrder);

        entryset = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(entrysetFile));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.split(" ").length > markovOrder) {
                    entryset.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to read from " + entrysetFile + ": " + e.getMessage());
        }
        mc.addToTable(entryset);

        history = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(historyFile));
            String line;
            while ((line = br.readLine()) != null && entryset.size() < historySize) {
                if (line.split(" ").length > markovOrder) {
                    history.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to read from " + historyFile + ": " + e.getMessage());
        }
        mc.addToTable(history);
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        if (event.getMessage().getContent().startsWith("!") &&
                adminIDs.contains(event.getMessage().getAuthor().getStringID())) {
            handleConfigMsg(event); //config msg handling
        }
        else if (!bannedIDs.contains(event.getMessage().getAuthor().getStringID()) && isOn){
            handleNormalMsg(event); //handle other messages
        }
    }

    //handle config messages for bot admins
    private void handleConfigMsg(MessageReceivedEvent event) {
        IMessage msg = event.getMessage();
        String msgContent = msg.getContent();
        String command = msgContent.split(" ")[0];
        switch (command) {
            case ("!register"):
                if (msg.getMentions().size() > 0) {
                    String reply = "";

                    for (IUser user : msg.getMentions()) {
                        if (!adminIDs.contains(user.getStringID())) {
                            adminIDs.add(user.getStringID());
                        }
                        reply += user.getDisplayName(msg.getGuild()) + " ";
                    }
                    saveIDs();

                    reply += "registered for bot access.";
                    sendReply(reply, msg.getClient(), msg.getChannel());
                }
                break;

            case ("!unregister"):
                if (msg.getMentions().size() > 0) {
                    String reply = "";

                    for (IUser user : msg.getMentions()) {
                        adminIDs.remove(user.getStringID());
                        reply += user.getDisplayName(msg.getGuild()) + " ";
                    }
                    saveIDs();

                    reply += "unregistered for bot access.";
                    sendReply(reply, msg.getClient(), msg.getChannel());
                }
                break;

            case ("!ban"):
                if (msg.getMentions().size() > 0) {
                    String reply = "Banned ";

                    for (IUser user : msg.getMentions()) {
                        if (!bannedIDs.contains(user.getStringID())) {
                            bannedIDs.add(user.getStringID());
                        }
                        reply += user.getDisplayName(msg.getGuild()) + " ";
                    }
                    saveIDs();

                    reply += "from bot usage.";
                    sendReply(reply, msg.getClient(), msg.getChannel());
                }
                break;

            case ("!unban"):
                if (msg.getMentions().size() > 0) {
                    String reply = "Unbanned ";

                    for (IUser user : msg.getMentions()) {
                        bannedIDs.remove(user.getStringID());
                        reply += user.getDisplayName(msg.getGuild()) + " ";
                    }
                    saveIDs();

                    reply += "from bot usage";
                    sendReply(reply, msg.getClient(), msg.getChannel());
                }
                break;

            case ("!rum"):
                slurring = !slurring;
                mc = slurring ? new MarkovChain2(markovOrder + 1) : new MarkovChain(markovOrder);
                mc.addToTable(entryset);
                mc.addToTable(history);
                sendReply((slurring ? "Chugging rum!" : "Rum wore off..."), msg.getClient(), msg.getChannel());
                break;

            case ("!toggle"):
                isOn = !isOn;
                sendReply("Bot is " + (isOn ? "on" : "off") + ".", msg.getClient(), msg.getChannel());
                break;

            case ("!set"):
                String[] splits = msgContent.split(" ");
                if (splits.length < 3 || splits[1].equals("admin_ids")
                        || splits[1].equals("token")) {
                    break;
                }

                String setReply;
                BotProperties.instance().set(splits[1], splits[2]);
                switch (splits[1]) {
                    case ("hist_size"):
                        historySize = Integer.parseInt(BotProperties.instance().get(splits[1]));
                        setReply = splits[1] + " has been set to " + splits[2];
                        break;
                    case ("hist_min"):
                        historyMinimum = Integer.parseInt(BotProperties.instance().get(splits[1]));
                        setReply = splits[1] + " has been set to " + splits[2];
                        break;
                    case ("buf_size"):
                        bufferSize = Integer.parseInt(BotProperties.instance().get(splits[1]));
                        setReply = splits[1] + " has been set to " + splits[2];
                        break;
                    case ("recall_ival"):
                        recallInterval = Integer.parseInt(BotProperties.instance().get(splits[1]));
                        setReply = splits[1] + " has been set to " + splits[2];
                        break;
                    case ("msg_ival"):
                        msgInterval = Integer.parseInt(BotProperties.instance().get(splits[1]));
                        setReply = splits[1] + " has been set to " + splits[2];
                        break;
                    case ("markov_order"):
                        markovOrder = Integer.parseInt(BotProperties.instance().get(splits[1]));
                        mc = slurring ? new MarkovChain2(markovOrder + 1) : new MarkovChain(markovOrder);
                        mc.addToTable(entryset);
                        mc.addToTable(history);
                        setReply = splits[1] + " has been set to " + splits[2];
                        break;
                    case ("cooldown_ms"):
                        cooldownMs = Integer.parseInt(BotProperties.instance().get(splits[1]));
                        setReply = splits[1] + " has been set to " + splits[2];
                        break;
                    default:
                        setReply = "unrecognized or restricted property: " + splits[1];
                        break;
                }
                sendReply(setReply, msg.getClient(), msg.getChannel());
                break;

            case ("!save"):
                BotProperties.instance().save();
                sendReply("Changes have been written to file.", msg.getClient(), msg.getChannel());
                break;

            case ("!status"):
                String statusReply = "Status:\n" +
                        "Bot: " + (isOn ? "ON" : "OFF") + "\n" +
                        "Slurring: " + (slurring ? "ON" : "OFF") + "\n" +
                        "History size: " + history.size() + "\n" +
                        "Message count: " + messageCount + "\n";
                sendReply(statusReply, msg.getClient(), msg.getChannel());
                break;

            case ("!properties"):
                sendReply(BotProperties.instance().getDump(), msg.getClient(), msg.getChannel());
                break;

            case ("!help"):
                String helpReply = "Commands: \n" +
                        "**!register @UserName**\n" +
                        "Register one or more users for bot access.\n" +
                        "**!unregister @UserName**\n" +
                        "Unregister one or more users from bot access.\n" +
                        "**!ban @UserName**\n" +
                        "Ban one or more users  from bot usage.\n" +
                        "**!unban @UserName**\n" +
                        "Unban one or more users from bot usage.\n" +
                        "**!rum**\n" +
                        "JESUS PUT DOWN THAT RUM!\n" +
                        "**!toggle**\n" +
                        "Toggle bot activation.\n" +
                        "**!set [property_name] [value]**\n" +
                        "Set a property.\n" +
                        "**!save**\n" +
                        "Save properties so they persist after rebooting.\n" +
                        "**!properties**\n" +
                        "Show available properties with current values. Properties that are **[restricted]** cannot be changed with **!set**.\n";
                sendReply(helpReply, msg.getClient(), msg.getChannel());
                break;

            default:
                //sendReply("Unrecognized command.", msg.getClient(), msg.getChannel());
                //do nothing instead, otherwise it freaks out when an admin goes "!!" for example
                break;
        }
    }

    //handle non-config messages
    private void handleNormalMsg(MessageReceivedEvent event) {
        IDiscordClient client = event.getClient();
        IMessage msg = event.getMessage();
        String msgContent = msg.getContent();
        IChannel chan = msg.getChannel();

        msgContent = MentionStripper.stripMentions(msgContent, chan);

        String myID = client.getOurUser().getStringID();

        if (!msg.getAuthor().getStringID().equals(client.getOurUser().getStringID())
                && !chan.getName().equals(msg.getAuthor().getName())) {
            messageCount++;

            if ((msgContent.contains("<@" + myID + ">")
                    && System.currentTimeMillis() > lastRequest + cooldownMs
                    && history.size() >= historyMinimum)) {
                //generate reply when @-mentioned
                lastRequest = System.currentTimeMillis(); //reset cooldown
                String reply = mc.getOutput();
                sendReply(reply, client, chan);
            } else if (messageCount >= msgInterval
                    && msg.getChannel().getName().equals(autoChannel)) {
                //limit autoresponse to channel
                //automatic response every msgInterval amount of messages
                messageCount = 0;
                String reply = mc.getOutput();
                sendReply(reply, client, chan);
            } else {
                if (history.size() >= historySize) {
                    history.subList(bufferSize, history.size());
                    mc = slurring ? new MarkovChain2(markovOrder + 1) : new MarkovChain(markovOrder);
                    mc.addToTable(entryset);
                    mc.addToTable(history);
                    System.out.println("Refreshed markov chain.");
                }

                for (String line : msgContent.split("\n")) {
                    if (line.split(" ").length > markovOrder) {
                        history.add(line);
                        mc.addToTable(line);
                        System.out.println("Added: \'" + line + "\' to history (size: " + history.size() + ").");
                    } else {
                        System.out.println("Skipped: \'" + line + "\'");
                    }
                }
            }

            //save history
            if (!history.isEmpty() && history.size() % recallInterval == 0) {
                try {
                    FileWriter writer = new FileWriter(historyFile);

                    String[] history_arr = new String[history.size()];
                    history_arr = history.toArray(history_arr); //copy to array to prevent concurrent modification
                    for (String s : history_arr) {
                        writer.write(s + "\n");
                    }

                    writer.close();
                } catch (Exception e) {
                    System.err.println("Unable to write to " + historyFile + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    //send reply back to channel
    private void sendReply(String reply, IDiscordClient client, IChannel channel)
    {
        try {
            new MessageBuilder(client).withChannel(channel).withContent(reply).build();
        } catch (RateLimitException e) {
            System.err.print("Sending messages too quickly!");
            e.printStackTrace();
        } catch (DiscordException e) {
            System.err.print(e.getErrorMessage());
            e.printStackTrace();
        } catch (MissingPermissionsException e) {
            System.err.print("Missing permissions for channel!");
            e.printStackTrace();
        }
    }

    //save user IDs
    private void saveIDs() {
        String newAdminIDs = "";
        for (int i = 0; i < adminIDs.size(); i++) {
            newAdminIDs += adminIDs.get(i);
            if (i < adminIDs.size() - 1) {
                newAdminIDs += ",";
            }
        }

        String newBannedIDs = "";
        for (int i = 0; i < bannedIDs.size(); i++) {
            newBannedIDs += bannedIDs.get(i);
            if (i < bannedIDs.size() - 1) {
                newBannedIDs += ",";
            }
        }
        BotProperties.instance().set("admin_ids", newAdminIDs);
        BotProperties.instance().set("banned_ids", newBannedIDs);
    }
}
