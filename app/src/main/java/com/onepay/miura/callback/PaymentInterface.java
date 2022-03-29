package com.onepay.miura.callback;

public interface PaymentInterface
{
   void onClick(String carNumber,String expMonth,String expYear,String cvc,String msg);
   void updateInfo(String str);
}