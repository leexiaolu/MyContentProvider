package com.example.leesnriud.mycontentprovider;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private ContentResolver resolver;
    private Uri uri;
    private Cursor cursor;

    @BindView(R.id.tv_text)
    TextView tvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.bt_one, R.id.bt_two, R.id.bt_three, R.id.bt_four, R.id.bt_five})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_one:
                uri = Uri.parse("content://sms/");
                resolver = getContentResolver();
                //获取的是哪些列的信息
                cursor = resolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);
                while (cursor.moveToNext()) {
                    String address = cursor.getString(0);
                    String date = cursor.getString(1);
                    String type = cursor.getString(2);
                    String body = cursor.getString(3);
                    Log.e("123","地址："+address);
                    Log.e("123","时间："+date);
                    Log.e("123","类型："+type);
                    Log.e("123","内容："+body);
                    Log.e("123","-----------------");
                }
                cursor.close();
                break;
            case R.id.bt_two:
                //5.0及以上无法写入,出现以下异常
                //java.lang.SecurityException: Permission Denial: writing com.android.providers.telephony.SmsProvider
                resolver = getContentResolver();
                uri = Uri.parse("content://sms/");
                ContentValues conValues = new ContentValues();
                conValues.put("address", "123456789");
                conValues.put("type", 1);
                conValues.put("date", System.currentTimeMillis());
                conValues.put("body", "测试短信");
                resolver.insert(uri, conValues);
                Log.e("HeHe", "短信插入完毕~");
                break;
            case R.id.bt_three:
                //①查询raw_contacts表获得联系人的id
                ContentResolver resolver = getContentResolver();
                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                //查询联系人数据
                cursor = resolver.query(uri, null, null, null, null);
                while(cursor.moveToNext())
                {
                    //获取联系人姓名,手机号码
                    String cName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String cNum = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.e("123","姓名:" + cName);
                    Log.e("123","号码:" + cNum);
                    Log.e("123","-------------");
                }
                cursor.close();
                break;
            case R.id.bt_four:
                queryContact("1245");
                break;
            case R.id.bt_five:
                try {
                    AddContact();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    //查询指定号码联系人
    private void queryContact(String number){
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            Log.e("123",number + "对应的联系人名称：" + name);
        }
        cursor.close();
    }

    //添加联系人
    private void AddContact() throws RemoteException, OperationApplicationException {
        //使用事务添加联系人
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri dataUri =  Uri.parse("content://com.android.contacts/data");

        ContentResolver resolver = getContentResolver();
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation op1 = ContentProviderOperation.newInsert(uri)
                .withValue("account_name", null)
                .build();
        operations.add(op1);

        //依次是姓名，号码，邮编
        ContentProviderOperation op2 = ContentProviderOperation.newInsert(dataUri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/name")
                .withValue("data2", "lee")
                .build();
        operations.add(op2);

        ContentProviderOperation op3 = ContentProviderOperation.newInsert(dataUri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/phone_v2")
                .withValue("data1", "15566555566")
                .withValue("data2", "2")
                .build();
        operations.add(op3);

        ContentProviderOperation op4 = ContentProviderOperation.newInsert(dataUri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/email_v2")
                .withValue("data1", "000000@qq.com")
                .withValue("data2", "2")
                .build();
        operations.add(op4);
        //将上述内容添加到手机联系人中~
        resolver.applyBatch("com.android.contacts", operations);
        Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_SHORT).show();
    }
}
