package com.example;

public class CommonConstants {
	public static final String USER_CREATION_TOPIC = "user_create";
    public static final String TRANSACTION_CREATE_TOPIC = "txn_create";
    public static final String TRANSACTION_COMPLETION_TOPIC = "txn_complete";
    public static final String WALLET_UPDATED_TOPIC = "wallet_update";
    /**
     * this USER_DELETE_TOPIC used to delete the user from all three service
     * user, transaction, wallet.
     */
    public static final String USER_DELETE_TOPIC = "user_delete_topic"; 
    
    public static final String WALLET_UPDATE_STATUS = "walletUpdateStatus";

    
    /**
     * Below keys are used when we send data from user-service to
     * other services.
     */
    public static final String USER_CREATION_TOPIC_IDENTIFIER_KEY = "userIdentifier";
    public static final String USER_CREATION_TOPIC_IDENTIFIER_VALUE = "identifierValue";
    public static final String USER_CREATION_TOPIC_USERID = "userId";
    public static final String USER_CREATION_TOPIC_PHONE="phone";
    
    
    public static final String TRANSACTION_CREATION_TOPIC_SENDER = "sender";
    public static final String TRANSACTION_CREATION_TOPIC_RECEIVER = "receiver";
    public static final String TRANSACTION_CREATION_TOPIC_AMOUNT = "amount";
    public static final String TRANSACTION_CREATION_TOPIC_PURPOSE = "paymentPurpose";
    public static final String TRANSACTION_CREATION_TOPIC_TRANSACTION_ID = "transactionId";
    
    public static final String USER_DELETE_USERID = "username";
}
