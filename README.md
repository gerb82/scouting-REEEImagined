Scouting REEEImagined
so why? just... Y(team)?




ok memes aside: why are we here?
so as you know, the current scouting system is a jabbering pile of mess.
no other way to describe it. it did it's job wonderfully, it's good and all.
but it's a nightmare to maintain, and it's a bit of a mess.
in order to use the system, you need the main server running a linux virtual machine.
and then you need to connect other computers to it, to scout.
and you open a port for scouters, and another for people who wanna view the data.
they view it through kibana, which incidentally has a memory leak.
and just, no matter how you look at it, it's redundant.
the ui for scouters is weird, the main computer's ui is weird, and kibana is just bad.
what's even worse is that the war room is extremely inefficient, because half the
time they are trying to extract the data from the system.
and all of these are nitpicks yea, but the system is hard to config, so we're stuck with that.
it's also written in a weird web language, when we work in java. so every year someone has to learn that weird language in order to update it.



so, what now?
simple:
we nuke it from orbit (not the team), and we rebuild it. why rebuild the wheel?
because our wheel is currently a 2d square, and the rotational axis goes from one corner to the opposite one.

ok ok, back to serious things. what does this system need to do?

access:
local network: scouting area only.
public network: anything that cannot or should not be connected directly to the local system/can also be connected through the internet. this network will be password protected, with different roles having different passwords, as this is the only way to stop
someone from connecting to it and messing with things severely, be it a person in the team in a wrong role, or even an outsider.

1) host computer. the code runs on this one. it is the equivalent of the server side on the last system. it will run the database, the configuration, everything. it will also be the only access-point to the internet on the local network.
2) scouting computers. these computers connect to the host computer and scout through it. same concept as the last system. part of the local system. will have a list of games, video player, and event list per game as well as ways to add them.
3) war room computer. this is the computer that the war room will use. it will be connected to the host computer and it will display the data in an easy to read way. will be directly connected to coach board. also part of the local network.
4) coach board connection. this system will connect directly to the coach board system when it is remade properly. connected through the war room computer.
5) pit scouters access. pit scouting is gonna happen through this system. pit scouters will be able to submit reports through their phones. will be connected through the public network.
6) data access. war room will have it's own specialized interface to access all the data easily. the data will still be accessible for others in case the need arises. will support both the public and local networks.
7) other teams access. it is not that uncommon for other teams to ask for data. when another team is allowed to access, we need to support giving them the data they want without affecting the system in any way shape or form. LOCAL NETWORK ONLY.
8) technician access. i'm not kidding. i'm gonna have a beeper for the system administrator when something malfunctions. i don't care where he is, it will beep. also, basic debugging panel. part of the public network. it will mimic certain features from the host computer, but the host computer will have the "full capabilities"
9) deep scouters. any and all tactical information that is not just performance data will have it's way to be entered. through scouting computers as well as remotely. this way, strategy scouters that analyze games will have an easy way to write their notes. public network mainly.
10) possibility: while live scouting was disregarded completely in the meeting, it will be experimented with when the system is live. if we find it to be legitimate, it will happen. public network mainly.
11) possibility: cameraman access. i hate runners. i think every runner is a technological failure. as such, we might make it so that the camera videos can be added easily. public network mainly.

12) scale-ability:
well, really, just have no limit on how many of each connection we can run at the same time.

13) future compatibility: EVERYTHING, and i mean EVERYTHING, will be based around a central configuration system.
that means most program-wide variables, data collected, forms it's displayed in, gui, everything.
14) for data collected, we will have different types of data that can be collected, and different attributes per paramter collected.
15) also, we might add python support in the future, so that it will be possible to add custom data types, attributes, or ways to interact with it.
it might not necessarily be python. we'll see how we do it when we get there.
16) we will also have a map configuration, with the possibility to have a few of them if it is required (for a changing map like the stronghold game)
17) game-wide things will also happen (for power-ups etc)
18) the gui for the different screens will be configure-able as well. the gui will be in fxml format, and editable, with a default preset.
we will have a testing environment for the different screens to test them without starting up the whole system.
19) configuration editor. ideally, the configurators will not touch the configuration files directly, and instead edit them through a specialized editor. this way we can promise that we won't have any syntax issues.


that's... a lot of features. i know. so work-plan (i'll be using feature numbers):
1) running alpha version - start of august. 10th of august worst case scenario.
alpha includes: 1 and 2 zone. 3 somewhere along the way. maybe 4. 6 for debugging, aka crude. 12 is a basic rule. 13 is a must. 14 too. 16, 17 and 18 must have some sort of basic support.
gui will be pretty crude and just functional, nothing more. configuration edited by text
2) running beta version - rosh-hashana in the worst case possible.
beta includes: everything in the alpha. easy way to create connections for all the different clients, and as many of them being functional as possible. deeper support for the configuration with more options. at least start working on 15.
gui will be easy on the eyes, might need some optimization. maybe the start of a configuration editor
3) running final version - off-season competition.
final version includes: everything that was considered a must, at a level where it can stay for years without being touched. more features can be added, and will be,
and things will be improved. but in theory, it should be good enough that if we disappear, it will be functional for the years to come. configuration editor being complete.
4) because we can version - day of the kickoff.
because we can version includes: any nitpicks you had about the system: this is your version. easter-eggs, hidden jokes and features, optimization, anything, this is where it all happens.