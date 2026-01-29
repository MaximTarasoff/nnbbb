package api.requests.skelethon.interfaces;

public interface GetAllEndpointInterface {
    Object getAll(Class<?> clazz);
    Object getAll(Class<?> clazz, long id);
}
