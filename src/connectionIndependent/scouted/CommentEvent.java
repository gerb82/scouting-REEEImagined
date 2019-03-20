package connectionIndependent.scouted;

import java.io.Serializable;
import java.util.Date;

public class CommentEvent implements Serializable {

    private String text;
    private short commentID;
    private Date timeOfOccurance = null;

    protected CommentEvent(String text, Short commentID) {
        this.text = text;
        this.commentID = commentID == null ? -1 : commentID;
    }

    public Date getTimeOfOccurance() {
        return timeOfOccurance;
    }

    public void setTimeOfOccurance(Date timeOfOccurance) {
        this.timeOfOccurance = timeOfOccurance;
    }

    public short getCommentID() {
        return commentID;
    }

    public void setCommentID(short commentID) {
        this.commentID = commentID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
