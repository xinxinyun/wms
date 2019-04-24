package com.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.com.tools.Beeper;
import com.contants.WmsContanst;
import com.job.StorgeJob;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;
import com.uhf.uhf.UHFApplication;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "仓储库存监听";
    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;

    private Button startupBtn;
    private Button shutdownBtn;

    private JobManager jobManager;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>();

    /**
     * RFID监听
     */
    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            String epcCode = tag.strEPC;
            if (!epcCodeList.contains(epcCode)) {
                epcCodeList.add(epcCode);
                //添加识别码到消息队列。
                jobManager.addJobInBackground(new StorgeJob(epcCode));
                //调用蜂鸣声提示已扫描到商品
                Beeper.beep(Beeper.BEEPER_SHORT);
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("门店消磁程序");

        startupBtn = findViewById(R.id.startupBtn);
        shutdownBtn = findViewById(R.id.shutdownBtn);

        startupBtn.setOnClickListener(this);
        shutdownBtn.setOnClickListener(this);

        jobManager = UHFApplication.getJobManager();

        //开启FRID识别
        startup();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startupBtn:
                startup();
                break;
            case R.id.shutdownBtn:
                shutdown();
                break;
        }
    }

    /**
     * 启动程序开始扫描
     */
    private void startup() {
        //实时扫描多少个物资
        if (connector.connectCom(WmsContanst.TTYS1, WmsContanst.baud)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rxObserver);
                //设定读取间隔时间
                Thread.currentThread().sleep(500);
                mReader.realTimeInventory((byte) 0xff, (byte) 0x01);

                startupBtn.setVisibility(View.GONE);
                shutdownBtn.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setContentText("RFID设备模块读取失败！");
                sweetAlertDialog.show();
                return;
            }
        } else {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setContentText("RFID设备模块读取失败！");
            sweetAlertDialog.show();
            return;
        }
    }

    private void shutdown() {
        //RFID模块下线
        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
        shutdownBtn.setVisibility(View.GONE);
        startupBtn.setVisibility(View.VISIBLE);
    }
}
