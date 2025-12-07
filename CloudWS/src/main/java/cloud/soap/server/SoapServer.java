package cloud.soap.server;

import javax.xml.ws.Endpoint;
import cloud.soap.service.CloudService;

public class SoapServer {

    public static void main(String[] args) {
        String url = "http://localhost:8089/cloud";
        System.out.println("Starting SOAP Server...");
        Endpoint.publish(url, new CloudService());
        System.out.println("SOAP Server running at: " + url + "?wsdl");
    }
}
