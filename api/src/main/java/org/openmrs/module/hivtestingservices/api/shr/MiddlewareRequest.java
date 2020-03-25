package org.openmrs.module.hivtestingservices.api.shr;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class MiddlewareRequest {

    String patientID;
    String cardSerialNumber;
    String userName;
    String pwd;

    @JsonCreator
    public MiddlewareRequest(@JsonProperty("PATIENTID") String patientID, @JsonProperty("CARD_SERIAL_NO") String cardSerialNumber, @JsonProperty("USERNAME") String userName, @JsonProperty("PASSWORD") String pwd) {
        this.patientID = patientID;
        this.cardSerialNumber = cardSerialNumber;
        this.userName = userName;
        this.pwd = pwd;
    }

    @JsonProperty("PATIENTID")
    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    @JsonProperty("CARD_SERIAL_NO")
    public String getCardSerialNumber() {
        return cardSerialNumber;
    }

    public void setCardSerialNumber(String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    @JsonProperty("USERNAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty("PASSWORD")
    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
