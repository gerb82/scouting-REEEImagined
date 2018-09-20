package Editor.EditorUtils;

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextInputControl;
import utilities.Utils;

public class TextLine extends TextInputControl{

    private TextLine lastLine = null;
    private TextLine nextLine = null;

    public static final class LineContent implements Content{
        private ExpressionHelper<String> helper = null;
        private StringBuilder characters = new StringBuilder();
        private TextLine parent = null;

        @Override public String get(int start, int end) {
            return characters.substring(start, end);
        }

        private String getRemainingPart(int start){
            start++;
            String test = characters.substring(start);
            characters.delete(start, characters.length());
            return test;
        }

        private void insertToEnd(String string){
            insert(characters.length(), string, true);
        }

        @Override public void insert(int index, String text, boolean notifyListeners) {
            StringBuilder finalText = new StringBuilder();
            TextLine current = parent;
            boolean deleteNext = false;
            for(char c : text.toCharArray()){
                if(deleteNext){ deleteNext = false; continue;}
                switch(Utils.IdentifyChar(c)){
                    case NULL:
                    case INVALID:
                        continue;
                    case NEW_LINE:
                        String remaining = getRemainingPart(index);
                        current.getLineContent().addText(index, finalText.toString(), notifyListeners);
                        finalText = new StringBuilder(remaining);
                        current = current.newLine();
                        continue;
                    case BACK_SPACE:
                        if(finalText.toString().isEmpty()) current.getLineContent().deleteBack(index, notifyListeners);
                        else finalText.deleteCharAt(characters.length());
                        continue;
                    case DELETE:
                        deleteNext = true;
                    default:
                        finalText.append(c);
                }
            }
            if(deleteNext){deleteForward(index)}
        }

        private void addText(int index, String text, boolean notifyListeners){
            text = TextInputControl.filterInput(text, true, true);
            if (!text.isEmpty()) {
                characters.insert(index, text);
                if (notifyListeners) {
                    ExpressionHelper.fireValueChangedEvent(helper);
                }
            }
        }

        private void deleteForward(int index){
            delete(index);
        }

        @Override public void delete(int start, int end, boolean notifyListeners) {
            if (end > start) {
                characters.delete(start, end);
                if (notifyListeners) {
                    ExpressionHelper.fireValueChangedEvent(helper);
                }
            }
        }

        public void deleteBack(int index, boolean notifyListeners){
            if(index > 0) delete(index--, index, notifyListeners);
            else deleteLine();
        }

        public void deleteLine(){
            if(parent.getLastLine() != null) {
                parent.getLastLine().getLineContent().insertToEnd(getRemainingPart(0));
                parent.selfDestruct();
            }
        }

        @Override public int length() {
            return characters.length();
        }

        @Override public String get() {
            return characters.toString();
        }

        @Override public void addListener(ChangeListener<? super String> changeListener) {
            helper = ExpressionHelper.addListener(helper, this, changeListener);
        }

        @Override public void removeListener(ChangeListener<? super String> changeListener) {
            helper = ExpressionHelper.removeListener(helper, changeListener);
        }

        @Override public String getValue() {
            return get();
        }

        @Override public void addListener(InvalidationListener listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override public void removeListener(InvalidationListener listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }
    }
    public TextLine(){
        super();
    }

    private TextLine newLine(){

    }

    private void selfDestruct(){

    }

    private TextLine getLastLine(){
        return lastLine;
    }

    private TextLine getNextLine(){
        return nextLine;
    }

    private void setNextLine(TextLine line){
        this.nextLine = line;
    }

    private void setLastLine(TextLine line){
        this.lastLine = line;
    }

    public LineContent getLineContent(){
        return (LineContent)super.getContent();
    }

}
