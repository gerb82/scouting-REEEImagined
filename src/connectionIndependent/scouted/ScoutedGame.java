package connectionIndependent.scouted;

public class ScoutedGame {

    private short game;
    private byte competition;
    private short redAllianceScore;
    private short blueAllianceScore;
    private byte redAllianceRP;
    private byte blueAllianceRP;
    private String mapConfiguration;
    private short teamNumber1;
    private short teamNumber2;
    private short teamNumber3;
    private short teamNumber4;
    private short teamNumber5;
    private short teamNumber6;

    public ScoutedGame(short game, byte competition, short redAllianceScore, short blueAllianceScore, byte redAllianceRP, byte blueAllianceRP, String mapConfiguration, short teamNumber1, short teamNumber2, short teamNumber3, short teamNumber4, short teamNumber5, short teamNumber6) {
        this.game = game;
        this.competition = competition;
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

    public short getTeamNumber1() {
        return teamNumber1;
    }

    public void setTeamNumber1(short teamNumber1) {
        this.teamNumber1 = teamNumber1;
    }

    public short getTeamNumber2() {
        return teamNumber2;
    }

    public void setTeamNumber2(short teamNumber2) {
        this.teamNumber2 = teamNumber2;
    }

    public short getTeamNumber3() {
        return teamNumber3;
    }

    public void setTeamNumber3(short teamNumber3) {
        this.teamNumber3 = teamNumber3;
    }

    public short getTeamNumber4() {
        return teamNumber4;
    }

    public void setTeamNumber4(short teamNumber4) {
        this.teamNumber4 = teamNumber4;
    }

    public short getTeamNumber5() {
        return teamNumber5;
    }

    public void setTeamNumber5(short teamNumber5) {
        this.teamNumber5 = teamNumber5;
    }

    public short getTeamNumber6() {
        return teamNumber6;
    }

    public void setTeamNumber6(short teamNumber6) {
        this.teamNumber6 = teamNumber6;
    }

    public String[] getTeamsArray() {
        return new String[]{String.valueOf(teamNumber1), String.valueOf(teamNumber2), String.valueOf(teamNumber3), String.valueOf(teamNumber4), String.valueOf(teamNumber5), String.valueOf(teamNumber6)};
    }
}