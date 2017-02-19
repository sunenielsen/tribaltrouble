##Apache Tomcat 6
Typical Tomcat installations listen on port 8080.
<br />
This sample Apache Virtual Host configuration [00_registration_vhost.conf](00_registration_vhost.conf) will direct HTTP requests to the Tomcat instance listening on port 8080.
<br />
In addition, the code has been modified to use HTTP instead of HTTPS:
```
tt/classes/com/oddlabs/tt/render/Renderer.java

//return new HttpRequestParameters("https://" + Settings.getSettings().registration_address + "/oddlabs/registration", parameters);
return new HttpRequestParameters("http://" + Settings.getSettings().registration_address + "/oddlabs/registration", parameters);
```

The Registration servlet depends on [DNS](../dns/DNS.md) and [MySQL](../mysql/MYSQL.md)
