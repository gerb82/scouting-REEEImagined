package connectionIndependent.scouted;

public class FullScoutingEvent {

    private ScoutingEvent event;
    private Short team;
    private short game;
    private byte competition;
    private String mapConfiguration;
    private Byte startingLocation;

    public FullScoutingEvent(ScoutingEvent event, Short team, short game, byte competition, String mapConfiguration, Byte startingLocation) {
        this.event = event;
        this.team = team;
        this.game = game;
        this.competition = competition;
        this.mapConfiguration = mapConfiguration;
        this.startingLocation = startingLocation;
    }

    public ScoutingEvent getEvent() {
        return event;
    }

    public Short getTeam() {
        return team;
    }

    public short getGame() {
        return game;
    }

    public byte getCompetition() {
        return competition;
    }

    public String getMapConfiguration() {
        return mapConfiguration;
    }

    public Byte getStartingLocation() {
        return startingLocation;
    }
}
