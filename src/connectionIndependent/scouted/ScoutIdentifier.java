package connectionIndependent.scouted;

import java.io.Serializable;

public class ScoutIdentifier implements Serializable{

    private String competition;
    private short game;
    private String team;
    private byte priority;
    private byte state;

    public ScoutIdentifier(String competition, short game, String team, byte priority, byte state) {
        this.competition = competition;
        this.game = game;
        this.team = team;
        this.priority = priority;
        this.state = state;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public short getGame() {
        return game;
    }

    public void setGame(short game) {
        this.game = game;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return getTeam();
    }
}
