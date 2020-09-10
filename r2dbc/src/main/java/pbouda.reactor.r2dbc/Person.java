package pbouda.reactor.r2dbc;

public class Person {

    private final Integer correlationId;
    private final Integer personId;
    private final String lastname;
    private final String firstname;
    private final String city;
    private final String country;

    public Person(
            Integer correlationId,
            Integer personId,
            String lastname,
            String firstname,
            String city,
            String country) {

        this.correlationId = correlationId;
        this.personId = personId;
        this.lastname = lastname;
        this.firstname = firstname;
        this.city = city;
        this.country = country;
    }

    public Integer getCorrelationId() {
        return correlationId;
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

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return "Person{" +
               "correlationId=" + correlationId +
               ", personId=" + personId +
               ", lastname='" + lastname + '\'' +
               ", firstname='" + firstname + '\'' +
               ", city='" + city + '\'' +
               ", country='" + country + '\'' +
               '}';
    }
}
