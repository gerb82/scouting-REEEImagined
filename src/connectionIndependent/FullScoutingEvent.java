package connectionIndependent;

public class FullScoutingEvent {

    private ScoutingEvent event;
    private Short team;
    private short game;
    private byte competition;
    private String mapConfiguration;
    private byte startingLocation;
    private boolean alliance;

    public FullScoutingEvent(ScoutingEvent event, Short team, short game, byte competition, String mapConfiguration, byte startingLocation, boolean alliance) {
        this.event = event;
        this.team = team;
        this.game = game;
        this.competition = competition;
        this.mapConfiguration = mapConfiguration;
        this.startingLocation = startingLocation;
        this.alliance = alliance;
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

    public byte getStartingLocation() {
        return startingLocation;
    }

    public boolean getAlliance() {
        return alliance;
    }
}
