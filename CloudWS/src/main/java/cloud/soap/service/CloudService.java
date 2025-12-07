package cloud.soap.service;

import cloud.storage.CloudStorage;

import javax.jws.WebService;

@WebService(endpointInterface = "cloud.soap.service.ICloud")
public class CloudService implements ICloud {

    private CloudStorage storage = new CloudStorage();

    @Override
    public String[] listFiles() {
        return storage.listFiles();
    }

    @Override
    public boolean upload(String filename, byte[] data) {
        return storage.upload(filename, data);
    }

    @Override
    public byte[] download(String filename) {
        return storage.download(filename);
    }

    @Override
    public boolean delete(String filename) {
        return storage.delete(filename);
    }
}
