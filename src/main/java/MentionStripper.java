import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionStripper {
    private static Pattern userIDPattern = Pattern.compile("<@!*[0-9]*>"); //user ID pattern
    private static Pattern roleIDPattern = Pattern.compile("<@&+[0-9]*>"); //role ID pattern

    //remove all @-mentions so the bot won't ping users
    public static String stripMentions(String msg, IChannel chan) {
        msg = msg.replaceAll("@here", "here");
        msg = msg.replaceAll("@everyone", "everyone");
        for (IRole role : chan.getGuild().getRoles()) {
            msg = msg.replaceAll("@" + role.getName(), role.getName());
        }

        //user mention stripping
        StringBuffer output = new StringBuffer();
        Matcher matcher = userIDPattern.matcher(msg);
        while (matcher.find()) {
            String rep = String.format("%s", getUserName(matcher.group(), chan));
            matcher.appendReplacement(output, rep);
        }
        matcher.appendTail(output);

        //role mention stripping
        msg = output.toString();
        output = new StringBuffer();
        matcher = roleIDPattern.matcher(msg);
        while (matcher.find()) {
            String rep = String.format("%s", getRoleName(matcher.group(), chan));
            matcher.appendReplacement(output, rep);
        }
        matcher.appendTail(output);

        return output.toString();
    }

    private static String getUserName(String id, IChannel chan) {
        String strippedID = id.replaceAll("[!<>@]", ""); //strip non-numeric

        if (strippedID.equals(chan.getClient().getOurUser().getStringID())) {
            return id;
        }

        //reverse username lookup, not very pretty
        //maybe bring this up w/ Discord4j devs?
        for (IUser user : chan.getUsersHere()) {
            if (user.getStringID().equals(strippedID)) {
                try {
                    return user.getDisplayName(chan.getGuild());
                } catch (NoSuchElementException e) {
                    return user.getNicknameForGuild(chan.getGuild());
                }

            }
        }

        return ""; //TODO: find a more elegant solution than this
    }

    //role mention stripping
    private static String getRoleName(String id, IChannel chan) {
        String strippedID = id.replaceAll("[&<>@]", ""); //strip non-numeric

        for (IRole role : chan.getGuild().getRoles()) {
            if (role.getStringID().equals(strippedID)) {
                return role.getName();
            }
        }

        return "";
    }
}
