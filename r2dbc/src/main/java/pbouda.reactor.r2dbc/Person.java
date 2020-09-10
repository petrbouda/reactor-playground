package pbouda.reactor.r2dbc;

public class Person {
    private final Integer personId;
    private final String lastname;
    private final String firstname;

    public Person(Integer personId, String lastname, String firstname) {
        this.personId = personId;
        this.lastname = lastname;
        this.firstname = firstname;
    }

    public Integer getPersonId() {
        return personId;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFirstname() {
        return firstname;
    }
}