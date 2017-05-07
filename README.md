# MarkovBot
A markov chain based Discord bot using the [Discord4J](https://github.com/austinv11/Discord4J) API.

Primarily created to liven up a friend's Discord server a little, I decided to make this bot more configurable so others can use it on their server as well.

####What does it do?

Markov Bot reads messages from all channels it has access to, disassembles them and creates a [markov chain](https://en.wikipedia.org/wiki/Markov_chain) which it uses to spout nonsensical (and often quite humorous) replies when @-mentioned, or randomly injects a reply every x number of replies.

####How do I use it?

Simply run a Maven build on the project (preferably ``package``), change the config.ini file, avatar.png and entryset.txt files to your preference and run the generated jar from command line, it's that easy!

####How do I configure it?

MarkovBot comes with a config.ini file which you can edit to suit your preferences. For it to work at its barest minimum, it requires a token. Follow [these steps](https://github.com/reactiflux/discord-irc/wiki/Creating-a-discord-bot-&-getting-a-token) to acquire your token, and add it to the ``token`` property in config.ini

Be also sure to add your Discord user ID to the ``admin_ids`` property in config.ini, it makes changing parameters on the fly much easier. To get your user ID, simply enable Dev Mode in Discord, right click your avatar and click "Copy ID".

Here is a rundown of the properties in config.ini:

* ``token`` is the token the bot needs to connect to your server.
* ``bot_name`` specifies the bot name.
* ``hist_size`` is the maximum amount of messages that Markov Bot will keep track of.
* ``hist_min`` is the minimum amount of messages that Markov Bot will need before it will become active.
* ``buf_size`` is the amount of messages that Markov Bot will drop once the amount of messages exceeds ``hist_size``
* ``recall_ival`` is the message amount interval between which Markov Bot will save its message history to history.txt
* ``msg_ival`` is the message amount interval between which Markov Bot will autopost in the specified autoposting channel (currently restricted to 1 channel).
* ``markov_order`` is the amount of words (or the amount of letters + 1 if slurring is enabled; see below for details on slurring mode) the markov chain will use to predict the next word. The lower this value, the more nonsensical Markov Bot becomes. Conversely, the higher this value, the more it will lean towards quoting messages verbatim.
* ``history_file`` specifies the history file where Markov Bot saves its messages. I do not recommend changing this.
* ``entryset_file`` specifies the file that contains Markov Bot's permanent set, i.e. messages that will persist unlike those in the history file.
* ``admin_ids`` specifies the IDs of users that can access bot commands, separated by commas. See below for a rundown on bot commands.
* ``banned_ids`` specifies the IDs of users that will not get a response from Markov Bot if they attempt to @-mention it. Markov Bot will also not add their messages to its history.
* ``auto_channel`` specifies the name of the channel where Markov Bot will autopost. If you want to disable autopost, this value can be left unchanged.

Markov bot also comes with a variety of bot commands. These bot commands are only accessible to those whose IDs are in ``admin_ids``:

* ``!register @UserName`` registers one or more users for bot access.
* ``!unregister @UserName`` unregisters one or more users from bot access.
* ``!ban @UserName`` bans one or more users  from bot usage.
* ``unban @UserName`` unbans one or more users from bot usage.
* ``!rum`` makes Markov Bot enter slurring mode, where it builds a markov chain built on individual letters rather than words, making it seem like the bot had a little too much to drink.
* ``!toggle`` toggles bot activation.
* ``!set [property_name] [value]`` sets a property that's specified in config.ini if it's not restricted.
* ``!save`` saves properties so they persist after rebooting.
* ``!properties`` shows available properties with current values. Properties that are ``[restricted]`` cannot be changed with ``!set``.
* ``!status`` shows minor debug information such as history size, number of messages since last autopost, whether the bot is on or not and whether slurring mode is on or not.

####So is there anything wrong with it?

Probably! So should you find anything that needs fixing or if there's a missing feature you require, don't hesitate to contact me!
