package connectionIndependent.scouted;

import java.util.Date;

public class PitScouterComment extends CommentEvent{

    // TODO Serializing and sql selects and inserts
    private short associatedTeam;

    public PitScouterComment(String text, short associatedTeam, Date timeOfOccurance, Short commentID){
        super(text, commentID);
        setTimeOfOccurance(timeOfOccurance);
        this.associatedTeam = associatedTeam;
    }

    public short getAssociatedTeam() {
        return associatedTeam;
    }

    public void setAssociatedTeam(short associatedTeam) {
        this.associatedTeam = associatedTeam;
    }
}
