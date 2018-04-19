# General
Basiclly, this project aim to build a blockchain from scratch. Things covered are shown below:
1. Create the core blockchain
2. build APIs around the blockchain
3. Create a dynamic peer-to-peer server for multiple contributors
4. Implement a proof-of-work system to balance users. 

# Blockchain
Distributed and decentralized ledger that stores data like transactions, that is publicly shared across all the nodes of its network. 

## Crytocurrency
It contains three parts, blockchain, mining and wallet. 
### Blockchain
With hash and last hash, we connect each block, and make the connection stable.
### Mining
Add transactions to the blockchain, need to be confirmed, solving a proof of work algorithm. Once one miner solve, broadcast it to others, miner can add this block and other miners will verify. 
Changable difficulty can adjust to control the rate of new bl;ocks coming in. 
### Wallet
Store the private and public key of an individual
Public key is the address of the wallet. 

# Make it run
create package.json file

    npm init -y

Install nodemon, which offers a live development environment

    npm i nodemon --save-dev

Install the cryto-js package for the hash algorithm implementation SHA-256

    npm i crypto-js --save

Build the test environment for the project

    npm i jest --save-dev

To make some APIs, and communicate with HTTP request, we need a module named `express`

    npm i express --save

Parse incoming request bodies in a middleware before your handlers, available under the req.body property.

    npm i body-parser --save

Support the p2p server, need to use `websocket` package

    npm i ws --save
# Some Explanations

## multi-miners
Miners may want to add some blocks to the blockchain at the same time. There are 2 mechanisms to make the judgement.

1. Choose the longest one
2. Check if the data has been changed/tampered

## P2P networking

To truly support multiple miners to run this blockchain application together. Use websocket to do so.  
The first app wiil start the peer to peer server, and the laters will connect to the original one. 