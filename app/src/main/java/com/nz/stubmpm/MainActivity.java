package com.nz.stubmpm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nz.stubmpm.model.UserResponse;
import com.nz.stubmpm.webservice.ApiService;
import com.nz.stubmpm.webservice.RetrofitClient;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {
    Context context=this;
    TextView txtStub;
    Button btnProceed,btnCancel;
    String timestamp="";
    Intent intent;
    String requestFor="";
    String from="";
    String totalAmounts="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();

        intent=getIntent();
         requestFor=intent.getStringExtra("RequestFor");
        from=intent.getStringExtra("From");
         totalAmounts=intent.getStringExtra("totalAmount");
        if (requestFor!=null) {
            requestFor=intent.getStringExtra("RequestFor");
            Toast.makeText(context, "Requested for " + requestFor+ " "+from, Toast.LENGTH_SHORT).show();
            if (!requestFor.equals("")) {
                txtStub.setText("Requested for " + requestFor);
                btnProceed.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                if (from.equalsIgnoreCase("Master")||from.equalsIgnoreCase("Visa")||from.equalsIgnoreCase("EFTPos"))
                {

                        Intent intent=new Intent(MainActivity.this,CardActivity.class);
                        intent.putExtra("From",from);

                        intent.putExtra("Amount",totalAmounts);
                        startActivityForResult(intent,123);
                        finish();
                    }else {
                      //  callTimeStamp();

          }  }

        }

        txtStub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent1=new Intent();
                intent1.putExtra("ResponseFor","TimestampResponse");
                setResult(100,intent1);
                finish();*/
                callTimeStampWs();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response="“functionName”: “settlementInquiry”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
                response=requestFor+" request is canceled from Stub App";
                Intent intent1=new Intent();
                intent1.putExtra("ResponseFor",requestFor);
                intent1.putExtra("message",response);
                setResult(2100,intent1);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (requestFor) {
                    case "TimeStamp":
                        callTimeStampWs();
                        break;

                    case "purchase":
                        if (from.equalsIgnoreCase("EFTPos")||from.equalsIgnoreCase("Visa")||from.equalsIgnoreCase("Master"))
                        {
                           Intent intent=new Intent(MainActivity.this,CardActivity.class);
                           intent.putExtra("From",from);

                           intent.putExtra("Amount",totalAmounts);
                           startActivityForResult(intent,123);
                        }else {
                            callTimeStamp();
                        }
                   /* String functionName=intent.getStringExtra("functionName");
                    String terminalID=intent.getStringExtra("terminalID");
                    String uniqueID=intent.getStringExtra("uniqueID");
                    String posInfo1=intent.getStringExtra("posInfo1");
                    String totalAmount=intent.getStringExtra("totalAmount");
                    Log.v("@RESPONSES",totalAmount+" "+timestamp);
                    sendPurchaseResponse(functionName,terminalID,uniqueID,posInfo1,totalAmount,timestamp);*/
                        break;

                    case "Refund":
                        sendRefundResponse();
                        break;

                    case "PurchasePlusCash":
                        sendPurchasePlusCashResponse();
                        break;

                    case "CashAdvance":
                        sendCashAdvanceResponse();
                        break;

                    case "Void":
                        sendVoidResponse();
                        break;

                    case "Authorise":
                        sendAuthoriseResponse();
                        break;

                    case "Finalise":
                        sendFinaliseResponse();
                        break;

                    case "SettlementInquiry":
                        sendSettlementInquiryResponse();
                        break;

                    case "SettlementCutover":
                        sendSettlementCutover();
                        break;

                    case "TerminalReadCard":
                        sendTerminalReadCard();
                        break;

                    case "ReprintReceipt":
                        sendReprintReceiptResponse();
                        break;

                    case "TerminalStatus":
                        sendTerminalStatusResponse();
                        break;

                    case "Close":
                        //sendPurchaseResponse();
                        break;

                    default:
                        break;
                }

            }
        });
    }

    private void sendPurchaseResponse(String functionName, String terminalID, String uniqueID, String posInfo1, String totalAmount, String timestamp) {
        String transactionId=new Date().getTime() + " ";
        String response="\"functionName:\""+functionName+", \"terminalId\":"+terminalID+", \"uniqueId\": "+uniqueID+", \"transactionId\": \""+transactionId+"\", \"transactionTimeStamp\": \""+timestamp+"\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": "+totalAmount+", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \""+cardName+"\", \"CardType\": \""+cardType+"\", \"AccountType\": \""+cardAcType+"\", \"CardExpiry\": \""+expiryDate+"\", \"Timestamp\": \""+timestamp+"\", \"RequestTimestamp\": \""+timestamp+"\", \"ResponseTimestamp\": \""+timestamp+"\"\n";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","purchase");
        intent1.putExtra("message",response);

        Log.v("@RESPONSES",response);
        setResult(200,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendPurchasePlusCashResponse() {
        String response="“functionName”: “purchase”, “PurchasePlusCash”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
      //  response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","PurchasePlusCash");
        intent1.putExtra("message",response);
        setResult(300,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendRefundResponse() {
        String response="“functionName”: “refund”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
        //response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","Refund");
        intent1.putExtra("message",response);
        setResult(1400,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendCashAdvanceResponse() {
        String response="“functionName”: “cashAdvance”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
       // response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","CashAdvance");
        intent1.putExtra("message",response);
        setResult(400,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendVoidResponse() {
        String response="“functionName”: “void”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
        //response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","Void");
        intent1.putExtra("message",response);
        setResult(500,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendAuthoriseResponse() {
        String response="“functionName”: “authorise”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
      //  response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","Authorise");
        intent1.putExtra("message",response);
        setResult(600,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendFinaliseResponse() {
        String response="“functionName”: “finalise”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
       // response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","Finalise");
        intent1.putExtra("message",response);
        setResult(700,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendSettlementInquiryResponse() {
        String response="“functionName”: “settlementInquiry”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
      //  response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","SettlementInquiry");
        intent1.putExtra("message",response);
        setResult(800,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendSettlementCutover() {
        String response="“functionName”: “SettlementCutover”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
      //  response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","SettlementCutover");
        intent1.putExtra("message",response);
        setResult(900,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendTerminalReadCard() {
        String response="“functionName”: “TerminalReadCard”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
      //  response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","TerminalReadCard");
        intent1.putExtra("message",response);
        setResult(1000,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendReprintReceiptResponse() {
        String response="“functionName”: “ReprintReceipt”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
      //  response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","ReprintReceipt");
        intent1.putExtra("message",response);
        setResult(1100,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void sendTerminalStatusResponse() {
        String response="“functionName”: “TerminalStatus”, “terminalID”: “what was sent is returned”, “uniqueID”: “what was sent is returned”, \"transactionId\": \"f363c7de-102c-4d80-a902- ed37413ca599\", \"transactionTimeStamp\": \"201809182353193193\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": \"COMPLETED\", \"AmountTotal\": \"615\", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"....0138\", \"CardType\": \"TEST CARD\", \"AccountType\": \"CREDIT\", \"Timestamp\": \"20180919115520\", \"RequestTimestamp\": \"201809182353196949\", \"ResponseTimestamp\": \"201809182355302736\"\n";
      //  response="Under Implementation";
        Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","TerminalStatus");
        intent1.putExtra("message",response);
        setResult(1200,intent1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }


    private void initUi() {
        txtStub=findViewById(R.id.txtStub);
        btnProceed=findViewById(R.id.btnProceed);
        btnCancel=findViewById(R.id.btnCancel);
    }

    private void callTimeStampWs() {
        openProgressDialog();
        ApiService apiService = RetrofitClient.getClient(context).create(ApiService.class);
        Call<UserResponse> call = apiService.getTimeStamp();
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                try {
                    Log.v("@RESP_TOKEN", "RespPAY: " + response);
                    // Log.v("@RESP", "RespCOupon: " + response.body().getCoupon().size());
                  UserResponse userResponse = response.body();
                    if (userResponse != null) {
                        Log.v("@RESP_TOKEN", "ResPAY: " + new Gson().toJson(response.body()));
                       // Toast.makeText(context, "" + userResponse.getMessage(), LENGTH_SHORT).show();
                        String message = userResponse.getMessage();

                        if ((progressDialog != null) && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        Intent intent1=new Intent();
                        intent1.putExtra("ResponseFor","TimestampResponse");
                        intent1.putExtra("message",userResponse.getMessage());
                        intent1.putExtra("time",userResponse.getTime());
                        intent1.putExtra("time_zone",userResponse.getTime_zone());
                        setResult(100,intent1);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.v("@RESP_TOKEN", "RespCityException: " + ex.getLocalizedMessage());
                    Toast.makeText(context, ex.getLocalizedMessage(), LENGTH_SHORT).show();
                    try {
                        if ((progressDialog != null) && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    } catch (final IllegalArgumentException e) {
                        // Handle or log or ignore
                    } catch (final Exception e) {
                        // Handle or log or ignore
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.v("@RESP", "RespCityFail: " + t.getLocalizedMessage());
                Toast.makeText(context, t.getLocalizedMessage(), LENGTH_SHORT).show();
                try {
                    if ((progressDialog != null) && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
            }
        });

    }


    private void callTimeStamp() {
        openProgressDialog();
        ApiService apiService = RetrofitClient.getClient(context).create(ApiService.class);
        Call<UserResponse> call = apiService.getTimeStamp();
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                try {
                    Log.v("@RESP_TOKEN", "RespPAY: " + response);
                    // Log.v("@RESP", "RespCOupon: " + response.body().getCoupon().size());
                    UserResponse userResponse = response.body();
                    if (userResponse != null) {
                        Log.v("@RESP_TOKEN", "ResPAY: " + new Gson().toJson(response.body()));
                        // Toast.makeText(context, "" + userResponse.getMessage(), LENGTH_SHORT).show();
                        String message = userResponse.getMessage();

                        if ((progressDialog != null) && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        timestamp=userResponse.getTime();
                        String functionName=intent.getStringExtra("functionName");
                        String terminalID=intent.getStringExtra("terminalID");
                        String uniqueID=intent.getStringExtra("uniqueID");
                        String posInfo1=intent.getStringExtra("posInfo1");
                        String totalAmount=intent.getStringExtra("totalAmount");
                        Log.v("@RESPONSES",totalAmount+" "+timestamp);
                        sendPurchaseResponse(functionName,terminalID,uniqueID,posInfo1,totalAmount,timestamp);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.v("@RESP_TOKEN", "RespCityException: " + ex.getLocalizedMessage());
                    Toast.makeText(context, ex.getLocalizedMessage(), LENGTH_SHORT).show();
                    try {
                        if ((progressDialog != null) && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    } catch (final IllegalArgumentException e) {
                        // Handle or log or ignore
                    } catch (final Exception e) {
                        // Handle or log or ignore
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.v("@RESP", "RespCityFail: " + t.getLocalizedMessage());
                Toast.makeText(context, t.getLocalizedMessage(), LENGTH_SHORT).show();
                try {
                    if ((progressDialog != null) && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } catch (final IllegalArgumentException e) {
                    // Handle or log or ignore
                } catch (final Exception e) {
                    // Handle or log or ignore
                }
            }
        });

    }

    ProgressDialog progressDialog;

    public void openProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.......");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // closeTimer();
               // callAuthToken("TRANSACTION_DETAILS");
                progressDialog.dismiss();//dismiss dialog

            }
        });
        progressDialog.show();
    }

    String cardName="";
    String cardType="";
    String cardAcType="";
    String expiryDate="";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null)
        {

           cardName=data.getExtras().getString("cardNum");
            String Card= data.getExtras().getString("cardType");
            String cards[]=Card.split("-");
            cardType=cards[0];
            cardAcType=cards[1];
            expiryDate=data.getExtras().getString("cardExpiryDate");
            Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
            callTimeStamp();
        }

    }
}