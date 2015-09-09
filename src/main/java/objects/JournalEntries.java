package objects;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entries")
public class JournalEntries {

    public static class EntryComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            JournalEntry e1 = (JournalEntry) o1;
            JournalEntry e2 = (JournalEntry) o2;
            if (!e1.getLocation().equals(e2.getLocation())) {
                return e1.getLocation().compareTo(e2.getLocation());
            }
            if (!e1.getName().equals(e2.getName())) {
                return e1.getName().compareTo(e2.getName());
            }
            return 0;
        }

    }

    public static final EntryComparator entryCompare = new EntryComparator();

    private List<JournalEntry> entries = new ArrayList<>();

    @XmlElement(name = "entry")
    public List<JournalEntry> getEntries() {
        return entries;
    }

    public void add(JournalEntry e) {
        if (!entries.contains(e)) {
            entries.add(e);
        }
    }

    public Array<JournalEntry> toArray(Skin skin) {

        Array<JournalEntry> ar = new Array<>();

        for (JournalEntry e : entries) {
            CheckBox cb = new CheckBox("", skin, "journal");
            cb.setChecked(e.getFinished());
            e.setCheckbox(cb);
            ar.add(e);
        }
        return ar;
    }

    public void fromArray(Array<JournalEntry> ar) {
        this.entries.clear();
        for (int i = 0; i < ar.size; i++) {
            JournalEntry e = ar.get(i);
            e.setFinished(e.getCheckbox().isChecked());
            this.entries.add(e);
        }
    }

}
