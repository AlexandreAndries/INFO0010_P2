# INFO0010_P2
Project - Part 2

Authors :
- Andries Alexandre s196948
- Rotheudt Thomas s191895

Brief :

This project implements a multithreaded server capable of serving multiple
clients simultaneously. Each thread listens for a client connection and
once a connection has been established, it accepts the client's DNS query, parses
it and analyses it for errors. If an error is encountered, the server answers
with an appropriate error response. If no errors are encountered, the server
proceeds to transfer an HTTP request and send back the corresponding response
to the client.

Usage :

Launch server using the following command : "java Server <owned domain name>""
Example : java Server tnl.test

Std output :

Question (CL=<IP of the direct client>, NAME=<domain name to query>, TYPE=<question type>) => <reply code>
