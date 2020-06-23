# Remember to change mySQL connection properties in the project! (src/main/resoueces -> hibernate.properties)

# Running order and why it's important
You **must** run the server first and then the client. That's because otherwise the client tries to connect to the server, but there is no server to connect to, so you will get an error.

# How to supply arguments while running with Maven?
When configuring the `Maven Build...` target, Add a parameter with the name `exec.args` and the arguments you want as a value.

# Running the client
Simply run the goal ``exec:java@client`` with two arguments: the host and the port.

# Running the server
Run the goal ``exec:java@server`` with one argument: the port to listen to.
