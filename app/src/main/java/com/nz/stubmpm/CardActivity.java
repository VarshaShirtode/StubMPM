package com.nz.stubmpm;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.nz.stubmpm.model.UserResponse;
import com.nz.stubmpm.webservice.ApiService;
import com.nz.stubmpm.webservice.RetrofitClient;
import com.pos.sdk.DeviceManager;
import com.pos.sdk.DevicesFactory;
import com.pos.sdk.callback.ResultCallback;
import com.pos.sdk.magcard.IMagCardListener;
import com.pos.sdk.magcard.MagCardDevice;
import com.pos.sdk.magcard.TrackData;
import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask;
import com.pro100svitlo.creditCardNfcReader.utils.CardNfcUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class CardActivity extends AppCompatActivity implements CardNfcAsyncTask.CardNfcInterface {
    Context context = this;

    // private Toolbar mToolbar;
    private LinearLayout mCardReadyContent;
    private TextView mPutCardContent;
    private LinearLayout mPutCard_Right, mPutCard_Down;
    LinearLayout mPutCard_Up, mEftpos;
    private TextView mCardNumberText;
    private TextView mExpireDateText, mCardType;
    private ImageView mCardLogoIcon;
    private CardNfcAsyncTask mCardNfcAsyncTask;
    private NfcAdapter mNfcAdapter;
    private AlertDialog mTurnNfcDialog;
    private ProgressDialog mProgressDialog;
    private String mDoNotMoveCardMessage;
    private String mUnknownEmvCardMessage;
    private String mCardWithLockedNfcMessage;
    private boolean mIsScanNow;
    private boolean mIntentFromCreate;
    private CardNfcUtils mCardNfcUtils;
    LinearLayout llCardPayment, content_eftpos;
    EditText etText;
    public String amount = "0.00";
    String rate = "0.00";
    Button btnCardDebit, btnBankDebit;
    Button btnTest;
    TextView btnSaving, btnCheque, btnCancel, content_amount_value;
    String from = "";
    boolean status = false;
    String timestamp = "";
    Intent intent;
    String requestFor = "";
    // String from="";
    String totalAmounts = "";
    public MagCardDevice mMagCardDevice;
    TextView txtSwipe;
TextView version;
String directNFC="No";
String card="",expiry="",cardType="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
       /* mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);*/
        version = (TextView) findViewById(R.id.version);
        DevicesFactory.create(this, new ResultCallback<DeviceManager>() {
            @Override
            public void onFinish(DeviceManager deviceManager) {
                mMagCardDevice = deviceManager.getMagneticDevice();
                onSwipeCard();
                showNormalMessage("Created");
            }

            @Override
            public void onError(int i, String s) {
            }
        });
        intent = getIntent();
        from = "blank";
        if (intent != null) {
            amount = intent.getStringExtra("totalAmount");
            Log.v("NFCCARD","Amount "+amount);
            if (intent.hasExtra("From")) {
                from = intent.getStringExtra("From");
                Log.v("NFCCARD","From "+from);
                if (from.equalsIgnoreCase("EFTPos")) {

                } else {
                    mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
                }
            }
            // requestFor=intent.getStringExtra("RequestFor");
            totalAmounts = intent.getStringExtra("totalAmount");
            if (requestFor != null) {
                requestFor = intent.getStringExtra("RequestFor");
                // Toast.makeText(context, "Requested for " + requestFor + " " + from, Toast.LENGTH_SHORT).show();
            }
            if(intent.hasExtra("DIRECT_NFC")) {
                directNFC = intent.getStringExtra("DIRECT_NFC");
                card=intent.getStringExtra("Card");
                expiry=intent.getStringExtra("Expiry");
                cardType=intent.getStringExtra("Cardtype");
                Log.v("NFCCARD","inside hasEXtre "+card+" "+expiry+" "+cardType);
            }

        }
        // amount="70.00";


        if (mNfcAdapter == null) {
            TextView noNfc = (TextView) findViewById(android.R.id.candidatesArea);
            noNfc.setVisibility(View.VISIBLE);
        } else {
            mCardNfcUtils = new CardNfcUtils(this);
            mCardLogoIcon = (ImageView) findViewById(android.R.id.icon);

            createProgressDialog();
            initNfcMessages();
            mIntentFromCreate = true;
            onNewIntent(getIntent());
        }
        mPutCardContent = (TextView) findViewById(R.id.content_putCard);
        mPutCard_Up = (LinearLayout) findViewById(R.id.content_up);
        mEftpos = (LinearLayout) findViewById(R.id.content_eftpos);
        content_amount_value = (TextView) findViewById(R.id.content_amount_value);
        if (amount != null) {
            content_amount_value.setText("" + currencyFormat(amount));
        } else {
            content_amount_value.setText("" + currencyFormat("0.00"));
        }
        mPutCard_Down = (LinearLayout) findViewById(R.id.content_down);
        mPutCard_Right = (LinearLayout) findViewById(R.id.content_right);
        mPutCard_Right = (LinearLayout) findViewById(R.id.content_right);
        mCardReadyContent = (LinearLayout) findViewById(R.id.content_cardReady);
        llCardPayment = findViewById(R.id.llCardPayment);
        mCardNumberText = (TextView) findViewById(android.R.id.text1);
        mExpireDateText = (TextView) findViewById(android.R.id.text2);
        etText = findViewById(R.id.etText);
        btnTest = findViewById(R.id.btnTest);
        btnCheque = findViewById(R.id.btnCheque);
        btnSaving = findViewById(R.id.btnSaving);
        btnCancel = findViewById(R.id.btnCancel);
        mCardType = (TextView) findViewById(R.id.text3);
        btnBankDebit = findViewById(R.id.btnBankDebit);
        btnCardDebit = findViewById(R.id.btnCardDebit);
        txtSwipe = findViewById(R.id.txtSwipe);
        if (from != null && from.equalsIgnoreCase("EFTPos")) {

        }

        if (directNFC.equals("Yes"))
        {
                if (amount == null) {
                    Log.v("RESPOS", "amount is null");
                }
                Double amt = Double.parseDouble(amount);
                if (amt >= 80.00) {
                    String card = "4568963216986325";
                    card = getPrettyCardNumber(card);
                    Log.v("RESPOS", "Resp  " + card);
                    String expiredDate = "10/23";
                    String cardType = "Credit";
                    mCardNumberText.setText(card);
                    mExpireDateText.setText(expiredDate);
                    setTypeName(card);
                    parseCardType(cardType);
                    calculateAmountToPay(rate, mCardType.getText().toString(), amount);
                    cardName = mCardNumberText.getText().toString();
                    String Card = mCardType.getText().toString();
                    String cards[] = Card.split("-");

                    if (cards.length == 2) {
                        cardCBType = cards[0];
                        cardAcType = cards[1];
                    }
                    expiryDate = mExpireDateText.getText().toString();
                    //  Toast.makeText(CardActivity.this,"Amount is greater than 80.00",Toast.LENGTH_SHORT).show();
                    if (from != null && from.equalsIgnoreCase("EFTPos")) {
                        mPutCardContent.setVisibility(View.GONE);
                        mPutCard_Up.setVisibility(View.GONE);
                        mEftpos.setVisibility(View.VISIBLE);
                        mCardReadyContent.setVisibility(View.GONE);
                        llCardPayment.setVisibility(View.GONE);

                        btnCheque.setVisibility(View.VISIBLE);
                        btnSaving.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);

                           /* String card ="4568963216986325";
                            card = getPrettyCardNumber(card);
                            Log.v("RESPOS","Resp  "+card);
                            String expiredDate = "10/23";
                            String cardType = "Credit";
                            mCardNumberText.setText(card);
                            mExpireDateText.setText(expiredDate);
                            setTypeName(card);
                            parseCardType(cardType);
                            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
                            cardName=mCardNumberText.getText().toString();
                            String Card= mCardType.getText().toString();
                            String cards[]=Card.split("-");

                            if (cards.length==2) {
                                cardCBType = cards[0];
                                cardAcType = cards[1];
                            }
                            expiryDate=mExpireDateText.getText().toString();*/
                    } else {
                        callKeyboardDialog();
                    }
                } else {
                    //   Toast.makeText(CardActivity.this,"Amount is Less than 80.00",Toast.LENGTH_SHORT).show();
                       /* mPutCardContent.setVisibility(View.GONE);
                        mPutCard_Up.setVisibility(View.GONE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);
                        btnCheque.setVisibility(View.GONE);
                        btnSaving.setVisibility(View.GONE);

                        mCardReadyContent.setVisibility(View.GONE);
                        llCardPayment.setVisibility(View.GONE);
                        setTypeName(etText.getText().toString());
                        calculateAmountToPay(rate,mCardType.getText().toString(),amount);

                        String Card= mCardType.getText().toString();
                        String cards[]=Card.split("-");
                        Log.v("RESPOS","Res "+Card);
                            if (cards.length==2) {
                                cardCBType = cards[0];
                                cardAcType = cards[1];
                            }
                            cardName=mCardNumberText.getText().toString();
                            expiryDate=mExpireDateText.getText().toString();
                            //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                            status=true;
                            callTimeStamp();*/

                    status = true;
                    mPutCardContent.setVisibility(View.GONE);
                    mPutCard_Up.setVisibility(View.GONE);
                    mEftpos.setVisibility(View.GONE);
                    mCardReadyContent.setVisibility(View.GONE);
                    llCardPayment.setVisibility(View.GONE);

                    if (from != null && from.equalsIgnoreCase("EFTPos")) {
                        btnCheque.setVisibility(View.VISIBLE);
                        btnSaving.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);

                            /*String card ="4568963216986325";
                            card = getPrettyCardNumber(card);
                            Log.v("RESPOS","Resp  "+card);
                            String expiredDate = "10/23";
                            String cardType = "Credit";
                            mCardNumberText.setText(card);
                            mExpireDateText.setText(expiredDate);
                            setTypeName(card);
                            parseCardType(cardType);
                            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
                            cardName=mCardNumberText.getText().toString();
                            String Card= mCardType.getText().toString();
                            String cards[]=Card.split("-");

                            if (cards.length==2) {
                                cardCBType = cards[0];
                                cardAcType = cards[1];
                            }
                            expiryDate=mExpireDateText.getText().toString();*/
                    } else {
                        btnCheque.setVisibility(View.GONE);
                        btnSaving.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);

                        String card = "4568963216986325";
                        card = getPrettyCardNumber(card);
                        Log.v("RESPOS", "Resp  " + card);
                        String expiredDate = "10/23";
                        String cardType = "Credit";
                        mCardNumberText.setText(card);
                        mExpireDateText.setText(expiredDate);
                        setTypeName(card);
                        parseCardType(cardType);
                        calculateAmountToPay(rate, mCardType.getText().toString(), amount);
                        cardName = mCardNumberText.getText().toString();
                        Log.v("RESPOS", "Resp  " + card);
                        String Card = mCardType.getText().toString();
                        String cards[] = Card.split("-");

                        if (cards.length == 2) {
                            cardCBType = cards[0];
                            cardAcType = cards[1];
                        }
                        expiryDate = mExpireDateText.getText().toString();
                        //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                        status = true;
                        callTimeStamp();
                    }


                }



        }else{

        }



        txtSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CardActivity.this, SwipeActivity.class);
                startActivity(i);
            }
        });

        mPutCard_Right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeCard();
            }
        });


        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager nm = ( NotificationManager ) getSystemService(
                        NOTIFICATION_SERVICE );
                Notification notif = new Notification();
                notif.ledARGB = 0xFFff0000;
                notif.flags = Notification.FLAG_SHOW_LIGHTS;
                notif.ledOnMS = 100;
                notif.ledOffMS = 100;
                nm.notify(12345, notif);
