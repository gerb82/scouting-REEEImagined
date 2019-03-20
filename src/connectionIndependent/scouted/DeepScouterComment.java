package connectionIndependent.scouted;

import java.util.Date;

public class DeepScouterComment extends CommentEvent {

    // TODO Serializing and sql selects and inserts
    private Short associatedTeam;
    private short associatedGame;

    public DeepScouterComment(String text, Short associatedTeam, short associatedGame, Date timeOfOccurance, Short commentID){
        super(text, commentID);
        setTimeOfOccurance(timeOfOccurance);
        this.associatedTeam = associatedTeam;
        this.associatedGame = associatedGame;
    }

    public Short getAssociatedTeam() {
        return associatedTeam;
    }

    public void setAssociatedTeam(Short associatedTeam) {
        this.associatedTeam = associatedTeam;
    }

    public short getAssociatedGame() {
        return associatedGame;
    }

    public void setAssociatedGame(short associatedGame) {
        this.associatedGame = associatedGame;
    }
}
