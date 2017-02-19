Apache Tomcat 6 is required for the Registration servlet.
<br />
Typical Tomcat installations listen on port 8080.
<br />
The sample Apache Virtual Host configuration [00_registration_vhost.conf](00_registration_vhost.conf) requires:
- mod_proxy
- mod_proxy_http
<br />
<br />
This example will direct HTTP requests to the Tomcat instance.
<br />
In order to use HTTP instead of HTTPS, the code has been modified in:
- tt/classes/com/oddlabs/tt/render/Renderer.java
```
//return new HttpRequestParameters("https://" + Settings.getSettings().registration_address + "/oddlabs/registration", parameters);
return new HttpRequestParameters("http://" + Settings.getSettings().registration_address + "/oddlabs/registration", parameters);
```

The Registration servlet depends on [DNS](../dns/DNS.md) and [MySQL](../mysql/MYSQL.md)
