package org.openmrs.module.hivtestingservices.exception;

public class InvalidSettingException extends Exception{
    public InvalidSettingException(){
        super();
    }
    public InvalidSettingException(String message){
        super(message);
    }
}