// Program the end of the light :
               // mCleanLedHandler.postDelayed(mClearLED_Task, LED_TIME_ON );
            }/*{
                if (amount == null) {
                    Log.v("RESPOS", "amount is null");
                }
                Double amt = Double.parseDouble(amount);
                if (amt >= 80.00) {
                    String card = "4568963216986325";
                    card = getPrettyCardNumber(card);
                    Log.v("RESPOS", "Resp  " + card);
                    String expiredDate = "10/23";
                    String cardType = "Credit";
                    mCardNumberText.setText(card);
                    mExpireDateText.setText(expiredDate);
                    setTypeName(card);
                    parseCardType(cardType);
                    calculateAmountToPay(rate, mCardType.getText().toString(), amount);
                    cardName = mCardNumberText.getText().toString();
                    String Card = mCardType.getText().toString();
                    String cards[] = Card.split("-");

                    if (cards.length == 2) {
                        cardCBType = cards[0];
                        cardAcType = cards[1];
                    }
                    expiryDate = mExpireDateText.getText().toString();
                    //  Toast.makeText(CardActivity.this,"Amount is greater than 80.00",Toast.LENGTH_SHORT).show();
                    if (from != null && from.equalsIgnoreCase("EFTPos")) {
                        mPutCardContent.setVisibility(View.GONE);
                        mPutCard_Up.setVisibility(View.GONE);
                        mEftpos.setVisibility(View.VISIBLE);
                        mCardReadyContent.setVisibility(View.GONE);
                        llCardPayment.setVisibility(View.GONE);

                        btnCheque.setVisibility(View.VISIBLE);
                        btnSaving.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);

                           *//* String card ="4568963216986325";
                            card = getPrettyCardNumber(card);
                            Log.v("RESPOS","Resp  "+card);
                            String expiredDate = "10/23";
                            String cardType = "Credit";
                            mCardNumberText.setText(card);
                            mExpireDateText.setText(expiredDate);
                            setTypeName(card);
                            parseCardType(cardType);
                            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
                            cardName=mCardNumberText.getText().toString();
                            String Card= mCardType.getText().toString();
                            String cards[]=Card.split("-");

                            if (cards.length==2) {
                                cardCBType = cards[0];
                                cardAcType = cards[1];
                            }
                            expiryDate=mExpireDateText.getText().toString();*//*
                    } else {
                        callKeyboardDialog();
                    }
                } else {
                    //   Toast.makeText(CardActivity.this,"Amount is Less than 80.00",Toast.LENGTH_SHORT).show();
                       *//* mPutCardContent.setVisibility(View.GONE);
                        mPutCard_Up.setVisibility(View.GONE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);
                        btnCheque.setVisibility(View.GONE);
                        btnSaving.setVisibility(View.GONE);

                        mCardReadyContent.setVisibility(View.GONE);
                        llCardPayment.setVisibility(View.GONE);
                        setTypeName(etText.getText().toString());
                        calculateAmountToPay(rate,mCardType.getText().toString(),amount);

                        String Card= mCardType.getText().toString();
                        String cards[]=Card.split("-");
                        Log.v("RESPOS","Res "+Card);
                            if (cards.length==2) {
                                cardCBType = cards[0];
                                cardAcType = cards[1];
                            }
                            cardName=mCardNumberText.getText().toString();
                            expiryDate=mExpireDateText.getText().toString();
                            //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                            status=true;
                            callTimeStamp();*//*

                    status = true;
                    mPutCardContent.setVisibility(View.GONE);
                    mPutCard_Up.setVisibility(View.GONE);
                    mEftpos.setVisibility(View.GONE);
                    mCardReadyContent.setVisibility(View.GONE);
                    llCardPayment.setVisibility(View.GONE);

                    if (from != null && from.equalsIgnoreCase("EFTPos")) {
                        btnCheque.setVisibility(View.VISIBLE);
                        btnSaving.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);

                            *//*String card ="4568963216986325";
                            card = getPrettyCardNumber(card);
                            Log.v("RESPOS","Resp  "+card);
                            String expiredDate = "10/23";
                            String cardType = "Credit";
                            mCardNumberText.setText(card);
                            mExpireDateText.setText(expiredDate);
                            setTypeName(card);
                            parseCardType(cardType);
                            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
                            cardName=mCardNumberText.getText().toString();
                            String Card= mCardType.getText().toString();
                            String cards[]=Card.split("-");

                            if (cards.length==2) {
                                cardCBType = cards[0];
                                cardAcType = cards[1];
                            }
                            expiryDate=mExpireDateText.getText().toString();*//*
                    } else {
                        btnCheque.setVisibility(View.GONE);
                        btnSaving.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);

                        String card = "4568963216986325";
                        card = getPrettyCardNumber(card);
                        Log.v("RESPOS", "Resp  " + card);
                        String expiredDate = "10/23";
                        String cardType = "Credit";
                        mCardNumberText.setText(card);
                        mExpireDateText.setText(expiredDate);
                        setTypeName(card);
                        parseCardType(cardType);
                        calculateAmountToPay(rate, mCardType.getText().toString(), amount);
                        cardName = mCardNumberText.getText().toString();
                        Log.v("RESPOS", "Resp  " + card);
                        String Card = mCardType.getText().toString();
                        String cards[] = Card.split("-");

                        if (cards.length == 2) {
                            cardCBType = cards[0];
                            cardAcType = cards[1];
                        }
                        expiryDate = mExpireDateText.getText().toString();
                        //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                        status = true;
                        callTimeStamp();
                    }


                }


            }*/
        });

        btnSaving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callKeyboardDialog();
            }
        });
        btnCheque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callKeyboardDialog();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = false;
                callTimeStamp();
            }
        });




        btnCardDebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent=new Intent();
                intent.putExtra("cardNum",mCardNumberText.getText().toString());
                intent.putExtra("cardType",mCardType.getText().toString());
                intent.putExtra("cardExpiryDate",mExpireDateText.getText().toString());
                setResult(123,intent);
                finish();*/
                cardName=mCardNumberText.getText().toString();
                String Card= mCardType.getText().toString();
                String cards[]=Card.split("-");
                Log.v("RESPOS","Res "+Card);

                    if (cards.length==2) {
                        cardCBType = cards[0];
                        cardAcType = cards[1];
                    }
                    expiryDate=mExpireDateText.getText().toString();
                    //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                    status=true;
                    callTimeStamp();


            }
        });

        btnBankDebit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent=new Intent();
                intent.putExtra("cardNum",mCardNumberText.getText().toString());
                intent.putExtra("cardType",mCardType.getText().toString());
                intent.putExtra("cardExpiryDate",mExpireDateText.getText().toString());
                setResult(123,intent);
                finish();*/
                cardName=mCardNumberText.getText().toString();
                String Card= mCardType.getText().toString();
                String cards[]=Card.split("-");

                    if (cards.length==2) {
                        cardCBType = cards[0];
                        cardAcType = cards[1];
                    }
                    expiryDate=mExpireDateText.getText().toString();
                    //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                    status=true;
                    callTimeStamp();


            }
        });
    }

    private void calculateAmountToPay(String rate, String cardType, String amount) {
        double charge=Double.parseDouble(rate);
        double per= (Double.parseDouble(amount)/100)*charge;
        double totalAmount=per+Double.parseDouble(amount);
        llCardPayment.setVisibility(View.GONE);

        /*btnCardDebit.setText(cardType+" Payment "+currencyFormat(""+totalAmount));
        if (cardType.endsWith("Debit")) {
            btnBankDebit.setText("Bank Debit Payment " + currencyFormat("" + amount));
            btnBankDebit.setVisibility(View.VISIBLE);
        }else{
            btnBankDebit.setVisibility(View.GONE);
        }*/
//New changed calculation
        btnCardDebit.setText(cardType+" Payment "+currencyFormat(""+amount));
        if (cardType.endsWith("Debit")) {
            btnBankDebit.setText("Bank Debit Payment " + currencyFormat("" + amount));
            btnBankDebit.setVisibility(View.VISIBLE);
        }else{
            btnBankDebit.setVisibility(View.GONE);
        }
    }
    private String currencyFormat(String grandTotal) {
        double number = Double.parseDouble(grandTotal);
        String COUNTRY = "US";
        String LANGUAGE = "en";
        String str = NumberFormat.getCurrencyInstance(new Locale(LANGUAGE, COUNTRY)).format(number);
        return str;
    }

    //Custom Keyboard to enter amount to pay
    public void callKeyboardDialog() {
        final Dialog dialog = new Dialog(CardActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater lf = (LayoutInflater) (CardActivity.this)
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogview = lf.inflate(R.layout.keyboard_layout, null);
        dialog.setContentView(dialogview);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        EditText edt_amount_key = (EditText) dialogview.findViewById(R.id.edt_amount);
        ImageView imageview_circle1=(ImageView) dialogview.findViewById(R.id.imageview_circle1);
        ImageView imageview_circle2=(ImageView) dialogview.findViewById(R.id.imageview_circle2);
        ImageView imageview_circle3=(ImageView) dialogview.findViewById(R.id.imageview_circle3);
        ImageView imageview_circle4=(ImageView) dialogview.findViewById(R.id.imageview_circle4);


        com.nz.stubmpm.MyKeyboard keyboard = (com.nz.stubmpm.MyKeyboard) dialogview.findViewById(R.id.keyboard);

        // prevent system keyboard from appearing when EditText is tapped
        edt_amount_key.setRawInputType(InputType.TYPE_CLASS_TEXT);
        edt_amount_key.setTextIsSelectable(true);
        // pass the InputConnection from the EditText to the keyboard
        InputConnection ic = edt_amount_key.onCreateInputConnection(new EditorInfo());
        keyboard.setInputConnection(ic);

        edt_amount_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               // String s  = ss.toString();
                if (s.length() == 1) {
                    imageview_circle1.setVisibility(View.VISIBLE);
                    imageview_circle2.setVisibility(View.GONE);
                    imageview_circle3.setVisibility(View.GONE);
                    imageview_circle4.setVisibility(View.GONE);
                } else if (s.length() == 2)
                {
                    imageview_circle1.setVisibility(View.VISIBLE);
                    imageview_circle2.setVisibility(View.VISIBLE);
                    imageview_circle3.setVisibility(View.GONE);
                    imageview_circle4.setVisibility(View.GONE);
                }
                else if (s.length() == 3)
                {
                    imageview_circle1.setVisibility(View.VISIBLE);
                    imageview_circle2.setVisibility(View.VISIBLE);
                    imageview_circle3.setVisibility(View.VISIBLE);
                    imageview_circle4.setVisibility(View.GONE);
                }
                else if (s.length() == 4)
                {
                    imageview_circle1.setVisibility(View.VISIBLE);
                    imageview_circle2.setVisibility(View.VISIBLE);
                    imageview_circle3.setVisibility(View.VISIBLE);
                    imageview_circle4.setVisibility(View.VISIBLE);
                }
                else if (s.length() == 0)
                {
                    imageview_circle1.setVisibility(View.GONE);
                    imageview_circle2.setVisibility(View.GONE);
                    imageview_circle3.setVisibility(View.GONE);
                    imageview_circle4.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable ss) {

            }
        });



        TextView mButtonClose = (TextView) dialogview.findViewById(R.id.button_close);
        mButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status=false;
                callTimeStamp();
                Toast.makeText(CardActivity.this,"Transaction is closed",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        keyboard.mButtonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(CardActivity.this,""+edt_amount_key.getText().toString(),Toast.LENGTH_SHORT).show();
               String pin= edt_amount_key.getText().toString().trim();
                 if (pin.length()<4) {
                     status=false;
                     callTimeStamp();
                     Toast.makeText(CardActivity.this,"Incorrect PIN",Toast.LENGTH_SHORT).show();
                 }
                 else{
                         status = true;
                         mPutCardContent.setVisibility(View.GONE);
                         mPutCard_Up.setVisibility(View.GONE);
                         mEftpos.setVisibility(View.GONE);
                         mPutCard_Down.setVisibility(View.GONE);
                         mPutCard_Right.setVisibility(View.GONE);
                         btnCheque.setVisibility(View.GONE);
                         btnSaving.setVisibility(View.GONE);
                         btnCancel.setVisibility(View.GONE);
                         mCardReadyContent.setVisibility(View.GONE);
                         llCardPayment.setVisibility(View.GONE);

                         /*String card = mCardNfcAsyncTask.getCardNumber();
                         //card ="4568963216986325";
                         card = getPrettyCardNumber(card);
                         Log.v("RESPOS","Resp  "+card);
                         String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
                         String cardType = mCardNfcAsyncTask.getCardType();
                         mCardNumberText.setText(card);
                         mExpireDateText.setText(expiredDate);
                         setTypeName(card);
                         parseCardType(cardType);
                         calculateAmountToPay(rate,mCardType.getText().toString(),amount);
                         cardName=mCardNumberText.getText().toString();
                         String Card= mCardType.getText().toString();
                         String cards[]=Card.split("-");

                         if (cards.length==2) {
                             cardCBType = cards[0];
                             cardAcType = cards[1];
                         }
                         expiryDate=mExpireDateText.getText().toString();*/
                         //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                         status=true;
                         callTimeStamp();
                 }
                dialog.dismiss();
                /* if (!edt_amount_key.getText().toString().equals("")) {

                    keyboard_amount = edt_amount_key.getText().toString();
                } else {
                    keyboard_amount = "0.00";
                }
                edt_amount.setText(keyboard_amount);
                callGetExchangeRate();*/
               // dialog.dismiss();
            }
        });

        dialog.getWindow().setAttributes(lp);
        dialog.show();

    }



    @Override
    protected void onResume() {
        super.onResume();
        try {
            version.setText(getResources().getString(R.string.stub_Version) + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mIntentFromCreate = false;

        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()){
            showTurnOnNfcDialog();
            mPutCardContent.setVisibility(View.GONE);
            mPutCard_Up.setVisibility(View.GONE);
            mEftpos.setVisibility(View.GONE);
            mPutCard_Down.setVisibility(View.GONE);
            mPutCard_Right.setVisibility(View.GONE);
            btnCheque.setVisibility(View.GONE);
            btnSaving.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        } else if (mNfcAdapter != null){
            if (!mIsScanNow){
                mPutCardContent.setVisibility(View.GONE);
                if (from!=null&&from.equalsIgnoreCase("EFTPos"))
                {
                    mPutCard_Up.setVisibility(View.GONE);
                    mEftpos.setVisibility(View.VISIBLE);
                    /*btnCheque.setVisibility(View.VISIBLE);
                    btnSaving.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);*/
                    btnCheque.setVisibility(View.GONE);
                    btnSaving.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                }else{
                    mPutCard_Up.setVisibility(View.VISIBLE);
                    mEftpos.setVisibility(View.GONE);
                    btnCheque.setVisibility(View.GONE);
                    btnSaving.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.GONE);
                }

                mPutCard_Down.setVisibility(View.VISIBLE);
                mPutCard_Right.setVisibility(View.VISIBLE);
                mCardReadyContent.setVisibility(View.GONE);
                llCardPayment.setVisibility(View.GONE);
            }
            mCardNfcUtils.enableDispatch();

        }else {
            mPutCardContent.setVisibility(View.GONE);
            if (from!=null&&from.equalsIgnoreCase("EFTPos"))
            {
                mPutCard_Up.setVisibility(View.GONE);
                mEftpos.setVisibility(View.VISIBLE);
                    /*btnCheque.setVisibility(View.VISIBLE);
                    btnSaving.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);*/
                btnCheque.setVisibility(View.GONE);
                btnSaving.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
            }else{
                mPutCard_Up.setVisibility(View.VISIBLE);
                mEftpos.setVisibility(View.GONE);
                btnCheque.setVisibility(View.GONE);
                btnSaving.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
            }

            mPutCard_Down.setVisibility(View.VISIBLE);
            mPutCard_Right.setVisibility(View.VISIBLE);
            mCardReadyContent.setVisibility(View.GONE);
            llCardPayment.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mCardNfcUtils.disableDispatch();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       // Toast.makeText(CardActivity.this,"Inside onNewIntent",LENGTH_SHORT).show();
        Log.v("STUBNFC","inside on new");
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mCardNfcAsyncTask = new CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate)
                    .build();
        }
    }

    @Override
    public void startNfcReadCard() {
        Log.v("STUBNFC","inside start");
        mIsScanNow = true;
        mProgressDialog.show();
    }

    @Override
    public void cardIsReadyToRead() {
        Log.v("STUBNFC","inside on card ready");
        Double amt=Double.parseDouble(amount);
        if (amt>=80.00)
        {
            /*String card ="4568963216986325";
            card = getPrettyCardNumber(card);
            Log.v("RESPOS","Resp  "+card);
            String expiredDate = "10/23";
            String cardType = "Credit";
            mCardNumberText.setText(card);
            mExpireDateText.setText(expiredDate);
            setTypeName(card);
            parseCardType(cardType);
            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
            cardName=mCardNumberText.getText().toString();
            String Card= mCardType.getText().toString();
            String cards[]=Card.split("-");

            if (cards.length==2) {
                cardCBType = cards[0];
                cardAcType = cards[1];
            }
            expiryDate=mExpireDateText.getText().toString();*/
            String card = mCardNfcAsyncTask.getCardNumber();
            card = getPrettyCardNumber(card);
            Log.v("RESPOS","Resp  "+card);
            String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
            String cardType = mCardNfcAsyncTask.getCardType();
            mCardNumberText.setText(card);
            mExpireDateText.setText(expiredDate);
            setTypeName(card);
            parseCardType(cardType);
            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
            cardName=mCardNumberText.getText().toString();
            String Card= mCardType.getText().toString();
            String cards[]=Card.split("-");

            if (cards.length==2) {
                cardCBType = cards[0];
                cardAcType = cards[1];
            }
            expiryDate=mExpireDateText.getText().toString();

            //  Toast.makeText(CardActivity.this,"Amount is greater than 80.00",Toast.LENGTH_SHORT).show();
            if (from!=null&&from.equalsIgnoreCase("EFTPos")) {
                mPutCardContent.setVisibility(View.GONE);
                mPutCard_Up.setVisibility(View.GONE);
                mEftpos.setVisibility(View.VISIBLE);
                mCardReadyContent.setVisibility(View.GONE);
                llCardPayment.setVisibility(View.GONE);

                btnCheque.setVisibility(View.VISIBLE);
                btnSaving.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                mPutCard_Down.setVisibility(View.GONE);
                mPutCard_Right.setVisibility(View.GONE);
            }else{
                callKeyboardDialog();
            }
        }else{
            mPutCardContent.setVisibility(View.GONE);
            mPutCard_Up.setVisibility(View.GONE);
            mEftpos.setVisibility(View.GONE);

            if (from!=null&&from.equalsIgnoreCase("EFTPos")) {
                btnCheque.setVisibility(View.VISIBLE);
                btnSaving.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                mPutCard_Down.setVisibility(View.GONE);
                mPutCard_Right.setVisibility(View.GONE);
            }else{
                btnCheque.setVisibility(View.GONE);
                btnSaving.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                mPutCard_Down.setVisibility(View.GONE);
                mPutCard_Right.setVisibility(View.GONE);
            }

            mCardReadyContent.setVisibility(View.GONE);
            llCardPayment.setVisibility(View.GONE);

            String card = mCardNfcAsyncTask.getCardNumber();
            card = getPrettyCardNumber(card);
            Log.v("RESPOS","Resp  "+card);
            String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
            String cardType = mCardNfcAsyncTask.getCardType();
            mCardNumberText.setText(card);
            mExpireDateText.setText(expiredDate);
            setTypeName(card);
            parseCardType(cardType);
            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
            cardName=mCardNumberText.getText().toString();
            String Card= mCardType.getText().toString();
            String cards[]=Card.split("-");

            if (cards.length==2) {
                cardCBType = cards[0];
                cardAcType = cards[1];
            }
            expiryDate=mExpireDateText.getText().toString();
            //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
            status=true;
            if (from!=null&&from.equalsIgnoreCase("EFTPos")) {

            }else {
                callTimeStamp();
            }

        }


       /* mPutCardContent.setVisibility(View.GONE);

        mPutCard_Up.setVisibility(View.GONE);
        mPutCard_Down.setVisibility(View.GONE);
        mPutCard_Right.setVisibility(View.GONE);
        btnCheque.setVisibility(View.GONE);
        btnSaving.setVisibility(View.GONE);
        mCardReadyContent.setVisibility(View.VISIBLE);
        llCardPayment.setVisibility(View.VISIBLE);
        String card = mCardNfcAsyncTask.getCardNumber();
        card = getPrettyCardNumber(card);
        Log.v("RESPOS","Resp  "+card);
        String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
        String cardType = mCardNfcAsyncTask.getCardType();
        mCardNumberText.setText(card);
        mExpireDateText.setText(expiredDate);
        setTypeName(card);
        parseCardType(cardType);
        calculateAmountToPay(rate,mCardType.getText().toString(),amount);*/


    }

    private void setTypeName(String card) {
        if (!card.equals(""))
        {
            String firstOne=card.substring(0,1);
            String firstTwo=card.substring(0,2);
            String firstSix=card.substring(0,6);
            String firstEight=card.substring(0,8);

            //Toast.makeText(CardActivity.this,firstSix,Toast.LENGTH_SHORT).show();

            if (firstTwo.equals("22")||firstOne.equals("5"))
            {
                mCardType.setText("Mastercard-Credit");
                rate="3.00";
                if (firstTwo.equals("50"))
                {
                    if (firstSix.equals("502293"))
                    {
                        mCardType.setText("MasterCard-Debit");
                        rate="0.50";
                    }
                    else {
                        mCardType.setText("Argencard-Credit");
                        rate = "3.00";
                    }
                }
                else if (firstTwo.equals("52"))
                {
                    if (firstEight.equals("52749145"))
                    {
                        mCardType.setText("MasterCard-Debit");
                        rate="0.50";
                    }
                    else if (firstEight.equals("52625319"))
                    {
                        mCardType.setText("MasterCard-Credit");
                        rate="2.20";
                    }
                    else {
                        mCardType.setText("Nativa-Credit");
                        rate="3.00";
                    }
                }
                else {

                    switch (firstSix) {
                        case "502293":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "515796":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "519163":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.20";
                            break;
                        case "531496":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "533389":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "535182":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "535715":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "536618":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "537976":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "538417":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "538419":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "538652":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "540221":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.50";
                            break;
                        case "542782":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.50";
                            break;
                        case "543250":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.20";
                            break;
                        case "544291":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.20";
                            break;
                        case "547343":
                            mCardType.setText("MasterCard-Credit");
                            rate="2.20";
                            break;
                        case "551200":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "555290":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.50";
                            break;
                        case "551201":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "555967":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "558251":
                            mCardType.setText("MasterCard-Credit");
                            rate="2.20";
                            break;
                        case "588951":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "589726":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                    }
                    switch (firstEight)
                    {
                        case "51167330":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.50";
                            break;
                        case "52625319":
                            mCardType.setText("MasterCard-Credit");
                            rate="2.20";
                            break;
                        case "52749145":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;

                        case "53498924":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";

                            break;
                        case "53499172":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "53499548":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "53538585":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "53539611":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "53556334":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "54081935":
                            mCardType.setText("MasterCard-Debit");
                            rate="0.50";
                            break;
                        case "54703628":
                            mCardType.setText("MasterCard-Credit");
                            rate="1.20";
                            break;
                    }


                }
            } else if (firstTwo.equals("62")||firstTwo.equals("88"))
            {
                mCardType.setText("China Union Pay-Credit");
                rate="2.20";
            }
            else if (firstTwo.equals("35"))
            {
                mCardType.setText("JCB-Credit");
                rate="3.00";
            }
            else if (firstTwo.equals("37"))
            {
                mCardType.setText("Amex-Credit");
                rate="3.00";
                switch (firstSix)
                {
                    case "377403":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377423":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377428":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377433":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377439":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377446":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377477":
                        mCardType.setText("AMEX-Debit");
                        rate="0.50";
                        break;
                    case "377478":
                        mCardType.setText("AMEX-Debit");
                        rate="0.50";
                        break;
                    case "377479":
                        mCardType.setText("AMEX-Debit");
                        rate="0.50";
                        break;
                    case "377480":
                        mCardType.setText("AMEX-Debit");
                        rate="0.50";
                        break;

                    case "377803":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377821":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377865":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377876":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                    case "377889":
                        mCardType.setText("AMEX-Credit");
                        rate="2.10";
                        break;
                }
            }
            else if (firstTwo.equals("50"))
            {
                mCardType.setText("Argencard-Credit");
                rate="3.00";
            }
            else if (firstTwo.equals("52"))
            {
                mCardType.setText("Nativa-Credit");
                rate="3.00";
            }
            else if (firstTwo.equals("58"))
            {
                mCardType.setText("Naranja-Credit");
                rate="3.00";
            }
            else if (firstTwo.equals("60"))
            {
                mCardType.setText("Discover-Credit");
                rate="3.00";
            }
            else if (firstTwo.equals("63"))
            {
                mCardType.setText("ELO1-Credit");
                rate="3.00";
            }

            else if(firstTwo.charAt(0)=='4')
            {
                mCardType.setText("VISA-Credit");
                rate="3.00";
                switch (firstSix)
                {
                    case "405547":
                        mCardType.setText("VISA-Credit");
                        rate="1.80";
                        break;

                    case "418224":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "423914":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "428418":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "428429":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "428455":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "436527":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "436773":
                        mCardType.setText("VISA-Credit");
                        rate="1.20";
                        break;
                    case "445047":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "445049":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "454860":
                        mCardType.setText("VISA-Credit");
                        rate="1.20";
                        break;
                    case "454871":
                        mCardType.setText("VISA-Credit");
                        rate="1.20";
                        break;

                    case "462265":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "464278":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "467805":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "469396":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;

                    case "469397":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "478689":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "494310":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "499987":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                }
                switch(firstEight) {
                    //Visa
                    case "43677415":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677434":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677451":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677452":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677454":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677455":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677462":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;

                    case "43677487":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677490":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677495":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "43677463":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45054930":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45054942":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45054953":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45054973":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45054978":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45054990":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061219":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061220":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061226":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061227":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061241":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061246":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061278":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061290":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45061299":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45408433":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "45408435":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "45408436":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "45408439":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "45408442":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "45408450":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "45466851":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "45466852":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408776":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408777":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408778":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408779":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "45408788":
                        mCardType.setText("VISA-Debit");
                        rate="2.20";
                        break;
                    case "46408791":
                        mCardType.setText("VISA-Debit");
                        rate="2.20";
                        break;
                    case "46408792":
                        mCardType.setText("VISA-Debit");
                        rate="2.20";
                        break;
                    case "46408793":
                        mCardType.setText("VISA-Debit");
                        rate="2.20";
                        break;
                    case "46408801":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408802":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408803":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408807":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408808":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "46408809":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "47154008":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "47154009":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "47154020":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "47154026":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "47154033":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "47154050":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "47154067":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "47154078":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "48068210":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "48068221":
                        mCardType.setText("VISA-Credit");
                        rate="1.20";
                        break;
                    case "48068351":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "48068352":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "48068353":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "48068354":
                        mCardType.setText("VISA-Debit");
                        rate="0.50";
                        break;
                    case "48068359":
                        mCardType.setText("VISA-Credit");
                        rate="1.20";
                        break;

                    case "48356104":
                        mCardType.setText("VISA-Debit");
                        rate="1.20";
                        break;

                    case "49887306":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "49887318":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "49887325":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "49887326":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "49887373":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "49887374":
                        mCardType.setText("VISA-Credit");
                        rate="1.50";
                        break;
                    case "49887392":
                        mCardType.setText("VISA-Credit");
                        rate="1.80";
                        break;
                }

            } else{
                mCardType.setText(from + "-Credit");
                rate="3.00";
            }
        }
    }


    @Override
    public void doNotMoveCardSoFast() {
        Log.v("STUBNFC","inside on do nort");
        showSnackBar(mDoNotMoveCardMessage);
    }

    @Override
    public void unknownEmvCard() {
        Log.v("STUBNFC","inside on unknown");
        showSnackBar(mUnknownEmvCardMessage);
       // cardIsReadyToRead();
       // btnTest.performClick();
    }

    @Override
    public void cardWithLockedNfc() {
        Log.v("STUBNFC","inside on do nort");
        showSnackBar(mCardWithLockedNfcMessage);
    }

    @Override
    public void finishNfcReadCard() {
        Log.v("STUBNFC","inside on do nort");
        mProgressDialog.dismiss();
        mCardNfcAsyncTask = null;
        mIsScanNow = false;

        //Testing
    /*   mPutCardContent.setVisibility(View.GONE);
        mCardReadyContent.setVisibility(View.VISIBLE);
        setTypeName(etText.getText().toString());
        calculateAmountToPay(rate,mCardType.getText().toString(),amount);*/

    }

    private void createProgressDialog(){
        String title = getString(R.string.ad_progressBar_title);
        String mess = getString(R.string.ad_progressBar_mess);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(mess);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    private void showSnackBar(String message){
        //Snackbar.make(mToolbar, message, Snackbar.LENGTH_SHORT).show();
        Toast.makeText(CardActivity.this,message,Toast.LENGTH_LONG).show();
    }

    private void showTurnOnNfcDialog(){
        if (mTurnNfcDialog == null) {
            String title = getString(R.string.ad_nfcTurnOn_title);
            String mess = getString(R.string.ad_nfcTurnOn_message);
            String pos = getString(R.string.ad_nfcTurnOn_pos);
            String neg = getString(R.string.ad_nfcTurnOn_neg);
            mTurnNfcDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(mess)
                    .setPositiveButton(pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Send the user to the settings page and hope they turn it on
                            if (android.os.Build.VERSION.SDK_INT >= 16) {
                                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                            } else {
                                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }
                    })
                    .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onBackPressed();
                        }
                    }).create();
        }
        mTurnNfcDialog.show();
    }

    private void initNfcMessages(){
        mDoNotMoveCardMessage = getString(R.string.snack_doNotMoveCard);
        mCardWithLockedNfcMessage = getString(R.string.snack_lockedNfcCard);
        mUnknownEmvCardMessage = getString(R.string.snack_unknownEmv);
    }


    private void parseCardType(String cardType){
        if (cardType.equals(CardNfcAsyncTask.CARD_UNKNOWN)){
          /*  Snackbar.make(mToolbar, getString(R.string.snack_unknown_bank_card), Snackbar.LENGTH_LONG)
                    .setAction("GO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToRepo();
                        }
                    });*/
            AlertDialog.Builder alert=new AlertDialog.Builder(CardActivity.this);
            alert.setMessage(R.string.snack_unknown_bank_card);
            alert.setPositiveButton("GO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    goToRepo();
                }
            });
            alert.show();
        } else if (cardType.equals(CardNfcAsyncTask.CARD_VISA)){
            mCardLogoIcon.setImageResource(R.mipmap.visa_logo);
        } else if (cardType.equals(CardNfcAsyncTask.CARD_MASTER_CARD)){
            mCardLogoIcon.setImageResource(R.mipmap.master_logo);
        }
    }

    private String getPrettyCardNumber(String card){
        String div = " - ";
        return  card.substring(0,4) + div + card.substring(4,8) + div + card.substring(8,12)
                +div + card.substring(12,16);
    }

    private void goToRepo(){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.repoUrl)));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage("com.android.chrome");
        try{
            startActivity(i);
        } catch (ActivityNotFoundException e){
            i.setPackage(null);
            startActivity(i);
        }
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


                        timestamp=userResponse.getTime();
                        if (intent!=null) {
                            String functionName = intent.getStringExtra("functionName");
                            String terminalID = intent.getStringExtra("terminalID");
                            String uniqueID = intent.getStringExtra("uniqueID");
                            String posInfo1 = intent.getStringExtra("posInfo1");
                            String totalAmount = intent.getStringExtra("totalAmount");

                        Log.v("@RESPONSES",totalAmount+" "+timestamp);
                        sendPurchaseResponse(functionName,terminalID,uniqueID,posInfo1,totalAmount,timestamp);
                        }
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
    private void sendPurchaseResponse(String functionName, String terminalID, String uniqueID, String posInfo1, String totalAmount, String timestamp) {

        String transactionId=new Date().getTime() + " ";
        String response ="";
        String ResStatus="";
        String transactionStatus="";
       if (status==true) {
           transactionStatus="COMPLETED";
           if (from.equalsIgnoreCase("EFTPos")) {
               response = "\"functionName\":" + functionName + ", \"terminalId\":" + terminalID + ", \"uniqueId\": " + uniqueID + ", \"transactionId\": \"" + transactionId + "\", \"transactionTimeStamp\": \"" + timestamp + "\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": "+transactionStatus+", \"AmountTotal\": " + totalAmount + ", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"" + cardName+ "\", \"CardType\": \"" + "EFTPOS-Debit" + "\", \"AccountType\": \"" + "Debit" + "\", \"CardExpiry\": \"" + expiryDate + "\", \"Timestamp\": \"" + timestamp + "\", \"RequestTimestamp\": \"" + timestamp + "\", \"ResponseTimestamp\": \"" + timestamp + "\"\n";
           }else{
               response = "\"functionName\":" + functionName + ", \"terminalId\":" + terminalID + ", \"uniqueId\": " + uniqueID + ", \"transactionId\": \"" + transactionId + "\", \"transactionTimeStamp\": \"" + timestamp + "\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": "+transactionStatus+", \"AmountTotal\": " + totalAmount + ", \"Merchant\": \"1\", \"TransactionResult\": \"OK-ACCEPTED\", \"Result\": \"OK\", \"ResultText\": \"Transaction takes longer than usual\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"" + cardName+ "\", \"CardType\": \"" + cardCBType + "\", \"AccountType\": \"" + cardAcType + "\", \"CardExpiry\": \"" + expiryDate + "\", \"Timestamp\": \"" + timestamp + "\", \"RequestTimestamp\": \"" + timestamp + "\", \"ResponseTimestamp\": \"" + timestamp + "\"\n";
           }
           ResStatus="Success";
       }else{
          transactionStatus="CANCELLED";
           if (from.equalsIgnoreCase("EFTPos")) {
            response = "\"functionName\":" + functionName + ", \"terminalId\":" + terminalID + ", \"uniqueId\": " + uniqueID + ", \"transactionId\": \"" + transactionId + "\", \"transactionTimeStamp\": \"" + timestamp + "\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": "+transactionStatus+", \"AmountTotal\": " + totalAmount + ", \"Merchant\": \"1\", \"TransactionResult\": \"FAILED\", \"Result\": \"OK\", \"ResultText\": \"Transaction is Cancelled\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"" + cardName + "\", \"CardType\": \"" + "EFTPOS-Debit" + "\", \"AccountType\": \"" + "Debit" + "\", \"CardExpiry\": \"" + expiryDate + "\", \"Timestamp\": \"" + timestamp + "\", \"RequestTimestamp\": \"" + timestamp + "\", \"ResponseTimestamp\": \"" + timestamp + "\"\n";
           }else{
               response = "\"functionName\":" + functionName + ", \"terminalId\":" + terminalID + ", \"uniqueId\": " + uniqueID + ", \"transactionId\": \"" + transactionId + "\", \"transactionTimeStamp\": \"" + timestamp + "\", \"merchantId\": \"8dac4049-20b3-473f-b3a3-2682667ece33\", \"transactionStatus\": "+transactionStatus+", \"AmountTotal\": " + totalAmount + ", \"Merchant\": \"1\", \"TransactionResult\": \"FAILED\", \"Result\": \"OK\", \"ResultText\": \"Transaction is Cancelled\", \"AuthId\": \"PIN147\", \"AcquirerRef\": \"000013\", \"CardPan\": \"" + cardName + "\", \"CardType\": \"" + cardCBType + "\", \"AccountType\": \"" + cardAcType + "\", \"CardExpiry\": \"" + expiryDate + "\", \"Timestamp\": \"" + timestamp + "\", \"RequestTimestamp\": \"" + timestamp + "\", \"ResponseTimestamp\": \"" + timestamp + "\"\n";
           }
            ResStatus="Failed";
       }
       Intent intent1=new Intent();
        intent1.putExtra("ResponseFor","purchase");
        intent1.putExtra("message",response);
        intent1.putExtra("uniqueID",uniqueID);
        intent1.putExtra("transactionStatus",transactionStatus);
        intent1.putExtra("status",ResStatus);
        Log.v("RESPOS","passing "+cardName);
        Log.v("@RESPONSES",response);
        setResult(200,intent1);
       /* new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);*/
        if ((progressDialog != null) && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        finish();
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
    String cardCBType="";
    String cardAcType="";
    String expiryDate="";



    private void onSwipeCard() {
        showNormalMessage("Please swipe card");
        mMagCardDevice.swipeCard(40000, true, new IMagCardListener.Stub(){

            @Override
            public void onSwipeCardTimeout() throws RemoteException {
                showErrorMessage("Swipe card time out");
            }

            @Override
            public void onSwipeCardException(int i) throws RemoteException {
                showErrorMessage("Swipe card error " + i);
            }

            @Override
            public void onSwipeCardSuccess(TrackData trackData) throws RemoteException {
                showNormalMessage("Card number " + trackData.getCardno()+"\n Expiry Date "+trackData.getExpiryDate());
                showSuccessMessageSwipe(trackData.getCardno(),trackData.getExpiryDate());
                // displayData(""+trackData.getCardno());



            }

            @Override
            public void onSwipeCardFail() throws RemoteException {
                showErrorMessage("Swipe card failed ");
            }

            @Override
            public void onCancelSwipeCard() throws RemoteException {
                showNormalMessage("Swipe card canceled");
            }
        });
    }


    private void showSuccessMessageSwipe(String card_num, String expiryDate) {
        // txtShow.setText("Data "+please_swipe_card);
        Log.v("DATARESPONSE","Normal "+card_num+" "+expiryDate);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
               // txtShow.setText(please_swipe_card);
                Log.v("DATARESPONSE","Run "+card_num+" "+expiryDate);
                Double amt=Double.parseDouble(amount);
                if (amt>=80.00)
                {
            /*String card ="4568963216986325";
            card = getPrettyCardNumber(card);
            Log.v("RESPOS","Resp  "+card);
            String expiredDate = "10/23";
            String cardType = "Credit";
            mCardNumberText.setText(card);
            mExpireDateText.setText(expiredDate);
            setTypeName(card);
            parseCardType(cardType);
            calculateAmountToPay(rate,mCardType.getText().toString(),amount);
            cardName=mCardNumberText.getText().toString();
            String Card= mCardType.getText().toString();
            String cards[]=Card.split("-");

            if (cards.length==2) {
                cardCBType = cards[0];
                cardAcType = cards[1];
            }
            expiryDate=mExpireDateText.getText().toString();*/
                    String card = card_num;
                    card = getPrettyCardNumber(card);
                    Log.v("RESPOS","Resp  "+card);
                    String expiredDate = expiryDate;
                    String cardType = "Credit";
                    mCardNumberText.setText(card);
                    mExpireDateText.setText(expiredDate);
                    setTypeName(card);
                    parseCardType(cardType);
                    calculateAmountToPay(rate,mCardType.getText().toString(),amount);
                    cardName=mCardNumberText.getText().toString();
                    String Card= mCardType.getText().toString();
                    String cards[]=Card.split("-");
                     Log.v("STUBNFC","card"+Card);
                    if (cards.length==2) {
                        cardCBType = cards[0];
                        cardAcType = cards[1];
                    }
                    CardActivity.this.expiryDate =mExpireDateText.getText().toString();

                    //  Toast.makeText(CardActivity.this,"Amount is greater than 80.00",Toast.LENGTH_SHORT).show();
                    if (from!=null&&from.equalsIgnoreCase("EFTPos")) {
                        mPutCardContent.setVisibility(View.GONE);
                        mPutCard_Up.setVisibility(View.GONE);
                        mEftpos.setVisibility(View.VISIBLE);
                        mCardReadyContent.setVisibility(View.GONE);
                        llCardPayment.setVisibility(View.GONE);

                        btnCheque.setVisibility(View.VISIBLE);
                        btnSaving.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);
                    }else{
                        callKeyboardDialog();
                    }
                }else{
                    mPutCardContent.setVisibility(View.GONE);
                    mPutCard_Up.setVisibility(View.GONE);
                    mEftpos.setVisibility(View.GONE);

                    if (from!=null&&from.equalsIgnoreCase("EFTPos")) {
                        btnCheque.setVisibility(View.VISIBLE);
                        btnSaving.setVisibility(View.VISIBLE);
                        btnCancel.setVisibility(View.VISIBLE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);
                    }else{
                        btnCheque.setVisibility(View.GONE);
                        btnSaving.setVisibility(View.GONE);
                        btnCancel.setVisibility(View.GONE);
                        mPutCard_Down.setVisibility(View.GONE);
                        mPutCard_Right.setVisibility(View.GONE);
                    }

                    mCardReadyContent.setVisibility(View.GONE);
                    llCardPayment.setVisibility(View.GONE);

                    String card = card_num;
                    card = getPrettyCardNumber(card);
                    Log.v("RESPOS","Resp  "+card);
                    String expiredDate = expiryDate;
                    String cardType = "Credit";
                    mCardNumberText.setText(card);
                    mExpireDateText.setText(expiredDate);
                    setTypeName(card);
                    parseCardType(cardType);
                    calculateAmountToPay(rate,mCardType.getText().toString(),amount);
                    cardName=mCardNumberText.getText().toString();
                    String Card= mCardType.getText().toString();
                    String cards[]=Card.split("-");

                    if (cards.length==2) {
                        cardCBType = cards[0];
                        cardAcType = cards[1];
                    }
                    CardActivity.this.expiryDate =mExpireDateText.getText().toString();
                    //  Toast.makeText(MainActivity.this,cardName+" "+cardType+" "+cardAcType, LENGTH_SHORT).show();
                    if (from!=null&&from.equalsIgnoreCase("EFTPos")) {

                    }else{
                        status = true;
                        callTimeStamp();
                    }

                }


       /* mPutCardContent.setVisibility(View.GONE);

        mPutCard_Up.setVisibility(View.GONE);
        mPutCard_Down.setVisibility(View.GONE);
        mPutCard_Right.setVisibility(View.GONE);
        btnCheque.setVisibility(View.GONE);
        btnSaving.setVisibility(View.GONE);
        mCardReadyContent.setVisibility(View.VISIBLE);
        llCardPayment.setVisibility(View.VISIBLE);
        String card = mCardNfcAsyncTask.getCardNumber();
        card = getPrettyCardNumber(card);
        Log.v("RESPOS","Resp  "+card);
        String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
        String cardType = mCardNfcAsyncTask.getCardType();
        mCardNumberText.setText(card);
        mExpireDateText.setText(expiredDate);
        setTypeName(card);
        parseCardType(cardType);
        calculateAmountToPay(rate,mCardType.getText().toString(),amount);*/



            }
        });
    }
    private void showErrorMessage(String please_swipe_card) {

        Log.v("DATARESPONSE",""+please_swipe_card);
    }

    private void showNormalMessage(String please_swipe_card) {
        // txtShow.setText("Data "+please_swipe_card);
        Log.v("DATARESPONSE","Normal "+please_swipe_card);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //txtShow.setText(please_swipe_card);
                Toast.makeText(CardActivity.this," "+please_swipe_card, LENGTH_SHORT).show();
            }
        });
    }
}