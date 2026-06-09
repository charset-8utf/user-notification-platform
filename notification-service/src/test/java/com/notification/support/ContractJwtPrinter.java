package com.notification.support;

/** Prints {@link ServiceJwtTestSupport#CONTRACT_AUTHORIZATION} for pasting into contract Groovy. */
public final class ContractJwtPrinter {

    private ContractJwtPrinter() {
    }

    public static void main(String[] args) {
        System.out.print(ServiceJwtTestSupport.CONTRACT_AUTHORIZATION);
    }
}
