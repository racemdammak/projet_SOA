package cloud.soap.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface ICloud {

    @WebMethod
    String[] listFiles();

    @WebMethod
    boolean upload(String filename, byte[] data);

    @WebMethod
    byte[] download(String filename);

    @WebMethod
    boolean delete(String filename);

}
