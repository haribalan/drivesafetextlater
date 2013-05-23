drivesafetextlater
==================

An android app to detect motion with goal of using as much less battery as possible and smart as possible.
Idea of the app is a help people access text message safely or better do an auto respond. 
App is simple andriod application.

Movement detection:
- User can start the DriveMode manual before they start driving.
- When Wifi gets disconnected app starts sets alarm and wakes up and start checking for movement after x minutes for y minutes.
   (as of now x is 2.5 minutes and y is 5minutes)
- PassiveLocationListener (currently not yet implemented)
- CarDock Listener starts like Manual DriveMode Start
- If the movement is detected (if non manual start mode), drivemode is set true and then auto response begins.
- some logic used to increase min time or min distance of location listener to save some battery depending on the car speed.



Database is used to save settings and information, like the auto-text response message etc. Text is read out to the
user (as of now just like that, no permission asked) and auto reponse is sent on the Driving Mode.
TODO is to make this voice recognnition with user directing what to do including the response.

There is also a total app disable feature which can be used if the user is riding as a passenger!

If pretty much self explanatory. I would lauch it some time soon...


Thank You
Hari Raghupathy
