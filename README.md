# Description about the project

Tech stack: Java, SpringBoot, JPA Hibernate, MySQL, Apache Kafka(for service-to-service communication), Spring Security(for authenticate the Requests).

## API Endpoints:
1. user:
   * POST /user -> will create new user with wallet(initial balance credited to 100)
   * GET /getUser -> get Details of user
   * PUT /update/User -> user can update email and name.
   * PUT /update/UserPassword -> user can update the password.
   * DELETE /delete -> delete the user.
   * GET /transactionHistory -> get the transactionHistory of the user.
   * GET /admin/user/{phoneNo} -> this will return User object based on given username. this api used by other services to get authenticated user details.

2. transaction-service:
   POST /transact: Initate the transaction.
   GET /transaction/history/{username} -> getting the transactionHistory.

3. Wallet-Service:
   GET /wallet/balance -> return user wallet Balance.

## Overview

1. there are 4 services - user, transaction, wallet, notification-service.
   All these services has their own database and running on different ports.

2. all authentication done on user-service. where in each api call user will provide, their credentials in headers. and in
   Backend, we confirms the identity of the user and authenticate.
   How I implemented Service-to-service communication?
   Let's say a user want to get all his transaction_history that stores at txn-service.
   Steps:
   * So, first userDetails will be authenticated at user-service. if user is successfully authenticated then it sets basicAuth
	   in header("txn_service", "txn123") username and password. this depicts that this is a service to service call. then at
	   transaction service we authorize the request using antMatchers(with defined credentials authority) and return the result from database.

3. send money from one wallet to another wallet.
   this request is iniate from transaction-service.
   Steps:
 * we provide three details in requestParam -> receiverId, amount, remarks
 * then we get authenticated user, but authentication logic is written at the side of user-service. So, we do a RestCall to
	   user-service with Authorization header and get loggedin user details.
 * Now, a record is created in txn db - with sender, receiver, amount, payment_purpose, transactionId(generated randomly
	   using UUID), transactionStatus(initially it is pending).
 * Now, we send these details to kafka topic for consuming by other wallet-service to complete the transaction.
 * In wallet-service, Now, possible failure scenarios are:
	   1. receiver is not present at our application or senderBalance<Amount.
	   if above condition is not there, it means it update the walletdb with senderBalance-=amount, receiverBalance+=amount
	   and send these details to transaction-service for update the transaction with Success status else with Failure status.
 * and finally we update the transactionid status with Failure or Success at transaction-service.
