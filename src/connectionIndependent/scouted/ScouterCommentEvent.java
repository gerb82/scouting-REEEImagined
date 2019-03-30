package connectionIndependent.scouted;

public class ScouterCommentEvent extends CommentEvent {

    // TODO Serializing and sql selects and inserts
    private short associatedTeam;
    private short associatedGame;
    private transient ScoutingEvent associatedChain;
    private Short timeStamp;

    public ScouterCommentEvent(String text, short associatedTeam, short associatedGame, ScoutingEvent associatedChain, Short timeStamp, Short commentID){
        super(text, commentID);
        this.associatedTeam = associatedTeam;
        this.associatedGame = associatedGame;
        if(associatedChain != null){
            associatedChain.addRelatedComment(this);
            this.associatedChain = associatedChain;
        }
        this.timeStamp = timeStamp;
    }

    public short getAssociatedTeam() {
        return associatedTeam;
    }

    public void setAssociatedTeam(short associatedTeam) {
        this.associatedTeam = associatedTeam;
    }

    public short getAssociatedGame() {
        return associatedGame;
    }

    public void setAssociatedGame(short associatedGame) {
        this.associatedGame = associatedGame;
    }

    public ScoutingEvent getAssociatedChain() {
        return associatedChain;
    }

    public void setAssociatedChain(ScoutingEvent associatedChain) {
        this.associatedChain = associatedChain;
    }

    public Short getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Short timeStamp) {
        this.timeStamp = timeStamp;
    }
}
