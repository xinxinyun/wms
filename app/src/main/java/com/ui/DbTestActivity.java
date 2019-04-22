package com.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bean.MaterialInventory;
import com.uhf.uhf.R;
import com.util.DatabaseUtils;

public class DbTestActivity extends AppCompatActivity {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_test);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //必须先初始化
        DatabaseUtils.initHelper(this, "wms.db");

        DatabaseUtils.getHelper().createTableIfNotExists(MaterialInventory.class);
       /* //创建学生类
        Student student1 = new Student("张三", "1001", 12);

        //将学生类保存到数据库
        DatabaseUtils.getHelper().save(student1);

        DatabaseUtils.getHelper().queryAll(Student.class);

        Toast.makeText(getApplication(), "保存成功", Toast.LENGTH_SHORT).show();

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, String> paramsMap = new HashMap<>();
        headerMap.put("Content-Type", CONTENT_TYPE);//头部信息
        paramsMap.put("userName", "1111");//参数
        paramsMap.put("password", "2222");
        OkhttpUtil.okHttpPost("", paramsMap, headerMap, new CallBackUtil.CallBackDefault() {//回调
            @Override
            public void onFailure(Call call, Exception e) {
                //LogUtil.e(TAG, e.getMessage());
                //TVAppUtil.showToast("网络异常！请确认是否联网，以及服务器地址是否正确！");
                //SharedPreferencesUtil.sharePut("newServerAddress","");
            }

            @Override
            public void onResponse(Response response) {
                *//*try {
                    isLogin = true;
                    Headers responseHeaders = response.headers();
                    String c = responseHeaders.get("Set-Cookie");//cookie的处理
                    TVAppUtil.setCookie(c);
                    updateReportData();
                    HandlerUtil.sendTimingRequest();
                } catch (Exception e) {
                    LogUtil.e(TAG, e.getMessage());
                    TVAppUtil.showToast("登录异常！");
                } finally {
                    try {
                        response.body().close();
                    } catch (Exception e) {
                    }
                }*//*
            }
        });*/
    }

}

class Student {
    //姓名
    private String name;

    //学号
    private String nubmer;

    //年龄
    private int age;

    public Student() {
    }

    public Student(String name, String nubmer, int age) {
        this.name = name;
        this.nubmer = nubmer;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNubmer() {
        return nubmer;
    }

    public void setNubmer(String nubmer) {
        this.nubmer = nubmer;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}