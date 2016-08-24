package sip_stack_v3.netas.com.sip_stack_v3;

import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by sdulger on 10-Mar-15.
 */
public interface PNAdminService {

    @POST("/prov/services/PushNotificationAdminService")
    String subscribe(@Body String body);
}
