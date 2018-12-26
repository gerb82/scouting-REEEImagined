package connectionIndependent;

public class FullScoutingEvent {

    private ScoutingEvent event;
    private Integer team;
    private int game;
    private int competition;
    private Integer mapConfiguration;
    private int startingLocation;
    private int alliance;

    public FullScoutingEvent(ScoutingEvent event, Integer team, int game, int competition, Integer mapConfiguration, int startingLocation, int alliance) {
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

    public Integer getTeam() {
        return team;
    }

    public int getGame() {
        return game;
    }

    public int getCompetition() {
        return competition;
    }

    public Integer getMapConfiguration() {
        return mapConfiguration;
    }

    public int getStartingLocation() {
        return startingLocation;
    }

    public int getAlliance() {
        return alliance;
    }
}
