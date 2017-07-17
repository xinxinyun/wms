package com.uhf.uhf.spiner;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.com.tools.ExcelUtils;
import com.reader.helper.InventoryBuffer;
import com.uhf.uhf.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Showing the information from scan engine
 *
 * @author Administrator
 */
public class DialogCustomed {

    private Builder builder;
    private int layout;
    private AlertDialog dialog;
    private SpinerPopWindow mFileDirListView;
    private Context mContext;
    private Window window;

    private List<InventoryBuffer.InventoryTagMap> mTags = null;

    private EditText mFileName = null;
    private TextView mDirectory = null;
    private Button mCancel ;
    private Button mOk;

    private List<String> mFileDirList = new ArrayList<String>();
    private File SDCardRootDir = Environment.getExternalStorageDirectory();
    private String mRootDirMark = "..";

    InputMethodManager mInputMethodManager;

    public DialogCustomed(Context context, int layout) {
        builder = new Builder(context);
        mContext = context;
        mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        getDirectory(SDCardRootDir.getAbsolutePath());
        this.layout = layout;
    }

    @Deprecated
    public DialogCustomed(Context context, int theme, int layout) {
        builder = new Builder(context, theme);
        mContext = context;
        mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        getDirectory(SDCardRootDir.getAbsolutePath());
        this.layout = layout;
    }

    public Builder getBuilder() {
        return builder;
    }

    /**
     * get the object of window
     *
     * @return
     */
    public Window getDialog() {
        dialog = builder.create();
        dialog.setView(new EditText(mContext));
        dialog.show();
        window = dialog.getWindow();
        window.setContentView(layout);
        initView();
        return window;
    }

    private void initView() {
        mFileDirListView = new SpinerPopWindow(mContext);
        mFileDirListView.refreshData(mFileDirList, 0);
        mFileDirListView.setItemListener(new AbstractSpinerAdapter.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                /*
                if (!mDirectory.getText().toString().equals(mRootDirMark)) {
                    mDirectory.setText(mDirectory.getText().toString() + "/" + mFileDirList.get(pos));
                    Log.d("include!",mDirectory.getText().toString() + "/" + mFileDirList.get(pos));
                }
                */
                mDirectory.setText(mFileDirList.get(pos));
            }
        });
        mFileName = (EditText) dialog.findViewById(R.id.file_name);
        mCancel = (Button) dialog.findViewById(R.id.save_excel_cancel);
        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        mOk = (Button) dialog.findViewById(R.id.save_excel_ok);
        mOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFileName.getText().toString().equals("") || mFileName == null || mFileName.length() == 0) {
                    Toast.makeText(mContext,"The file is not allowed null!",Toast.LENGTH_LONG).show();
                    return;
                } else if (mTags == null || mTags.size() == 0) {
                    Toast.makeText(mContext,"The tags is empty!",Toast.LENGTH_LONG).show();
                    return;
                }
                final ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle("");
                progressDialog.setMessage("");
                progressDialog.show();
                final String file = SDCardRootDir.getAbsolutePath() + "/" + mDirectory.getText().toString() + "/" + mFileName.getText().toString() ;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ExcelUtils.writeTagToExcel(file,mTags);
                       MediaScannerConnection.scanFile(mContext,
                                new String[] {file}, null, null);
                        progressDialog.dismiss();
                        dialog.dismiss();
                    }
                }).start();

            }
        });
        mDirectory = (TextView) dialog.findViewById(R.id.file_dir);
        mDirectory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String directory = mDirectory.getText().toString();
                getDirectory(directory);
                mFileDirListView.refreshData(mFileDirList, 0);
                mFileDirListView.setWidth(mDirectory.getWidth());
                mFileDirListView.showAsDropDown(mDirectory);
            }
        });

        mDirectory.setText(mRootDirMark);
        mDirectory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * get view via the id of view
     *
     * @param id
     * @return
     */
    public View getViewById(int id) {
        if (dialog == null) getDialog();
        return dialog.findViewById(id);
    }

    /**
     * setting the view
     *
     * @param id
     */
    public void setDismissButtonId(int id) {
        View view = getViewById(id);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setTags(List<InventoryBuffer.InventoryTagMap> maps) {
        this.mTags = maps;
    }

    /**
     * close the dialog
     */
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void getDirectory(String filename) {
        mFileDirList.clear();
        mFileDirList.add(mRootDirMark);
        if (filename.equals(mRootDirMark)) {
            filename = SDCardRootDir.getAbsolutePath();
        } else if (!filename.contains(SDCardRootDir.getAbsolutePath())) {
            filename = SDCardRootDir.getAbsolutePath() + "/" + filename;
        }
        Log.d("file",filename);
        File file = new File(filename);
        if (file.listFiles() != null) {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    if (filename.equals(SDCardRootDir.getAbsolutePath())) {
                        mFileDirList.add(f.getName());
                    } else {
                        mFileDirList.add(f.getParentFile().getAbsolutePath().replace(SDCardRootDir.getAbsolutePath() + "/","")+ "/" + f.getName());
                    }
                    Log.d("filename",f.getName());
                }
            }
        }

    }

    private void saveFileToDB(String fileName) {
        ContentValues cv = new ContentValues();
        File file = new File(fileName);
        long current = System.currentTimeMillis();
        Date date = new Date(current);
        SimpleDateFormat formatter = new SimpleDateFormat();
        String title = formatter.format(date);
        // Lets label the recorded audio file as NON-MUSIC so that the file
        // won't be displayed automatically, except for in the playlist.
        cv.put(MediaStore.MediaColumns.DATA, fileName);
        cv.put(MediaStore.MediaColumns.TITLE, title);
        cv.put(MediaStore.MediaColumns.MIME_TYPE, file.getAbsolutePath());
        cv.put(MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.MEDIA_TYPE_NONE);

        ContentResolver resolver = mContext.getContentResolver();
        Uri base = MediaStore.Files.getContentUri("external");
        Uri result = resolver.insert(base, cv);
    }
}
