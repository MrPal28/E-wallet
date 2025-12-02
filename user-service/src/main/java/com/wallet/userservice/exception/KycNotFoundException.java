package com.wallet.userservice.exception;

public class KycNotFoundException extends ApplicationException{
  public KycNotFoundException(String msg){
    super(msg);
  } 
}