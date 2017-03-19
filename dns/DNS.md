## Domain Name System
Tribal Trouble must resolve specific hosts in order for Match play to work.
<br />
The sample [oddlabs.zone](oddlabs.zone) configuration resolves these specific hosts to the loopback interface.
<br />
One can then test the full capabilities of the game on your **private** machine.
<br />
One can just as easily resolve to a server on your **private** network.
<br />
Then again, one can modify the `_address` definitions in: `tt/classes/com/oddlabs/tt/global/Settings.java`
```
// network
public String registration_address = "registration.oddlabs.com";
public String matchmaking_address = "matchmaking.oddlabs.com";
public String bugreport_address = "bugreport.oddlabs.com";
public String router_address = "router.oddlabs.com";
```
