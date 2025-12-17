package com.wallet.transactionservice.eventDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletDebitSuccessEvent {
    private String referenceId;
}
