TODO (in order of priority):
1.make Hoppix-Updater write all the changes from old build to new, not just latest
2.make Hoppix-Updater set profile for you through json-file 'launcher_profiles.json'~ and change forge version whenever forge is updated. close already opened launchers otherwise launcher_profiles.json might be overriden by launcher on closing :/

v1.8:
- Changed the way token system works, no more weird encrypting

v1.7:
- fixed stupidness in isTokenValid(), token is now adding to bat-file after an update correctly

v1.6:
- adding token to bat-file after an update  

v1.5:
- access tokens are implemented
- builds require password

v1.4:
- clearOlderVersions() now deletes all the files in current directory that matches 'Hoppix-Updater-XXX.jar' except itself and 'Hoppix-Updater-XXX-BIN.jar'-s
- files 'forgeversion.txt', 'buildversion.txt' and 'updaterversion.txt' moved up to /info/ on the remote server. should fix 'people using Hoppix-Updater v1.0'

v1.3:
- getLatestChanges() now ignores text between ':' and '#'
- getUpdaterVersion() now deletes local 'updaterversion.txt' after reading it

v1.2:
- any time an error has been thrown it deletes 'forgeversion.txt' and 'buildversion.txt' to fix 'this damn thing is thinking that i'm updated yet'
- isForgeUpdated() and isBuildUpdated() is now reading internal variables that was written by getForgeVersion() and getBuildVersion(), not from 'forgeversion.txt' and 'buildversion.txt'

v1.1:
- Hoppix-Updater is now able to update itself
- Automatically detects and deletes older versions of Hoppix-Updater but it'll only work with -1.0/-0.1 versions. example: 2.1 will only delete 1.1 and 2.0. it's not finished and works really weird

v1.0:
- Initial build