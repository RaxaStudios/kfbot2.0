# Summary

This is a simple bot to connect to Twitch IRC.

## Details about Twitch IRC
You can find it [here](https://help.twitch.tv/customer/en/portal/articles/1302780-twitch-irc)

## Development

The development is being done on [Netbeans](https://netbeans.org/)

## Deployment

Built using Maven repositories

## Documentation


**Setup:**

All configurations can be done within the kfbot.xml file

**kfbot.xml should be located in the same folder as TwitchBotX-1.0.jar**

`<myChannel>` your channel name

`<botAccount>` bot's account name

`<botOAUTH>` obtained from Twitch oauth tool

`<botClientID>` obtained in settings on bot account or using oauth API access

`<youtubeAPI>` youtube key obtained through Google APIs

*Note:*

	All other settings, commands, counters, and filters can be set from chat
 ***

**Broadcaster only commands:**

!command-add `<!command_name>` `<message>`

!command-delete `<!command_name>`

!command-edit `<!command_name>` `<new message>`

!command-auth `<!command_name>` `<auth options:+- <username> <m> <s> <a>>`

	m = moderator
	s = subscriber
	a = anyone in chat
    
!command-enable `<!command_name>`

!command-disable `<!command_name>`

!command-repeat `<!command_name>` `<true|false>`

!command-delay `<!command_name>` `<initial delay in milliseconds>`

!command-interval `<!command_name>` `<interval in milliseconds>`

!command-sound `<!command_name>` `<filename.wav>`

!set-msgCache `<#2-100>`

!set-pyramidResponse `<message>`

*Note:*

	sounds must be in .wav format, this may change with future API changes
	all information can be found within the kfbot1.0.xml file
	auth="" and all other roles
***
**Editor + commands:**

`<Editor must be set manually in XML matching twitch name exactly>`

!filter-add `<msg>`

!filter-delete `<msg>`

!filter-all 

*Note:* 

	filters auto set to 10 minute timeout
	all bot messages sent in whispers to user of command
	v1.09 will allow for whisper only usage of filter system
	auth="+username1 +username2 -m -s -a" 
***

**Moderator + commands:**

!cnt-add `<counter name>`

!cnt-delete `<counter name>`

!cnt-set `<counter name>` `<#>`

!cnt-current `<counter name>`

!countadd `<counter name>` `<#>`

!totals 

*Note:*

	-add creates a new counter with value 0
	countadd adds the <#> to the counter's total
	totals shows all counters and values
		currently capped at 4 counters
		added to v1.09 enhancements
	+m authorization
***
**Viewer + commands:**

!uptime 

!followage

*Note:*

	+a authorization
