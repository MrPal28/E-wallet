package com.example.exception;

public class KycNotFoundException extends ApplicationException{
  public KycNotFoundException(String msg){
    super(msg);
  } 
}