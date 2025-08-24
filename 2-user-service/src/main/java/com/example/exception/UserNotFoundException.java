package com.example.exception;

public class UserNotFoundException extends ApplicationException{
  public UserNotFoundException(String msg){
    super(msg);
  }
}
