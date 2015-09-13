package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "city")
public class City {

    private String name;
    private String type;
    private String tlk_fname;
    private List<PersonRole> personRoles;
    private List<Conversation> conversations;
    private List<Person> people;

    @XmlTransient
    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    @XmlAttribute(name = "tlk_fname")
    public String getTlk_fname() {
        return tlk_fname;
    }

    @XmlElement(name = "personrole")
    public List<PersonRole> getPersonRoles() {
        return personRoles;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTlk_fname(String tlk_fname) {
        this.tlk_fname = tlk_fname;
    }

    public void setPersonRoles(List<PersonRole> personRoles) {
        this.personRoles = personRoles;
    }

    @Override
    public String toString() {
        return String.format("City [name=%s, type=%s, tlk_fname=%s, personRoles=%s]", name, type, tlk_fname, personRoles);
    }

    @XmlTransient
    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    public Person getPersonAt(int x, int y) {
        for (Person p : people) {
            if (p == null) {
                continue;
            }
            //System.out.println(x +","+ y +" p:"+ p.getX() +","+ p.getY());
            if (p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

    public Person resetTalkingFlags() {
        for (Person p : people) {
            if (p == null) {
                continue;
            }
            p.setTalking(false);
        }
        return null;
    }

}
