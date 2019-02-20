package connectionIndependent.scouted;

public class ScoutedGame {

    private short game;
    private String name;
    private byte competition;
    private Short redAllianceScore;
    private Short blueAllianceScore;
    private Byte redAllianceRP;
    private Byte blueAllianceRP;
    private String mapConfiguration;
    private Short teamNumber1;
    private Short teamNumber2;
    private Short teamNumber3;
    private Short teamNumber4;
    private Short teamNumber5;
    private Short teamNumber6;
    private boolean happened;

    public ScoutedGame(short game, byte competition, String name, short redAllianceScore, short blueAllianceScore, byte redAllianceRP, byte blueAllianceRP, String mapConfiguration, Short teamNumber1, Short teamNumber2, Short teamNumber3, Short teamNumber4, Short teamNumber5, Short teamNumber6) {
        happened = true;
        this.game = game;
        this.competition = competition;
        this.name = name;
        this.redAllianceScore = redAllianceScore;
        this.blueAllianceScore = blueAllianceScore;
        this.redAllianceRP = redAllianceRP;
        this.blueAllianceRP = blueAllianceRP;
        this.mapConfiguration = mapConfiguration;
        this.teamNumber1 = teamNumber1;
        this.teamNumber2 = teamNumber2;
        this.teamNumber3 = teamNumber3;
        this.teamNumber4 = teamNumber4;
        this.teamNumber5 = teamNumber5;
        this.teamNumber6 = teamNumber6;
    }

    public ScoutedGame(short game, byte competition, String name, Short teamNumber1, Short teamNumber2, Short teamNumber3, Short teamNumber4, Short teamNumber5, Short teamNumber6) {
        happened = false;
        this.game = game;
        this.competition = competition;
        this.name = name;
        redAllianceScore = null;
        blueAllianceScore = null;
        redAllianceRP = null;
        blueAllianceRP = null;
        mapConfiguration = null;
        this.teamNumber1 = teamNumber1;
        this.teamNumber2 = teamNumber2;
        this.teamNumber3 = teamNumber3;
        this.teamNumber4 = teamNumber4;
        this.teamNumber5 = teamNumber5;
        this.teamNumber6 = teamNumber6;
    }

    public boolean didHappen() {
        return happened;
    }

    public void justHappened(short redAllianceScore, short blueAllianceScore, byte redAllianceRP, byte blueAllianceRP, String mapConfiguration) {
        happened = true;
        this.redAllianceScore = redAllianceScore;
        this.blueAllianceScore = blueAllianceScore;
        this.redAllianceRP = redAllianceRP;
        this.blueAllianceRP = blueAllianceRP;
        this.mapConfiguration = mapConfiguration;
    }

    public short getGame() {
        return game;
    }

    public void setGame(short game) {
        this.game = game;
    }

    public byte getCompetition() {
        return competition;
    }

    public void setCompetition(byte competition) {
        this.competition = competition;
    }

    public short getRedAllianceScore() {
        return redAllianceScore;
    }

    public void setRedAllianceScore(short redAllianceScore) {
        this.redAllianceScore = redAllianceScore;
    }

    public short getBlueAllianceScore() {
        return blueAllianceScore;
    }

    public void setBlueAllianceScore(short blueAllianceScore) {
        this.blueAllianceScore = blueAllianceScore;
    }

    public byte getRedAllianceRP() {
        return redAllianceRP;
    }

    public void setRedAllianceRP(byte redAllianceRP) {
        this.redAllianceRP = redAllianceRP;
    }

    public byte getBlueAllianceRP() {
        return blueAllianceRP;
    }

    public void setBlueAllianceRP(byte blueAllianceRP) {
        this.blueAllianceRP = blueAllianceRP;
    }

    public String getMapConfiguration() {
        return mapConfiguration;
    }

    public void setMapConfiguration(String mapConfiguration) {
        this.mapConfiguration = mapConfiguration;
    }

    public Short getTeamNumber1() {
        return teamNumber1;
    }

    public void setTeamNumber1(Short teamNumber1) {
        this.teamNumber1 = teamNumber1;
    }

    public Short getTeamNumber2() {
        return teamNumber2;
    }

    public void setTeamNumber2(Short teamNumber2) {
        this.teamNumber2 = teamNumber2;
    }

    public Short getTeamNumber3() {
        return teamNumber3;
    }

    public void setTeamNumber3(Short teamNumber3) {
        this.teamNumber3 = teamNumber3;
    }

    public Short getTeamNumber4() {
        return teamNumber4;
    }

    public void setTeamNumber4(Short teamNumber4) {
        this.teamNumber4 = teamNumber4;
    }

    public Short getTeamNumber5() {
        return teamNumber5;
    }

    public void setTeamNumber5(Short teamNumber5) {
        this.teamNumber5 = teamNumber5;
    }

    public Short getTeamNumber6() {
        return teamNumber6;
    }

    public void setTeamNumber6(Short teamNumber6) {
        this.teamNumber6 = teamNumber6;
    }

    public String[] getTeamsArray() {
        return new String[]{String.valueOf(teamNumber1), String.valueOf(teamNumber2), String.valueOf(teamNumber3), String.valueOf(teamNumber4), String.valueOf(teamNumber5), String.valueOf(teamNumber6)};
    }

    public Short[] teamsArray() {
        return new Short[]{teamNumber1, teamNumber2, teamNumber3, teamNumber4, teamNumber5, teamNumber6};
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}