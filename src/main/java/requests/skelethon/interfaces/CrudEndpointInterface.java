package requests.skelethon.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get();
    Object get(long id);
    Object update(BaseModel model);
    Object update(long id, BaseModel model);
    Object delete(long id, BaseModel model);

}
