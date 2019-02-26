package connectionIndependent.scouted;

import java.util.ArrayList;

public class ScoutedTeam {

    private short number;
    private String name;
    private ArrayList<String> competitions;

    public ScoutedTeam(short number, String name, ArrayList<String> competitions) {
        this.number = number;
        this.name = name;
        this.competitions = competitions;
    }

    public short getNumber() {
        return number;
    }

    public void setNumber(short number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(ArrayList<String> competitions) {
        this.competitions = competitions;
    }

    @Override
    public String toString() {
        return name + " #" + number;
    }
}

