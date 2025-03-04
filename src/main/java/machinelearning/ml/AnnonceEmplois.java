package machinelearning.ml;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class AnnonceEmplois {

    private String publicationDate;
    private String applicationDeadline; 
    private String city;
    private String contractType; 
    private String degree; 
    private String experience;
    private String remoteWork;
    private String sector;

    public AnnonceEmplois() {}

    public AnnonceEmplois(String publicationDate, String applicationDeadline, String city, String contractType, 
    String degree, String experience, String remoteWork, String sector) {
        this.publicationDate = publicationDate;
        this.applicationDeadline = applicationDeadline;
        this.city = city;
        this.contractType = contractType;
        this.degree = degree;
        this.experience = experience;
        this.remoteWork = remoteWork;
        this.sector = sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getRemoteWork() {
        return remoteWork;
    }

    public void setRemoteWork(String remoteWork) {
        this.remoteWork = remoteWork;
    }

    public String toString() {
        return "AnnonceEmplois {" +
            ",publicationDate='" + publicationDate + '\'' +
            ", applicationDeadline='" + applicationDeadline + '\'' +
            ", sector='" + sector + '\'' +
            ", city='" + city + '\'' +
            ", contractType='" + contractType + '\'' +
            ", degree='" + degree + '\'' +
            ", experience='" + experience + '\'' +
            ", remoteWork='" + remoteWork + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnonceEmplois that = (AnnonceEmplois) o;
        return Objects.equals(publicationDate, that.publicationDate) &&
               Objects.equals(applicationDeadline, that.applicationDeadline) &&
               Objects.equals(city, that.city) &&
               Objects.equals(contractType, that.contractType) &&
               Objects.equals(degree, that.degree) &&
               Objects.equals(experience, that.experience) &&
               Objects.equals(remoteWork, that.remoteWork) &&
               Objects.equals(sector, that.sector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationDate, applicationDeadline, city, contractType, degree, experience, remoteWork, sector);
    }

}
