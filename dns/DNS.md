DNS is required for Tribal Trouble Match play.
The sample [oddlabs.zone](oddlabs.zone) file resolves services to the loopback interface. 
One can just as easily resolve to a server on your **private** network.
One can also modify the server definitions in:
> tt/classes/com/oddlabs/tt/global/Settings.java
```
// network
public String registration_address = "registration.oddlabs.com";
public String matchmaking_address = "matchmaking.oddlabs.com";
public String bugreport_address = "bugreport.oddlabs.com";
public String router_address = "router.oddlabs.com";
```
