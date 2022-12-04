package com.nz.stubmpm.webservice;



import com.nz.stubmpm.model.UserResponse;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiService {

    @Headers({
            "Accept:application/json",
            "Content-Type: application/json"
    })



    @POST(ApiConstants.GET_CURRENT_DATETIME)
    Call<UserResponse> getTimeStamp();
/*
    @POST(ApiConstants.PAYNOW)
    Call<UserResponse> paynow(@QueryMap Map<String,String> params);

    @POST(ApiConstants.SEND_REFUND_REQUEST)
    Call<UserResponse> sendRefundRequest(@QueryMap Map<String,String> params);

    @POST(ApiConstants.GET_REFUND_STATUS)
    Call<UserResponse> getRefundStatus(@QueryMap Map<String, String> params);

    @POST(ApiConstants.GET_ORDER_DETAILS)
    Call<UserResponse> getOrderDetails(@QueryMap Map<String, String> params);

    @POST(ApiConstants.GET_PAYMENT_STATUS)
    Call<UserResponse> getPaymentStatus(@QueryMap Map<String, String> params);


    @POST(ApiConstants.GET_TRANSACTION_DETAILS)
    Call<UserResponse> getTransactionDetails(@QueryMap Map<String, String> params);


    @POST(ApiConstants.SEND_PAYMENT_REQUEST)
    Call<UserResponse> sendPrintRequest(@QueryMap Map<String,String> params);

    @POST(ApiConstants.GET_PRINT_STATUS)
    Call<UserResponse> getPrintStatus(@QueryMap Map<String,String> params);

    @Headers( "Content-Type: application/json" )
    @POST(ApiConstants.CREATE_ORDER)
    Call<UserResponse> createOrder(@Body RequestBody body);

    @POST(ApiConstants.AUTH)
    @FormUrlEncoded
    Call<UserResponse> callAuthToken(@FieldMap Map<String,String> params, @Header("Authorization") String authHeader);

    @POST(ApiConstants.CREATE_ORDER)
    @FormUrlEncoded
    Call<UserResponse> callCreateOrder(@FieldMap Map<String,String> params, @Header("Authorization") String authHeader);
*/

  /*  @POST(com.nz.nztravelmate.webservice.ApiConstants.GET_CITY)
    Call<UserResponse> getCityList(@Query("language_id") String language_id);

    @POST(com.nz.nztravelmate.webservice.ApiConstants.SEND_UDID)
    Call<UserResponse> sendUDID(@Query("udid") String udid);*/

    /*@POST(ApiConstants.GET_BUSINESS_COUPON)
    Call<UserResponse> getCouponList(@Query("user_id") String user_id);*/

   /* @POST(ApiConstants.GET_BUSINESS_COUPON)
    @FormUrlEncoded
    Call<UserResponse> getCouponList(@FieldMap Map<String,String> params);
*/
   /* @POST(com.nz.nztravelmate.webservice.ApiConstants.GET_BUSINESS_COUPON)
    Call<UserResponse> getCouponList(@Query("user_id") String user_id, @Query("udid") String udid);

    @POST(com.nz.nztravelmate.webservice.ApiConstants.COLLECT_BUSINESS_COUPON)
    @FormUrlEncoded
    Call<UserResponse> collectBusinessCoupon(@FieldMap Map<String, String> params);

    @POST(com.nz.nztravelmate.webservice.ApiConstants.GET_BANNER)
    Call<UserResponse> getBannerList();

    @POST(com.nz.nztravelmate.webservice.ApiConstants.GET_CATEGORY)
    Call<UserResponse> getCategoryList(@Query("language_id") String language_id);

    @POST(com.nz.nztravelmate.webservice.ApiConstants.GET_BUSINESS_DETAILS)
    @FormUrlEncoded
    Call<UserResponse> getBusinessDetails(@FieldMap Map<String, String> params);
*/

  /*  @POST(ApiConstants.REGISTER_URL)
    Call<UserResponse> registerUser(@Body User user);

    @POST(ApiConstants.LOGIN_URL)
    Call<UserResponse> loginUser(@Body User user);

    @POST(ApiConstants.VIEW_USER)
    Call<UserResponse> viewUser(@Query("userId") String userId);

    @POST(ApiConstants.UPDATE_USER)
    Call<UserResponse> updateUser(@Body User user); //pending-500-Internal Server Error

    @POST(ApiConstants.ADD_CONTACTS)
    Call<UserResponse> addContacts(@Body Contacts contacts);

    @POST(ApiConstants.GET_CONTACTS)
    Call<UserResponse> getContacts(@Query("userId") String userId);

    @POST(ApiConstants.GET_ALARMS)
    Call<UserResponse> getAlarms(@Query("userId") String userId);

    @POST(ApiConstants.PANIC_ALERT)
    Call<UserResponse> sendPanic(@Query("userId") String userId);

    @POST(ApiConstants.panicAlertToEmails)
    Call<UserResponse> sendpanicAlertToEmails(@Body PanicAlert panicAlert);

    @FormUrlEncoded
    @POST(ApiConstants.DELETE_ALARM)
    Call<UserResponse> deleteAlarm(@Field("alarmId[]") ArrayList<Integer> alarmId);//@Query("alarmId") int[] alarmId);

    @POST(ApiConstants.ADD_ALARMS)
    Call<UserResponse> addAlarms(@Body Alarms alarms);

    @POST(ApiConstants.UPDATE_CONTACT)
    Call<UserResponse> updateContact(@Body Contacts contacts);

    @POST(ApiConstants.UPDATE_ALARM)
    Call<UserResponse> updateAlarm(@Body Alarms alarms);

    @FormUrlEncoded
    @POST(ApiConstants.DELETE_CONTACT)
    Call<UserResponse> deleteContact(@Field("contactId[]") ArrayList<Integer> contactId);//@Query("contactId") String contactId);

    @POST(ApiConstants.VIEW_CONTACT)
    Call<UserResponse> viewContact(@Query("contactId") String contact);

    @POST(ApiConstants.SEND_NOTIFICATION)
    Call<UserResponse> sendAlerts(@Body Alert alert);

    @POST(ApiConstants.GET_NOTIFICATION_HISTORY)
    Call<UserResponse> getStartNotifications(@Query("userId") String id);

    @POST(ApiConstants.GET_NOTIFICATION_HISTORY)
    Call<UserResponse> getNextPageNotifications(@Body PagingItem pagingItem);

    @POST(ApiConstants.UPDATE_SETTING)
    Call<UserResponse> updateSetting(@Body Setting settings);

    @POST(ApiConstants.GET_SETTING)
    Call<UserResponse> getSetting(@Query("userId") String id);

    @FormUrlEncoded
    @POST(ApiConstants.DELETE_NOTIFICATION)
    Call<UserResponse> deleteNotification(@Field("notificationId[]") ArrayList<Integer> notificationId);//@Body UserResponse settings);

    @POST(ApiConstants.REQUEST_API)
    Call<UserResponse> approveContact(@Body Contacts contact);

    @POST(ApiConstants.UPDATE_LOCATION)
    Call<UserResponse> updateLocation(@Body LocationUpdate locationUpdate);

    @POST(ApiConstants.ALARM_RESPONSE)
    Call<UserResponse> sendAlarmResponse(@Body AlarmResponse alarmResponse);

    @POST(ApiConstants.FALSE_ALARM_MAIL)
    Call<UserResponse> sendAlarmMails(@Body FalseAlarm user);

    @POST(ApiConstants.FORGOT_PASSCODE)
    Call<UserResponse> forgotPasscode(@Query("userId") String userId);

    @POST(ApiConstants.ENABLE_ALARMS)
    Call<UserResponse> enableAlarms(@Query("userId") String userId);

    @POST(ApiConstants.RESET_PASSWORD)
    Call<UserResponse> resetPassword(@Query("email") String userEmail);

    @POST(ApiConstants.GET_TRUSTEES)
    Call<UserResponse> getTrustees(@Query("userId") String userId);

    @FormUrlEncoded
    @POST(ApiConstants.UPDATE_SUBSCRIPTION)
    Call<UserResponse> updateSubscription(@Field("userId") String userId,
                                          @Field("subscription_end_date") String subscription_end_date,
                                          @Field("is_subscribed") String is_subscribed);*/
}
